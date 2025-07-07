package automationHelper;

import Pages.*;
import TestManagers.CapabilityManager;
import TestManagers.DriverManager;
import com.mysql.cj.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.BaseClass;
import utility.CustomAssert;
import utility.CustomSoftAssert;
import utility.EnvSetup;

import java.util.Map;

import static factory.SoftAssertionMessages.*;
import static utility.EnvSetup.*;
import static utility.FrameworkConstants.*;

public class ClientAutomationHelper extends BaseClass {

    private static final String defaultClientCapabilities = "browserName=chrome,version=latest,platform=win10,network=true,idleTimeout=900";

    private final Logger ltLogger = LogManager.getLogger(ClientAutomationHelper.class);
    DriverManager driverManager = new DriverManager();
    CapabilityManager capabilityManager = new CapabilityManager();

    public void startClientSession() {
        String capsString = StringUtils.isNullOrEmpty(TEST_SCENARIO_NAME.get()) ?
                defaultClientCapabilities :
                defaultClientCapabilities + ",name=" + TEST_SCENARIO_NAME.get().replace(",", "_");
        capabilityManager.buildClientTestCapability(capsString);
        driverManager.createClientDriver();
        EnvSetup.IS_UI_VERIFICATION_ENABLED.set(true);
    }

    public void stopClientSession() {
        driverManager.quit();
    }

    public void loginToLTDashboard() {
        CustomSoftAssert clientSoftAssert = EnvSetup.CLIENT_SOFT_ASSERT.get();
        LTHooks.startStepContext(driverManager, "login");
        LoginPage loginPage = new LoginPage(driverManager);
        boolean loginUsingCookies = false;
        //    loginUsingCookies = loginPage.loginToLTDashboardUsingCookies();
        ltLogger.info("Login done using cookies: {}", loginUsingCookies);
        if (!loginUsingCookies) {
            boolean status = loginPage.navigateToLoginPage();
            if (!status)
                throw new RuntimeException("Unable to navigate to login page");
            else {
                loginPage.fillUpLoginForm();
                loginPage.clickSubmitButton();
            }
            clientSoftAssert.assertTrue(loginPage.verifyUserIsLoggedIn(),
                    softAssertMessageFormat(USER_IS_NOT_ABLE_TO_LOGGED_IN_CLIENT_ERROR_MESSAGE));
        }
        EnvSetup.CLIENT_SOFT_ASSERT.set(clientSoftAssert);
        LTHooks.endStepContext(driverManager, "login");
    }

    public void navigateToDashboardOfSpecificTest(String testId) {
        LTHooks.startStepContext(driverManager, "Navigate to Test home page");
        LTDashboardCommonActions testHomePage = new LTDashboardCommonActions(testId, driverManager);
        CustomAssert.assertTrue(testHomePage.navigateToHomePageOfSpecificTest(),
                softAssertMessageFormat(UNABLE_TO_NAVIGATE_TO_LT_HOME_PAGE));
    }

    private String constructNetworkLogsFileName(Map<String, Object> capabilities) {
        /// CBT_Selenium_Test_2025-04-23-browserName=chrome,platform=sonoma,version=._,performance=true,resolution=._,network=true,visual=true,tunnel=true,console=true-network-logs.har
        final String postFixForNetworkLogs = "network-logs";
        final String extension = ".har";
        String buildName = (String) capabilities.get(BUILD_NAME);
        String testName = (String) capabilities.get(TEST_NAME);
        String finalNetworkLogsFileName = String.format("%s-%s-%s", buildName, testName, postFixForNetworkLogs)
                .replace("*", "_");
        if (finalNetworkLogsFileName.length() > MAX_FILE_NAME_LENGTH_FOR_DOWNLOADS_WITH_SELENIUM_WEB_DRIVER) {
            ltLogger.warn("Network logs file name is too long: {}. Truncating to {} characters.", finalNetworkLogsFileName,
                    MAX_FILE_NAME_LENGTH_FOR_DOWNLOADS_WITH_SELENIUM_WEB_DRIVER);
            finalNetworkLogsFileName = finalNetworkLogsFileName.substring(0,
                    MAX_FILE_NAME_LENGTH_FOR_DOWNLOADS_WITH_SELENIUM_WEB_DRIVER);
        }
        String finalNetworkLogsFileNameWithExtension = finalNetworkLogsFileName + extension;
        ltLogger.info("Network logs file name: {}", finalNetworkLogsFileNameWithExtension);
        return finalNetworkLogsFileNameWithExtension;
    }

