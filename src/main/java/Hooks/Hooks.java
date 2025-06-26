package Hooks;

import automationHelper.AutomationAPIHelper;
import com.mysql.cj.util.StringUtils;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.plugin.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reportingHelper.TestFailureReportManager;
import utility.BaseClass;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utility.EnvSetup.*;
import static utility.FrameworkConstants.*;

public class Hooks extends BaseClass {
    private final Logger ltLogger = LogManager.getLogger(Hooks.class);
    private final AutomationAPIHelper apiHelper = new AutomationAPIHelper();
    private final StringBuilder combinedAssertionErrorMessage = new StringBuilder();
    private boolean testAssertionError = false;
    private boolean clientAssertionError = false;
    private String clientTestStatus = "passed";
    private String clientTestErrorMessage = "";
    private String errorMessage = "";
    private String testStatus = "passed";
    private int beforeScenarioCount = 0;
    private int afterScenarioCount = 0;

    @Before(order = 1)
    public void beforeScenario(Scenario scenario) {
        if (System.getProperty("ENV") == null)
            throw new RuntimeException("ENV not set");
        TEST_SCENARIO_NAME.set(scenario.getName());
    }

    @Before(order = 2, value = "@tunnel_regression")
    public void beforeTunnelRegression() {
        ltLogger.info("Executing bash script for updating local hosts mapping");
        runBashScriptWithFlags(BASH_SCRIPT_PATH_FOR_UPDATE_LOCAL_HOSTS_MAPPING, false, "--addEntry");
    }

