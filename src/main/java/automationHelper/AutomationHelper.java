package automationHelper;

import TestManagers.CapabilityManager;
import TestManagers.DriverManager;
import TestManagers.TunnelManager;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Keys;
import org.testng.Assert;
import utility.BaseClass;
import utility.CustomSoftAssert;
import utility.EnvSetup;

import java.util.Map;

import static utility.EnvSetup.*;
import static utility.FrameworkConstants.*;
import static utility.UrlsAndLocators.*;

public class AutomationHelper extends BaseClass {
  private final Logger ltLogger = LogManager.getLogger(AutomationHelper.class);
  CapabilityManager capabilityManager = new CapabilityManager();
  DriverManager driverManager = new DriverManager();
  TunnelManager tunnelManager;
  AutomationAPIHelper apiHelper = new AutomationAPIHelper();

  private void createTestSession(String testCapability) {
    StopWatch stopWatch = new StopWatch();
    capabilityManager.buildTestCapability(testCapability);
    ltLogger.info("Test Caps: {}", EnvSetup.TEST_CAPS.get().toJson());
    TEST_REPORT.get().put("Caps", EnvSetup.TEST_CAPS.get().toJson());
    stopWatch.start();
    driverManager.createTestDriver();
    driverManager.getCookies();
    stopWatch.stop();
    EnvSetup.TEST_REPORT.get().put(TEST_SETUP_TIME, String.valueOf(stopWatch.getTime() / 1000.00));
  }

  private void runTestActions(String actionName) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    startTestContext(actionName);
    try {
      switch (actionName) {
      case "local":
        testLocalUrlWithTunnel();
      case "basicAuthentication":
        basicAuthentication();
        break;
      case "selfSigned":
        selfSignedCertificate();
        break;
      case "consoleLog":
        addConsoleLogs();
        break;
      case "timezone":
        verifyTimezone();
        break;
      case "fillFormUsingKeyboard":
        fillFormUsingKeyboard();
        break;
      case "exceptionLogTesting":
        addExceptionLogs();
        break;
      case "browserOSDetails":
        browserOSDetails();
        break;
      case "verifyExtension":
        verifyExtension();
        break;
      default:
        baseTest();
        break;
      }
    } catch (Exception e) {
      TEST_ERR_REPORT.get().put(actionName, e.getMessage());
      softAssert.fail(
        actionName + " test action failed. \nError message: " + e.getMessage() + "\nStack trace: " + ExceptionUtils.getStackTrace(
          e));
    }
    endTestContext(actionName);
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void startTestContext(String actionName) {
    driverManager.executeScript(LAMBDA_TEST_CASE_START + "=" + actionName);
  }

  private void endTestContext(String actionName) {
    driverManager.executeScript(LAMBDA_TEST_CASE_END + "=" + actionName);
  }

  private void basicAuthentication() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    driverManager.getURL(BASIC_AUTH);
    String pageHeading = driverManager.getText(basicAuthHeading);
    softAssert.assertTrue(pageHeading.equals("Basic Auth"), "Basic Authentication Failed");
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void addConsoleLogs() {
    driverManager.getURL(GOOGLE_URL);
    consoleLogs.forEach(consoleLog -> {
      driverManager.executeScript("console.error('" + consoleLog + "')");
    });
  }

  private void addExceptionLogs() {
    locatorsForExceptionLogs.forEach(invalidLocator -> {
      try {
        driverManager.waitForElementToBeVisible(invalidLocator, 5);
      } catch (Exception e) {
        ltLogger.info("Locator {} added to the exception log", invalidLocator.value());
      }
    });
  }