    private String constructSystemLogsFileName(String testID) {
        /// lambda-logs-selenium-DA-WIN-375492-1745478266638369730JAA.log
        final String systemLogsFileNamePrefix = "lambda-logs-selenium-";
        final String fileExtension = ".log";
        String finalSystemLogsFileName = systemLogsFileNamePrefix + testID + fileExtension;
        ltLogger.info("System logs file name: {}", finalSystemLogsFileName);
        return finalSystemLogsFileName;
    }

    private String constructConsoleLogFileName(String testID) {
        /// lambda-logs-console-DA-MAC-672987-1745494124374001474CIL.log
        final String consoleLogsFileNamePrefix = "lambda-logs-console-";
        final String fileExtension = ".log";
        String finalConsoleLogsFileName = consoleLogsFileNamePrefix + testID + fileExtension;
        ltLogger.info("Console logs file name: {}", finalConsoleLogsFileName);
        return finalConsoleLogsFileName;
    }

    private boolean isLogValidForCurrentTestConfig(String logType) {
        Map<String, Object> capsMap = EnvSetup.TEST_CAPS_MAP.get();
        if (capsMap == null) {
            return false;
        }
        switch (logType) {
            case "console" -> {
                return capsMap.get(BROWSER_NAME).toString().equalsIgnoreCase("chrome") || capsMap.get(BROWSER_NAME).toString()
                        .equalsIgnoreCase("edge");
            }
            case "performance" -> {
                return capsMap.get(BROWSER_NAME).toString().equalsIgnoreCase("chrome");
            }
            default -> {
                return true;
            }
        }
    }

    public void verifyTestLogsFromUI(String testId, String logName) {
        verifyTestArtifactFromUI(testId, logName, "logs");
    }

    public void verifyTestMediaFromUI(String testId, String testMediaName) {
        verifyTestArtifactFromUI(testId, testMediaName, "media");
    }

    private void verifyTestArtifactFromUI(String testId, String artifactName, String artifactType) {
        // Wait for artifact to be uploaded
        waitForSomeTimeAfterTestCompletionForLogsToBeUploaded(EnvSetup.TEST_REPORT.get().get(TEST_END_TIMESTAMP).toString(),
                120);

        CustomSoftAssert clientSoftAssert = EnvSetup.CLIENT_SOFT_ASSERT.get();
        String stepName = String.format("Verify %s %s from UI", artifactName, artifactType);

        try {
            LTHooks.startStepContext(driverManager, stepName);
            verifyTestLogsAndMediaFromUI(testId, artifactName, clientSoftAssert);
        } finally {
            LTHooks.endStepContext(driverManager, stepName);
            EnvSetup.CLIENT_SOFT_ASSERT.set(clientSoftAssert);
        }
    }

    private void verifyTestLogsAndMediaFromUI(String testId, String logName, CustomSoftAssert softAssert) {

        String jobPurpose = System.getProperty(JOB_PURPOSE, "");
        // List of artefacts to skip for smoke tests
        if (jobPurpose.equalsIgnoreCase("smoke") && skipArtefactsForSmokeTests.contains(logName)) {
            ltLogger.info("Skipping UI verification of {} logs for smoke tests", logName);
            System.err.printf("Skipping UI verification of %s logs for smoke tests%n", logName);
            return;
        }

        switch (logName.toLowerCase()) {
            case "command":
                verifyCommandLogs(testId, softAssert);
                break;
            case "network":
                verifyNetworkLogs(testId, softAssert);
                break;
            case "system":
                verifySystemLogs(testId, softAssert);
                break;
            case "console":
                verifyConsoleLogs(testId, softAssert);
                break;
            case "video":
                verifyTestVideo(testId, softAssert);
                break;
            case "performancereport":
                verifyPerformanceReport(testId, softAssert);
                break;
            default:
                softAssert.fail(softAssertMessageFormat(UNSUPPORTED_LOGS_TYPE_CLIENT_ERROR_MESSAGE, logName));
                break;
        }
    }

