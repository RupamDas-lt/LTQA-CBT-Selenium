package automationHelper;

import Pages.*;
import TestManagers.CapabilityManager;
import TestManagers.DriverManager;
import com.mysql.cj.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import utility.BaseClass;
import utility.CustomSoftAssert;
import utility.EnvSetup;

import java.util.Map;

import static factory.SoftAssertionMessages.*;
import static utility.EnvSetup.TEST_SCENARIO_NAME;
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
        clientSoftAssert.softAssertMessageFormat(USER_IS_NOT_ABLE_TO_LOGGED_IN_CLIENT_ERROR_MESSAGE));
    }
    EnvSetup.CLIENT_SOFT_ASSERT.set(clientSoftAssert);
    LTHooks.endStepContext(driverManager, "login");
  }

  public void navigateToDashboardOfSpecificTest(String testId) {
    LTHooks.startStepContext(driverManager, "Navigate to Test home page");
    LTDashboardCommonActions testHomePage = new LTDashboardCommonActions(testId, driverManager);
    Assert.assertTrue(testHomePage.navigateToHomePageOfSpecificTest(), "Unable to open test home page");
  }

  private String constructNetworkLogsFileName(Map<String, Object> capabilities) {
    /// CBT_Selenium_Test_2025-04-23-browserName=chrome,platform=sonoma,version=._,performance=true,resolution=._,network=true,visual=true,tunnel=true,console=true-network-logs.har
    final String postFixForNetworkLogsWithExtension = "network-logs.har";
    String buildName = (String) capabilities.get(BUILD_NAME);
    String testName = (String) capabilities.get(TEST_NAME);
    String finalNetworkLogsFileName = String.format("%s-%s-%s", buildName, testName, postFixForNetworkLogsWithExtension)
      .replace("*", "_");
    ltLogger.info("Network logs file name: {}", finalNetworkLogsFileName);
    return finalNetworkLogsFileName;
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
      softAssert.fail(softAssert.softAssertMessageFormat(UNSUPPORTED_LOGS_TYPE_CLIENT_ERROR_MESSAGE, logName));
      break;
    }
  }

  private void verifyCommandLogs(String testId, CustomSoftAssert softAssert) {
    TestCommandLogsPage commandLogsPage = new TestCommandLogsPage(testId, driverManager, softAssert);
    if (!commandLogsPage.openCommandLogsTab()) {
      softAssert.fail(softAssert.softAssertMessageFormat(UNABLE_TO_OPEN_COMMAND_LOGS_TAB_CLIENT_ERROR_MESSAGE));
      return;
    }
    commandLogsPage.verifyCommandLogs();
  }

  private void verifyNetworkLogs(String testId, CustomSoftAssert softAssert) {
    TestNetworkLogsPage networkLogsPage = new TestNetworkLogsPage(testId, driverManager, softAssert);
    if (!networkLogsPage.openNetworkLogsTab()) {
      softAssert.fail(softAssert.softAssertMessageFormat(UNABLE_TO_OPEN_NETWORK_LOGS_TAB_CLIENT_ERROR_MESSAGE));
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
      softAssert.fail(softAssert.softAssertMessageFormat(UNABLE_TO_OPEN_SYSTEM_LOGS_TAB_CLIENT_ERROR_MESSAGE));
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
      softAssert.fail(softAssert.softAssertMessageFormat(UNABLE_TO_OPEN_CONSOLE_LOGS_TAB_CLIENT_ERROR_MESSAGE));
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
      softAssert.fail(softAssert.softAssertMessageFormat(UNABLE_TO_OPEN_PERFORMANCE_REPORT_TAB_CLIENT_ERROR_MESSAGE));
      return;
    }
    performanceReportPage.isPerformanceReportDisplayed();
  }
}