  private void selfSignedCertificate() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    String selfsignedText;
    selfsignedText = driverManager.openUrlAndGetLocatorText(SELF_SIGNED_URL, selfSignedPageHeading, 10);
    ltLogger.info("SelfSigned text: {}", selfsignedText);
    if (selfsignedText != null && !selfsignedText.contains("badssl.com")) {
      selfsignedText = driverManager.openUrlAndGetLocatorText(SELF_SIGNED_URL_2, selfSignedPageHeading, 10);
      ltLogger.info("SelfSigned 2nd website text: {}", selfsignedText);
    }
    if (selfsignedText != null && !selfsignedText.contains("badssl.com")) {
      selfsignedText = driverManager.openUrlAndGetLocatorText(SELF_SIGNED_URL_FALLBACK, selfSignedFallbackPageHeading,
        10);
      ltLogger.info("SelfSigned fallback website text: {}", selfsignedText);
    }
    softAssert.assertTrue(validSelfSignedValues.contains(selfsignedText),
      "Self-signed site not open. There might be a certificate issue or website didn't open.");
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void verifyTimezone() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    String browserTimeOffSet = driverManager.executeScriptAndFetchValue("return new Date().getTimezoneOffset();")
      .toString();
    String testTimeZone = apiHelper.constructTimeZoneFromTimeOffset(browserTimeOffSet);
    String expectedTimeZone = EnvSetup.GIVEN_TEST_CAPS_MAP.get().getOrDefault("timezone", "").toString();
    if (!expectedTimeZone.isEmpty())
      softAssert.assertTrue(testTimeZone.equals(expectedTimeZone),
        "Timezone is not set correctly. Current timezone is: " + testTimeZone + " expected: " + expectedTimeZone);
    else
      System.err.println("Timezone validation is skipped as it is not passed in the test caps.");
  }

  private void fillFormUsingKeyboard() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    driverManager.getURL(HEROKU_APP_LOGIN_PAGE);
    boolean loginPageStatus = driverManager.isDisplayed(herokuLoginPageHeading, 5);
    if (loginPageStatus) {
      driverManager.sendKeys(herokuLoginPageUsernameInput, "tomsmith");
      driverManager.sendKeys(herokuLoginPageUsernameInput, Keys.TAB);
      driverManager.sendKeys(herokuLoginPagePasswordInput, "SuperSecretPassword!");
      driverManager.sendKeys(herokuLoginPagePasswordInput, Keys.TAB);
      driverManager.sendKeys(herokuLoginPageLoginButton, Keys.ENTER);
      String afterLoginPageHeading = driverManager.getText(herokuAfterLoginPageHeading, 5);
      softAssert.assertTrue(afterLoginPageHeading.contains("You logged into a secure area"),
        "Heroku app web page keyboard log in failed, Expected: You logged into a secure area but Received: " + afterLoginPageHeading);
    } else {
      softAssert.fail("Unable to open Heroku Login Page");
    }
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void browserOSDetails() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    String actualBrowserName = EnvSetup.GIVEN_TEST_CAPS_MAP.get().getOrDefault("browserName", "").toString()
      .toLowerCase();
    String actualBrowserVersion = EnvSetup.GIVEN_TEST_CAPS_MAP.get().getOrDefault("version", "").toString().trim();

    Map<String, String> browserDetails = (Map<String, String>) driverManager.executeScriptAndFetchValue(
      jsForFetchBrowserDetails);

    if (browserDetails != null && !browserDetails.isEmpty()) {
      String browserName = browserDetails.get("name");
      String browserVersion = browserDetails.get("version");

      ltLogger.info("Browser name: {}", browserName);
      ltLogger.info("Browser version: {}", browserVersion);

      softAssert.assertEquals(browserName, actualBrowserName,
        String.format("Browser name doesn't match. Expected: %s, Actual: %s", actualBrowserName, browserName));

      softAssert.assertTrue(browserVersion.contains(actualBrowserVersion),
        String.format("Browser version doesn't match. Expected: %s, Actual: %s", actualBrowserVersion, browserVersion));
    } else {
      ltLogger.error("Failed to fetch browser details.");
    }

    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void verifyExtension() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    driverManager.switchToTab(0, 1);
    softAssert.assertTrue(driverManager.getCurrentURL().contains("chrome-extension"),
      "Extension not working, or might be tab not switched");
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void baseTest() {
  }

  private void testLocalUrlWithTunnel() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    int httpServerStatus = apiHelper.getStatusCode(LOCAL_URL, null, null, null, null);
    Assert.assertEquals(httpServerStatus, 200,
      "Please start http server on port 8000 to start verifying tunnel. Expected status code: 200, original status code: " + httpServerStatus);
    driverManager.getURL(LOCAL_URL);
    boolean localUrlStatus = driverManager.isDisplayed(localUrlHeading);
    softAssert.assertTrue(localUrlStatus, "Local Url Not Displayed");
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  public void startSessionWithSpecificCapabilities(String testCapability, String testActions) {
    createTestSession(testCapability);
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    String[] testActionsArray = testActions.split(",");
    if (IS_EXTENSION_TEST.get()) {
      waitForTime(20);
      driverManager.switchToTab(1, 0);
    }
    for (String testAction : testActionsArray) {
      runTestActions(testAction);
    }
    stopWatch.stop();
    EnvSetup.TEST_REPORT.get().put(TEST_EXECUTION_TIME, String.valueOf(stopWatch.getTime() / 1000.00));
    stopWatch.reset();
    stopWatch.start();
    try {
      driverManager.quit();
    } catch (Exception e) {
      e.printStackTrace();
    }
    stopWatch.stop();
    EnvSetup.TEST_REPORT.get().put(TEST_STOP_TIME, String.valueOf(stopWatch.getTime() / 1000.00));
    EnvSetup.TEST_REPORT.get().put("test_actions_failures", TEST_ERR_REPORT.get());
  }

  public void startTunnel() {
    int maxTunnelStartRetry = 2;
    while (maxTunnelStartRetry > 0) {
      tunnelManager = new TunnelManager();
      tunnelManager.startTunnel("");
      boolean tunnelInfoAPIServerStatus = tunnelManager.checkTunnelInfoAPIServerIsInitiated();
      boolean tunnelCLIStatus;
      try {
        tunnelCLIStatus = tunnelManager.getTunnelStatusFromAPIServer();
      } catch (Exception e) {
        ltLogger.error("Encountered error while checking Tunnel Status: {}", e.getMessage());
        tunnelCLIStatus = false;
      }
      ltLogger.info("Tunnel info API server status {}, Tunnel CLI status {}", tunnelInfoAPIServerStatus,
        tunnelCLIStatus);
      if (tunnelInfoAPIServerStatus && tunnelCLIStatus)
        break;
      maxTunnelStartRetry--;
    }
  }

  public void stopTunnel() {
    tunnelManager.stopTunnel();
  }
}