    private void verifyCommandLogs(String testId, CustomSoftAssert softAssert) {
        TestCommandLogsPage commandLogsPage = new TestCommandLogsPage(testId, driverManager, softAssert);
        if (!commandLogsPage.openCommandLogsTab()) {
            softAssert.fail(softAssertMessageFormat(UNABLE_TO_OPEN_COMMAND_LOGS_TAB_CLIENT_ERROR_MESSAGE));
            return;
        }
        commandLogsPage.verifyCommandLogs();
    }

    private void verifyNetworkLogs(String testId, CustomSoftAssert softAssert) {
        TestNetworkLogsPage networkLogsPage = new TestNetworkLogsPage(testId, driverManager, softAssert);
        if (!networkLogsPage.openNetworkLogsTab()) {
            softAssert.fail(softAssertMessageFormat(UNABLE_TO_OPEN_NETWORK_LOGS_TAB_CLIENT_ERROR_MESSAGE));
            return;
        }
        networkLogsPage.verifyAllExpectedNetworkCallsArePresentInTheUI();
        String networkLogsName = constructNetworkLogsFileName(EnvSetup.TEST_CAPS_MAP.get());
        networkLogsPage.downloadNetworkLogsFromUI(networkLogsName);
        networkLogsPage.openNetworkLogsInNewTabAndVerify();
    }

    private void verifySystemLogs(String testId, CustomSoftAssert softAssert) {
        TestSystemLogsPage systemLogsPage = new TestSystemLogsPage(testId, driverManager, softAssert);
        if (!systemLogsPage.openSystemLogsTab()) {
            softAssert.fail(softAssertMessageFormat(UNABLE_TO_OPEN_SYSTEM_LOGS_TAB_CLIENT_ERROR_MESSAGE));
            return;
        }
        systemLogsPage.verifySystemLogs();
        String testID = systemLogsPage.getTestIDFromTestDashboard();
        String systemLogsName = constructSystemLogsFileName(testID);
        systemLogsPage.downloadSystemLogsFromUI(systemLogsName);
        systemLogsPage.openAndVerifySystemLogsInNewTab();
    }

    private void verifyConsoleLogs(String testId, CustomSoftAssert softAssert) {
        TestConsoleLogsPage consoleLogsPage = new TestConsoleLogsPage(testId, driverManager, softAssert);
        if (!consoleLogsPage.openConsoleLogsTab()) {
            softAssert.fail(softAssertMessageFormat(UNABLE_TO_OPEN_CONSOLE_LOGS_TAB_CLIENT_ERROR_MESSAGE));
            return;
        }
        if (!isLogValidForCurrentTestConfig("console")) {
            consoleLogsPage.verifyConsoleLogsNotSupportedMessageDisplayed();
            return;
        }
        consoleLogsPage.verifyConsoleLogsFromUI();
        String testID = consoleLogsPage.getTestIDFromTestDashboard();
        String consoleLogFileName = constructConsoleLogFileName(testID);
        consoleLogsPage.downloadConsoleLogsFromUI(consoleLogFileName);
        consoleLogsPage.openConsoleLogsInNewTabAndVerify();
    }

    private void verifyTestVideo(String testId, CustomSoftAssert softAssert) {
        TestVideoPage testVideoPage = new TestVideoPage(testId, driverManager, softAssert);
        testVideoPage.navigateToHomePageOfSpecificTest();
        testVideoPage.validateTestVideo();
    }