    private String getStackTrace(Throwable error) {
        StringWriter sw = new StringWriter();
        error.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private void updateScenarioStatusIfNeeded(Scenario scenario) {
        try {
            List<String> stepNames = getStepNames(scenario);
            TestCaseState testCaseState = getTestCaseState(scenario);

            List<Result> results = getStepResults(testCaseState);

            for (int i = 0; i < results.size(); i++) {
                Result result = results.get(i);
                if ("FAILED".equalsIgnoreCase(result.getStatus().name())) {
                    int expectedStepIndex = i - beforeScenarioCount;
                    String failedStepName = expectedStepIndex < stepNames.size() ?
                            stepNames.get(expectedStepIndex) :
                            "Unknown Step";
                    ltLogger.info("Failed step: {}", failedStepName);
                    handleFailedStep(result, failedStepName);
                    break;
                }
            }
        } catch (ReflectiveOperationException e) {
            ltLogger.error("Failed to update scenario status due to reflection error", e);
        }
    }

    private TestCaseState getTestCaseState(Scenario scenario) throws NoSuchFieldException, IllegalAccessException {
        Field delegateField = scenario.getClass().getDeclaredField("delegate");
        delegateField.setAccessible(true);
        return (TestCaseState) delegateField.get(scenario);
    }

    private List<Result> getStepResults(TestCaseState testCaseState) throws NoSuchFieldException, IllegalAccessException {
        Field stepResultsField = testCaseState.getClass().getDeclaredField("stepResults");
        stepResultsField.setAccessible(true);
        @SuppressWarnings("unchecked") List<Result> results = (List<Result>) stepResultsField.get(testCaseState);
        return results;
    }

    private List<String> getStepNames(Scenario scenario) throws NoSuchFieldException, IllegalAccessException {
        List<String> stepNames = new ArrayList<>();
        try {
            Field delegateField = scenario.getClass().getDeclaredField("delegate");
            delegateField.setAccessible(true);
            Object delegate = delegateField.get(scenario);

            Field testCaseField = delegate.getClass().getDeclaredField("testCase");
            testCaseField.setAccessible(true);
            TestCase testCase = (TestCase) testCaseField.get(delegate);

            int beforeHookCount = 0;
            int afterHookCount = 0;

            for (TestStep step : testCase.getTestSteps()) {
                if (step instanceof PickleStepTestStep) {
                    stepNames.add(((PickleStepTestStep) step).getStep().getText());
                } else if (step instanceof HookTestStep hookStep) {
                    if (hookStep.getHookType() == HookType.BEFORE) {
                        beforeHookCount++;
                    } else if (hookStep.getHookType() == HookType.AFTER) {
                        afterHookCount++;
                    }
                }
            }
            beforeScenarioCount = beforeHookCount;
            afterScenarioCount = afterHookCount;
            ltLogger.info("Number of @Before Hooks: {}", beforeHookCount);
            ltLogger.info("Number of @After Hooks: {}", afterHookCount);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return stepNames;
    }

    private void handleFailedStep(Result failResult, String failedStepName) {
        clientTestStatus = "failed";
        String stepErrorMessage = failResult.getError().getMessage();
        String errorStackTrace = getStackTrace(failResult.getError());

        StringBuilder errorMessageBuilder = new StringBuilder();
        if (clientTestErrorMessage.isEmpty()) {
            errorMessageBuilder.append("Failed Step: ").append(failedStepName).append("\nError: ").append(stepErrorMessage);
        } else {
            errorMessageBuilder.append(clientTestErrorMessage).append("\nFailed Step: ").append(failedStepName)
                    .append("\nError: ").append(stepErrorMessage);
        }
        clientTestErrorMessage = errorMessageBuilder.toString();

        ltLogger.debug("Scenario failed at step: {}", failedStepName);
        ltLogger.debug("Scenario failed with error: {}", stepErrorMessage);
        ltLogger.debug("Scenario failed with stacktrace: {}", errorStackTrace);

        // Update test status if the failed step is critical
        Pattern patterForCriticalStep = Pattern.compile(
                "^I start session ([a-zA-Z0-9_=,: ]+) driver quit to test ([a-zA-Z0-9_=,: ]+) with ([^\"]*)$");
        Matcher matcher = patterForCriticalStep.matcher(failedStepName);
        if (matcher.find()) {
            testStatus = "failed";
            errorMessage = clientTestErrorMessage;
        }

        TEST_REPORT.get().put("failed_step", failedStepName);
        TEST_REPORT.get().put("failed_step_error", stepErrorMessage);

        if (!isErrorAlreadyAddedInErrorList(stepErrorMessage)) {
            FAILED_ASSERTION_ERROR_TO_HASH_KEY_MAP.get().put(stepErrorMessage, "NA");
        }
    }

    private boolean isErrorAlreadyAddedInErrorList(String errorMessage) {
        for (String existingError : FAILED_ASSERTION_ERROR_TO_HASH_KEY_MAP.get().keySet()) {
            if (errorMessage.contains(existingError)) {
                ltLogger.info("Framework error: {}, matches with existing error: {}", errorMessage, existingError);
                return true; // Error already exists, no need to add again
            }
        }
        return false;
    }

    private void setTestStatus(String testId, String testStatus, String remarks, boolean... isClient) {
        HashMap<String, String> updatedPayload = new HashMap<>();
        updatedPayload.put("status_ind", testStatus);

        if (FAILED.equalsIgnoreCase(testStatus)) {
            String formattedErrorMessage = remarks.replaceAll("(\r\n|\n|\r)", "\\\\n").replaceAll("\t", "");
            updatedPayload.put("reason", formattedErrorMessage);
        }

        apiHelper.updateSessionDetailsViaAPI(testId, updatedPayload, isClient);
    }

    private void updateTestReport(Scenario scenario) {
        TEST_REPORT.get().put("test_env", TEST_ENV);
        TEST_REPORT.get().put("scenarioName", scenario.getName());
        TEST_REPORT.get().put("scenarioHashCode", scenario.hashCode());
        TEST_REPORT.get().put("userName", testUserName.get());
        TEST_REPORT.get().put("accessKey", testAccessKey.get());
        TEST_REPORT.get().put("hub", testGridUrl.get());
        TEST_REPORT.get().put("test_status", testStatus);
        TEST_REPORT.get().put("client_test_status", IS_UI_VERIFICATION_ENABLED.get() ? clientTestStatus : "NA");
    }

    private void printTestDashboardAndRetinaLinks(Scenario scenario, String testEnv) {
        if ("local".equalsIgnoreCase(testEnv)) {
            ltLogger.info("Local test environment, no dashboard URLs to print...");
            return;
        }

        String endPoint = switch (testEnv.toLowerCase()) {
            case "browserstack" -> "/dashboard/v2/sessions/";
            case "saucelab" -> "/tests/";
            default -> "/test?testID=";
        };

        StringBuilder stringBuilder = new StringBuilder();

        // Construct test session URLs
        constructUrlsFromSessionsQueue(TEST_SESSION_ID_QUEUE.get(), endPoint, testEnv, TEST_DASHBOARD_URL_BASE,
                TEST_RETINA_URL_BASE, "Test", stringBuilder);

        // Construct client session URLs if UI verification is enabled
        if (IS_UI_VERIFICATION_ENABLED.get()) {
            constructUrlsFromSessionsQueue(CLIENT_TEST_SESSION_ID_QUEUE.get(), endPoint, testEnv, CLIENT_DASHBOARD_URL_BASE,
                    CLIENT_RETINA_URL_BASE, "Client test", stringBuilder);
        }

        scenario.log(stringBuilder.toString());
    }

    private void constructUrlsFromSessionsQueue(Queue<String> queue, String endPoint, String testEnv,
                                                String dashboardBase, String retinaBase, String labelPrefix, StringBuilder outBuilder) {
        if (queue == null || queue.isEmpty())
            return;

        boolean isRetinaAllowed = !(testEnv.equalsIgnoreCase("browserstack") || testEnv.equalsIgnoreCase("saucelab"));
        boolean doesQueueContainsOnlyOneSession = queue.size() == 1;

        int index = 1;
        while (!queue.isEmpty()) {
            String sessionId = queue.poll();
            String dashboardUrl = apiHelper.constructAPIUrl(dashboardBase, endPoint, sessionId);
            outBuilder.append(labelPrefix).append(" dashboard URL");
            if (!doesQueueContainsOnlyOneSession)
                outBuilder.append(" of test number ").append(index);
            outBuilder.append(": ").append(dashboardUrl).append("\n");

            String retinaUrl = "NA";
            if (isRetinaAllowed) {
                retinaUrl = apiHelper.constructAPIUrl(retinaBase, "/search/?query=", sessionId);
                outBuilder.append(labelPrefix).append(" retina URL");
                if (!doesQueueContainsOnlyOneSession)
                    outBuilder.append(" of test number ").append(index);
                outBuilder.append(": ").append(retinaUrl).append("\n");
            }

            ltLogger.info("Index: {}, {} dashboard URL: {},\n{} retina URL: {}", index, labelPrefix, dashboardUrl,
                    labelPrefix, retinaUrl);

            index++;
        }
    }

    private void handleAssertionError(AssertionError e, boolean isClient) {
        String errorType = isClient ? "Client Assertion Error" : "Test Assertion Error";
        String errorMsg = e.getLocalizedMessage();

        if (isClient) {
            errorMsg = "The following assertions failed in client test for test session id: " + TEST_SESSION_ID.get() + "client session id: " + CLIENT_SESSION_ID.get() + "\n" + errorMsg;
            clientAssertionError = true;
            clientTestErrorMessage = clientTestErrorMessage.isEmpty() ? errorMsg : clientTestErrorMessage + "\n" + errorMsg;
            clientTestStatus = "failed";
            TEST_REPORT.get().put("client_test_assertion_errors", errorMsg);
        } else {
            errorMsg = "The following assertions failed for test session id: " + TEST_SESSION_ID.get() + "\n" + errorMsg;
            testAssertionError = true;
            errorMessage = errorMessage.isEmpty() ? errorMsg : errorMessage + "\n" + errorMsg;
            testStatus = "failed";
            TEST_REPORT.get().put("test_assertion_errors", errorMsg);
        }

        if (!combinedAssertionErrorMessage.isEmpty()) {
            combinedAssertionErrorMessage.append("\n");
        }
        combinedAssertionErrorMessage.append(errorType).append(": ").append(errorMsg);

        ltLogger.debug("{}: {}", errorType, errorMsg);
    }

    private void assertAll() {
        try {
            SOFT_ASSERT.get().assertAll();
        } catch (AssertionError e) {
            handleAssertionError(e, false);
        }
    }

    private void assertAllClient() {
        try {
            CLIENT_SOFT_ASSERT.get().assertAll();
        } catch (AssertionError e) {
            handleAssertionError(e, true);
        }
    }

    private void throwErrorBasedOnAssertions() {
        if (testAssertionError || clientAssertionError) {
            String finalErrorMessage = combinedAssertionErrorMessage.toString();
            ltLogger.info("Throwing combined assertion errors: {}", finalErrorMessage);
            throw new AssertionError(finalErrorMessage);
        }
    }

    private void closeAllActiveDrivers() {
        try {
            testDriver.get().quit();
        } catch (Exception ignored) {
            // Driver quit failure can be safely ignored
        }

        try {
            clientDriver.get().quit();
        } catch (Exception ignored) {

        }
    }

    private void updateLTTestStatus() {
        if (!StringUtils.isNullOrEmpty(TEST_SESSION_ID.get()) && COMPLETED.equalsIgnoreCase(
                apiHelper.getStatusOfSessionViaAPI(TEST_SESSION_ID.get()))) {
            ltLogger.warn("Test status: {}, Test Error message: {}", testStatus, errorMessage);
            setTestStatus(TEST_SESSION_ID.get(), testStatus, errorMessage);
        }

        if (!StringUtils.isNullOrEmpty(CLIENT_SESSION_ID.get()) && COMPLETED.equalsIgnoreCase(
                apiHelper.getStatusOfSessionViaAPI(CLIENT_SESSION_ID.get(), true))) {
            ltLogger.warn("Client test status: {}, Client Test Error message: {}", clientTestStatus, clientTestErrorMessage);
            setTestStatus(CLIENT_SESSION_ID.get(), clientTestStatus, clientTestErrorMessage, true);
        }
    }

    private void handleTestDataForSumoLogic() {
        if (System.getProperty(SEND_DATA_TO_SUMO, "false").equalsIgnoreCase("true")) {
            String customDataFromEnvVar = System.getProperty(PUT_CUSTOM_DATA_TO_SUMO_PAYLOAD, "");
            if (!StringUtils.isNullOrEmpty(customDataFromEnvVar)) {
                HashMap<String, Object> customDataMapFromCLI = getHashMapFromString(customDataFromEnvVar);
                TEST_REPORT.get().put("custom_data_from_cli", customDataMapFromCLI);
            }
            apiHelper.sendCustomDataToSumo(TEST_REPORT.get());
        }
    }

    @After(order = 1)
    public void afterScenario(Scenario scenario) {
        closeAllActiveDrivers();

        apiHelper.waitForTime(5);

        if (!TEST_ENV.equalsIgnoreCase("browserStack") && !TEST_ENV.equalsIgnoreCase("sauceLab")) {
            updateLTTestStatus();
        }

        updateTestReport(scenario);

        printTestDashboardAndRetinaLinks(scenario, TEST_ENV);

        handleTestDataForSumoLogic();

        resetTestData();
    }

    private static final Map<String, ThreadLocal<?>> threadLocalMap = new HashMap<>() {{
        put("TEST_CAPS_MAP", TEST_CAPS_MAP);
        put("MULTIPLE_TEST_CAPS_MAP", MULTIPLE_TEST_CAPS_MAP);
        put("SOFT_ASSERT", SOFT_ASSERT);
        put("CLIENT_SOFT_ASSERT", CLIENT_SOFT_ASSERT);
        put("IS_UI_VERIFICATION_ENABLED", IS_UI_VERIFICATION_ENABLED);
        put("TEST_SESSION_ID", TEST_SESSION_ID);
        put("TEST_TEST_ID", TEST_TEST_ID);
        put("CLIENT_SESSION_ID", CLIENT_SESSION_ID);
        put("TEST_REPORT", TEST_REPORT);
        put("TEST_VERIFICATION_DATA", TEST_VERIFICATION_DATA);
        put("MULTIPLE_TEST_VERIFICATION_DATA", MULTIPLE_TEST_VERIFICATION_DATA);
        put("TEST_DETAIL_API_RESPONSE", TEST_DETAIL_API_RESPONSE);
        put("TEST_FEATURE_FLAG_DETAILS", TEST_FEATURE_FLAG_DETAILS);
        put("SESSION_COMMAND_LOGS_COUNT_FROM_TEST_API", SESSION_COMMAND_LOGS_COUNT_FROM_TEST_API);
        put("SESSION_EXCEPTION_LOGS_COUNT_FROM_TEST_API", SESSION_EXCEPTION_LOGS_COUNT_FROM_TEST_API);
        put("SESSION_VISUAL_LOGS_COUNT_FROM_TEST_API", SESSION_VISUAL_LOGS_COUNT_FROM_TEST_API);
        put("TEST_TUNNEL_NAME", TEST_TUNNEL_NAME);
        put("TEST_TUNNEL_ID", TEST_TUNNEL_ID);
        put("BUILD_ID", BUILD_ID);
        put("TUNNEL_START_COMMAND", TUNNEL_START_COMMAND);
        put("ASSERTION_ERROR_TO_HASH_KEY_MAP", ASSERTION_ERROR_TO_HASH_KEY_MAP);
        put("FAILED_ASSERTION_ERROR_TO_HASH_KEY_MAP", FAILED_ASSERTION_ERROR_TO_HASH_KEY_MAP);
        put("TEST_SESSION_ID_QUEUE", TEST_SESSION_ID_QUEUE);
        put("CLIENT_TEST_SESSION_ID_QUEUE", CLIENT_TEST_SESSION_ID_QUEUE);
    }};

    private void resetTestData() {
        threadLocalMap.forEach((key, threadLocal) -> threadLocal.remove());
        TEST_REPORT.set(new HashMap<>());
        TEST_VERIFICATION_DATA.set(new HashMap<>());
    }

    @After(order = 3)
    public void updateTestStatus(Scenario scenario) {
        ltLogger.info("Test report: {}", TEST_REPORT.get());
        updateScenarioStatusIfNeeded(scenario);
        assertAll();
        assertAllClient();
        throwErrorBasedOnAssertions();
    }

    @After(order = 2)
    public void pushTestFailureReportToTestReport() {
        TestFailureReportManager testFailureReportManager = new TestFailureReportManager();
        if (System.getProperty(SEND_DATA_TO_SUMO, "false").equalsIgnoreCase("true")) {
            testFailureReportManager.publishTestFailureReportToSumoLogic(FAILED_ASSERTION_ERROR_TO_HASH_KEY_MAP.get());
        }
    }
}
