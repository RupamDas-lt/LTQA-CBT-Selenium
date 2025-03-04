package Hooks;

import automationHelper.AutomationAPIHelper;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.plugin.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.EnvSetup;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static utility.EnvSetup.*;
import static utility.FrameworkConstants.COMPLETED;
import static utility.FrameworkConstants.FAILED;

public class Hooks {
  private final Logger ltLogger = LogManager.getLogger(Hooks.class);
  private final AutomationAPIHelper apiHelper = new AutomationAPIHelper();
  private final StringBuilder combinedAssertionErrorMessage = new StringBuilder();
  private boolean testAssertionError = false;
  private boolean clientAssertionError = false;
  private String clientTestStatus = "passed";
  private String clientTestErrorMessage = "";
  private String errorMessage = "";
  private String scenarioName;
  private String testStatus = "passed";
  private int beforeScenarioCount = 0;
  private int afterScenarioCount = 0;

  @Before(order = 1)
  public void beforeScenario(Scenario scenario) {
    if (System.getProperty("ENV") == null)
      throw new RuntimeException("ENV not set");
    TEST_SCENARIO_NAME.set(scenario.getName());
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
    if (failedStepName.contains("I start session to test")) {
      testStatus = "failed";
      errorMessage = clientTestErrorMessage;
    }
  }

  private void setTestStatus(String testId, String testStatus, String remarks) {
    HashMap<String, String> updatedPayload = new HashMap<>();
    updatedPayload.put("status_ind", testStatus);

    if (FAILED.equalsIgnoreCase(testStatus)) {
      String formattedErrorMessage = remarks.replaceAll("(\r\n|\n|\r)", "\\\\n").replaceAll("\t", "");
      updatedPayload.put("reason", formattedErrorMessage);
    }

    apiHelper.updateSessionDetailsViaAPI(testId, updatedPayload);
  }

  private void setTestStatus() {
    HashMap<String, String> updatedPayload = new HashMap<>();
    updatedPayload.put("status_ind", testStatus);

    if (FAILED.equalsIgnoreCase(testStatus)) {
      String formattedErrorMessage = errorMessage.replaceAll("(\r\n|\n|\r)", "\\\\n").replaceAll("\t", "");
      updatedPayload.put("reason", formattedErrorMessage);
    }

    apiHelper.updateSessionDetailsViaAPI(TEST_SESSION_ID.get(), updatedPayload);
  }

  private void updateTestReport() {
    TEST_REPORT.get().put("scenarioName", scenarioName);
    TEST_REPORT.get().put("userName", EnvSetup.testUserName.get());
    TEST_REPORT.get().put("accessKey", EnvSetup.testAccessKey.get());
    TEST_REPORT.get().put("hub", EnvSetup.testGridUrl.get());
  }

  private void printTestDashboardAndRetinaLinks(Scenario scenario) {
    StringBuilder stringBuilder = new StringBuilder();
    String testDashboardUrl = apiHelper.constructAPIUrl(EnvSetup.TEST_DASHBOARD_URL_BASE, "/test?testID=",
      TEST_SESSION_ID.get());
    stringBuilder.append("Dashboard URL: ").append(testDashboardUrl).append("\n");
    String testRetinaUrl = apiHelper.constructAPIUrl(EnvSetup.TEST_RETINA_URL_BASE, "/search/?query=",
      TEST_SESSION_ID.get());
    stringBuilder.append("Test Retina URL: ").append(testRetinaUrl).append("\n");
    if (EnvSetup.IS_UI_VERIFICATION_ENABLED.get()) {
      String clientTestDashboardUrl = apiHelper.constructAPIUrl(EnvSetup.TEST_DASHBOARD_URL_BASE, "/test?testID=",
        EnvSetup.CLIENT_SESSION_ID.get());
      stringBuilder.append("Client test dashboard URL: ").append(clientTestDashboardUrl).append("\n");
      String clientTestRetinaUrl = apiHelper.constructAPIUrl(EnvSetup.TEST_RETINA_URL_BASE, "/search/?query=",
        EnvSetup.CLIENT_SESSION_ID.get());
      stringBuilder.append("Client test retina URL: ").append(clientTestRetinaUrl);
    }
    ltLogger.info("Test dashboard URL: {},\nTest Retina url: {}", testDashboardUrl, testRetinaUrl);
    scenario.log(stringBuilder.toString());
  }

  private void handleAssertionError(AssertionError e, boolean isClient) {
    String errorType = isClient ? "Client Assertion Error" : "Test Assertion Error";
    String errorMsg = e.getLocalizedMessage();

    if (isClient) {
      clientAssertionError = true;
      clientTestErrorMessage = clientTestErrorMessage.isEmpty() ? errorMsg : clientTestErrorMessage + "\n" + errorMsg;
      clientTestStatus = "failed";
    } else {
      testAssertionError = true;
      errorMessage = errorMessage.isEmpty() ? errorMsg : errorMessage + "\n" + errorMsg;
      testStatus = "failed";
    }

    if (!combinedAssertionErrorMessage.isEmpty()) {
      combinedAssertionErrorMessage.append("\n");
    }
    combinedAssertionErrorMessage.append(errorType).append(": ").append(errorMsg);

    ltLogger.debug("{}: {}", errorType, errorMsg);
  }

  private void assertAll() {
    try {
      EnvSetup.SOFT_ASSERT.get().assertAll();
    } catch (AssertionError e) {
      handleAssertionError(e, false);
    }
  }

  private void assertAllClient() {
    try {
      EnvSetup.CLIENT_SOFT_ASSERT.get().assertAll();
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
      EnvSetup.testDriver.get().quit();
    } catch (Exception ignored) {
      // Driver quit failure can be safely ignored
    }

    try {
      EnvSetup.clientDriver.get().quit();
    } catch (Exception ignored) {

    }
  }

  @After(order = 1)
  public void afterScenario(Scenario scenario) {
    closeAllActiveDrivers();

    apiHelper.waitForTime(5);

    if (TEST_SESSION_ID.get() != null && COMPLETED.equalsIgnoreCase(
      apiHelper.getSpecificSessionDetailsViaAPI(TEST_SESSION_ID.get(), "status_ind"))) {
      ltLogger.warn("Test status: {}, Test Error message: {}", testStatus, errorMessage);
      setTestStatus(TEST_SESSION_ID.get(), testStatus, errorMessage);
    }

    if (CLIENT_SESSION_ID.get() != null && COMPLETED.equalsIgnoreCase(
      apiHelper.getSpecificSessionDetailsViaAPI(CLIENT_SESSION_ID.get(), "status_ind"))) {
      ltLogger.warn("Client test status: {}, Client Test Error message: {}", clientTestStatus, clientTestErrorMessage);
      setTestStatus(CLIENT_SESSION_ID.get(), clientTestStatus, clientTestErrorMessage);
    }

    updateTestReport();

    printTestDashboardAndRetinaLinks(scenario);

    apiHelper.sendCustomDataToSumo(TEST_REPORT.get());
  }

  @After(order = 2)
  public void updateTestStatus(Scenario scenario) {
    scenarioName = scenario.getName();
    ltLogger.info("Test report: {}", TEST_REPORT.get());
    updateScenarioStatusIfNeeded(scenario);
    assertAll();
    assertAllClient();
    throwErrorBasedOnAssertions();
  }
}