    private void verifyPerformanceReport(String testId, CustomSoftAssert softAssert) {
        if (isLogValidForCurrentTestConfig("performance")) {
            return;
        }
        TestPerformanceReportPage performanceReportPage = new TestPerformanceReportPage(testId, driverManager, softAssert);
        if (!performanceReportPage.openPerformanceReportTab()) {
            softAssert.fail(softAssertMessageFormat(UNABLE_TO_OPEN_PERFORMANCE_REPORT_TAB_CLIENT_ERROR_MESSAGE));
            return;
        }
        performanceReportPage.isPerformanceReportDisplayed();
    }

    private void verifyTestShareLinkViaUI() {
        LTHooks.startStepContext(driverManager, "Verify Test Share Link via UI");
        CustomSoftAssert clientSoftAssert = EnvSetup.CLIENT_SOFT_ASSERT.get();

        // Test data
        String testShareLink = TEST_VERIFICATION_DATA.get().getOrDefault(testVerificationDataKeys.TEST_SHARE_LINK, "")
                .toString();
        String buildName = TEST_CAPS_MAP.get().getOrDefault(BUILD_NAME, "").toString();
        String testName = TEST_CAPS_MAP.get().getOrDefault(TEST_NAME, "").toString();

        TestShareLinkPage testShareLinkPage = new TestShareLinkPage(driverManager, clientSoftAssert, testShareLink);
        boolean status = testShareLinkPage.navigateToShareLink();
        if (status) {
            testShareLinkPage.verifyBuildName(buildName);
            testShareLinkPage.verifyTestName(testName);
            testShareLinkPage.verifyArtefactsAreDisplayed();
        }
        LTHooks.endStepContext(driverManager, "Verify Test Share Link via UI");
    }

    private void verifyBuildShareLinkPage() {
        LTHooks.startStepContext(driverManager, "Verify Build Share Link via UI");
        CustomSoftAssert clientSoftAssert = EnvSetup.CLIENT_SOFT_ASSERT.get();

        // Test data
        String buildShareLink = TEST_VERIFICATION_DATA.get().getOrDefault(testVerificationDataKeys.BUILD_SHARE_LINK, "")
                .toString();
        String buildName = MULTIPLE_TEST_CAPS_MAP.get().values().iterator().next().getOrDefault(BUILD_NAME, "").toString();
        String testName = MULTIPLE_TEST_CAPS_MAP.get().values().iterator().next().getOrDefault(TEST_NAME, "").toString();

        BuildShareLinkPage buildShareLinkPage = new BuildShareLinkPage(driverManager, clientSoftAssert, buildShareLink);
        boolean status = buildShareLinkPage.navigateToShareLink();
        if (status) {
            buildShareLinkPage.verifyBuildName(buildName);
            buildShareLinkPage.verifyTestList(testName);
        }
        LTHooks.endStepContext(driverManager, "Verify Build Share Link via UI");
    }

    public void verifyShareLinkViaUI(String linkType) {
        switch (linkType.toLowerCase()) {
            case "test":
                verifyTestShareLinkViaUI();
                break;
            case "build":
                verifyBuildShareLinkPage();
                break;
            default:
                ltLogger.warn("Unsupported link type: {}", linkType);
                throw new RuntimeException("Unsupported link type: " + linkType);
        }
    }

    public void runTestActions(String testAction) {
        CustomSoftAssert clientSoftAssert = EnvSetup.CLIENT_SOFT_ASSERT.get();
        ManualAccessibilitySessionPage manualAccessibilitySessionPage = new ManualAccessibilitySessionPage(driverManager, clientSoftAssert);
        switch (testAction) {
            case "appControls" -> {
                manualAccessibilitySessionPage.verifyAppControls();
            }
            case "screenshot" -> {
                manualAccessibilitySessionPage.screenshot();
            }
            case "recordSession" -> {
                manualAccessibilitySessionPage.recordSession();
            }
            case "rotate" -> {
                manualAccessibilitySessionPage.rotate();
            }

            default -> {
                CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();

                System.out.println("The action " + testAction + " is not recognized and does not match any case within the switch statement, thus executing the default case.");

                softAssert.assertFalse(true, String.format(
                        "The action '%s' is not recognized and does not match any case within the switch statement, thus executing the default case.",
                        testAction));
                EnvSetup.SOFT_ASSERT.set(softAssert);
            }
        }
    }

    public void openManualAccessibilityPage() {
        CustomSoftAssert clientSoftAssert = EnvSetup.CLIENT_SOFT_ASSERT.get();

        ManualAccessibilityPage manualAccessibilityPage = new ManualAccessibilityPage(driverManager, clientSoftAssert);
        manualAccessibilityPage.navigateToManualAccessibilityPage();

    }

    public void iSelectAppAndDevice(String OS) {
        CustomSoftAssert clientSoftAssert = EnvSetup.CLIENT_SOFT_ASSERT.get();
        ManualAccessibilityPage manualAccessibilityPage = new ManualAccessibilityPage(driverManager, clientSoftAssert);
        manualAccessibilityPage.iSelectAppAndDevice(OS);

    }

    public void iVerifyTestStartedOrNot() {
        CustomSoftAssert clientSoftAssert = EnvSetup.CLIENT_SOFT_ASSERT.get();
        ManualAccessibilitySessionPage manualAccessibilitySessionPage = new ManualAccessibilitySessionPage(driverManager, clientSoftAssert);
        manualAccessibilitySessionPage.verifyTestStartedOrNot();
    }

    public void verifyScanHappeningAndTestSavingOrNot() {
        CustomSoftAssert clientSoftAssert = EnvSetup.CLIENT_SOFT_ASSERT.get();
        ManualAccessibilitySessionPage manualAccessibilitySessionPage = new ManualAccessibilitySessionPage(driverManager, clientSoftAssert);
        manualAccessibilitySessionPage.verifyScanHappeningOrNot();
        manualAccessibilitySessionPage.verifyTestSavingOrNot();
    }

    public void verifyIssueTabAndImages() {
        CustomSoftAssert clientSoftAssert = EnvSetup.CLIENT_SOFT_ASSERT.get();
        ManualAccessibilitySessionPage manualAccessibilitySessionPage = new ManualAccessibilitySessionPage(driverManager, clientSoftAssert);
        manualAccessibilitySessionPage.verifyIssueTabAndImages();
    }

    public void iVerifyLeftNavbar(String testActions) {
        String[] testActionsArray = testActions.split(",");
        for (String testAction : testActionsArray) {
            runTestActions(testAction);
        }
    }

    public void iStopAccessibilityTest() {
        CustomSoftAssert clientSoftAssert = EnvSetup.CLIENT_SOFT_ASSERT.get();
        ManualAccessibilitySessionPage manualAccessibilitySessionPage = new ManualAccessibilitySessionPage(driverManager, clientSoftAssert);
        manualAccessibilitySessionPage.iStopAccessibilityTest();
    }

    public void iOpenManualDashboard() {
        CustomSoftAssert clientSoftAssert = EnvSetup.CLIENT_SOFT_ASSERT.get();
        ManualAccessibilityDashboardPage manualAccessibilityDashboardPage = new ManualAccessibilityDashboardPage(driverManager, clientSoftAssert);
        manualAccessibilityDashboardPage.iOpenManualDashboard();
        manualAccessibilityDashboardPage.iSearchForTheTest();
    }

    public void iValidateA11yReportAllIssuesAndMobileView() {
        CustomSoftAssert clientSoftAssert = EnvSetup.CLIENT_SOFT_ASSERT.get();
        ManualAccessibilityDashboardPage manualAccessibilityDashboardPage = new ManualAccessibilityDashboardPage(driverManager, clientSoftAssert);
        manualAccessibilityDashboardPage.iValidateAccessibilityReport();
        manualAccessibilityDashboardPage.iValidateAllIssuesTab();
        manualAccessibilityDashboardPage.iValidateMobileView();
    }
}
