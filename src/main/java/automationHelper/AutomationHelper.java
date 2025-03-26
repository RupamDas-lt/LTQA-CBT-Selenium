package automationHelper;

import TestManagers.CapabilityManager;
import TestManagers.DriverManager;
import TestManagers.TunnelManager;
import io.restassured.response.Response;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Keys;
import org.testng.Assert;
import utility.BaseClass;
import utility.CustomSoftAssert;
import utility.EnvSetup;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utility.EnvSetup.*;
import static utility.FrameworkConstants.*;
import static utility.UrlsAndLocators.*;

public class AutomationHelper extends BaseClass {
  private final Logger ltLogger = LogManager.getLogger(AutomationHelper.class);
  CapabilityManager capabilityManager = new CapabilityManager();
  DriverManager driverManager = new DriverManager();
  TunnelManager tunnelManager;
  AutomationAPIHelper apiHelper = new AutomationAPIHelper();
  TestArtefactsVerificationHelper artefactsHelper = new TestArtefactsVerificationHelper();

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

  private boolean checkIfValidTestAction(String actionName) {
    ltLogger.info("Checking if action {} is valid.", actionName);
    boolean runTestAction = false;
    if (testActionsToCapsMap.containsKey(actionName)) {
      Set<String> requiredCaps = testActionsToCapsMap.get(actionName);
      ltLogger.info("Test Action: {}, requires caps: {}", actionName, requiredCaps);
      for (String requiredCap : requiredCaps) {
        if (TEST_CAPS_MAP.get().containsKey(requiredCap)) {
          runTestAction = true;
          break;
        }
      }
    } else
      runTestAction = true;
    return runTestAction;
  }

  private void runTestActions(String actionName) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();

    boolean runTestAction = checkIfValidTestAction(actionName);

    if (!runTestAction) {
      ltLogger.info("Skipping test action: {}", actionName);
      return;
    }

    ltLogger.info("Executing test action: {}", actionName);

    startTestContext(actionName);
    try {
      switch (actionName) {
      case "local":
        testLocalUrlWithTunnel();
        break;
      case "throwError":
        throwNewError();
        break;
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
      case "uploadFile":
        uploadFile();
        break;
      case "geolocation":
        geolocation();
        break;
      case "loginCacheCleaned":
        loginCacheCleanedCheckUsingLTLoginPage();
        break;
      case "idleTimeout":
        waitForTestToGetIdleTimeout();
        break;
      case "noAction":
        break;
      case "networkLog":
      default:
        baseTest();
        break;
      }
    } catch (Exception e) {
      EnvSetup.TEST_REPORT.get().put("test_actions_failures", Map.of(actionName, e.getMessage()));
      throw new RuntimeException("Test action " + actionName + " failed", e);
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
      ArrayList<String> consoleLogsList = (ArrayList<String>) TEST_VERIFICATION_DATA.get()
        .getOrDefault(testVerificationDataKeys.CONSOLE_LOG, new ArrayList<>());
      consoleLogsList.add(consoleLog);
      TEST_VERIFICATION_DATA.get().put(testVerificationDataKeys.CONSOLE_LOG, consoleLogsList);
    });
  }

  private void addExceptionLogs() {
    Queue<String> exceptionLogsLocators = new LinkedList<>();
    locatorsForExceptionLogs.forEach(invalidLocator -> {
      try {
        driverManager.waitForElementToBeVisible(invalidLocator, 5);
      } catch (Exception e) {
        String exceptionLogsLocator = invalidLocator.value();
        ltLogger.info("Locator {} added to the exception log", exceptionLogsLocators);
        exceptionLogsLocators.add(exceptionLogsLocator);
      }
    });
    TEST_VERIFICATION_DATA.get().put(testVerificationDataKeys.EXCEPTION_LOG, exceptionLogsLocators);
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
    String expectedTimeZone = EnvSetup.TEST_CAPS_MAP.get().getOrDefault("timezone", "").toString();
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

  private String[] getBrowserDetailsFromWeb() {
    driverManager.getURL(BROWSER_DETAILS_URL);
    String text = driverManager.getText(browserDetailsText, 5);
    String regex = "(\\w+)\\s(\\d+)";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(text);
    if (matcher.find()) {
      return new String[] { matcher.group(1), matcher.group(2) };
    }
    return null;
  }

  private String[] getBrowserDetailsFromJS() {
    String javaScript = "return navigator.userAgent;";
    String userAgent = (String) driverManager.executeScriptAndFetchValue(javaScript);
    String browserName = getBrowserName(userAgent);
    String browserVersion = getBrowserVersion(userAgent, browserName);
    return new String[] { browserName, browserVersion };
  }

  private String getBrowserName(String userAgent) {
    if (userAgent.contains("OPR")) {
      return "Opera";
    } else if (userAgent.contains("Edg")) {
      return "MicrosoftEdge";
    } else if (userAgent.contains("Firefox")) {
      return "Firefox";
    } else if (userAgent.contains("Chrome")) {
      return "Chrome";
    } else if (userAgent.contains("Safari")) {
      return "Safari";
    } else {
      return "ie";
    }
  }

  private String getBrowserVersion(String userAgent, String browserName) {
    String[] dataArray = userAgent.split(" ");
    String browserVersion = "";
    switch (browserName) {
    case "Opera":
    case "MicrosoftEdge":
    case "Firefox":
      browserVersion = dataArray[dataArray.length - 1].split("/")[1];
      break;
    case "Chrome":
    case "Safari":
      browserVersion = dataArray[dataArray.length - 2].split("/")[1];
      break;
    case "Internet Explorer":
      if (dataArray[dataArray.length - 3].contains(":")) {
        browserVersion = dataArray[dataArray.length - 3].split(":")[1].replace(")", "");
      } else if (dataArray[dataArray.length - 3].contains("/")) {
        double num = Double.parseDouble(dataArray[dataArray.length - 3].split("/")[1].replace(";", ""));
        num = num + 4;
        browserVersion = Double.toString(num);
      }
      break;
    default:
      browserVersion = "Unknown";
    }

    return browserVersion.trim();
  }

  private void browserOSDetails() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    Map<String, Object> testCapsMap = EnvSetup.TEST_CAPS_MAP.get();

    String actualBrowserName = testCapsMap.getOrDefault("browserName", "").toString().toLowerCase();
    String actualBrowserVersion = testCapsMap.getOrDefault("version", "").toString().trim();

    // Handle beta/dev/latest versions
    if (actualBrowserVersion.matches(".*(dev|beta|latest).*")) {
      actualBrowserVersion = apiHelper.getBrowserVersionBasedOnKeyword(actualBrowserName, actualBrowserVersion,
        testCapsMap.get("platform").toString()).split("\\.")[0];
      ltLogger.info("Actual browser version: {}", actualBrowserVersion);
    }
    String seleniumVersion = testCapsMap.getOrDefault(SELENIUM_VERSION, "default").toString();
    ltLogger.info("Selenium version: {}", seleniumVersion);

    // Try fetching browser details from the web
    String[] browserDetails = getBrowserDetailsFromWeb();
    try {
      assert browserDetails != null;
      ltLogger.info("Browser details fetched from osBrowserDetails page: {}", Arrays.asList(browserDetails));
      if (validateBrowserDetails(browserDetails, actualBrowserName, actualBrowserVersion)) {
        return;
      }
    } catch (Exception e) {
      ltLogger.error("Error while verifying browser details from osBrowserDetails page", e);
    }

    if (!seleniumVersion.equals("latest") && !seleniumVersion.equals("default")) {
      ltLogger.warn("Verification skipped as selenium version is not in {default, latest}. Selenium version: {}",
        seleniumVersion);
      return;
    }

    // Fallback to fetching browser details using JavaScript
    ltLogger.warn("Unable to fetch browser details from website. Trying to fetch browser details using JS");
    browserDetails = getBrowserDetailsFromJS();
    String browserName = browserDetails[0].toLowerCase().trim();
    String browserVersion = browserDetails[1].trim();
    ltLogger.info("Browser name: {}", browserName);
    ltLogger.info("Browser version: {}", browserVersion);

    softAssert.assertTrue(browserName.contains(actualBrowserName) || actualBrowserName.contains(browserName),
      String.format("Browser name doesn't match. Expected: %s, Actual: %s", actualBrowserName, browserName));

    softAssert.assertTrue(browserVersion.contains(actualBrowserVersion),
      String.format("Browser version doesn't match. Expected: %s, Actual: %s", actualBrowserVersion, browserVersion));

    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private boolean validateBrowserDetails(String[] browserDetails, String expectedBrowserName,
    String expectedBrowserVersion) {
    expectedBrowserName = expectedBrowserName.equals("ie") ? "internet explorer" : expectedBrowserName;
    ltLogger.info("Actual details: {}, Expected name: {}, Expected version: {}", Arrays.asList(browserDetails),
      expectedBrowserName, expectedBrowserVersion);
    if (browserDetails.length < 2 || browserDetails[0] == null || browserDetails[1] == null) {
      ltLogger.warn("Invalid browser details provided: {}", Arrays.toString(browserDetails));
      return false;
    }
    String actualBrowserName = browserDetails[0].trim().toLowerCase();
    String actualBrowserVersion = browserDetails[1].trim();
    String normalizedExpectedBrowserName = expectedBrowserName.trim().toLowerCase();
    String normalizedExpectedBrowserVersion = expectedBrowserVersion.trim();

    boolean isBrowserNameValid = actualBrowserName.contains(
      normalizedExpectedBrowserName) || normalizedExpectedBrowserName.contains(actualBrowserName);
    boolean isBrowserVersionValid = actualBrowserVersion.equals(normalizedExpectedBrowserVersion);
    return isBrowserNameValid && isBrowserVersionValid;
  }

  private void verifyExtension() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    driverManager.switchToTab(1);
    softAssert.assertTrue(driverManager.getCurrentURL().contains("chrome-extension"),
      "Extension not working, or might be tab not switched");
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void baseTest() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    String sampleText = "Let's add it to list";
    driverManager.getURL(TODO_APP_URL);
    driverManager.click(todoListItem1);
    driverManager.click(todoListItem2);
    driverManager.sendKeys(todoInput, sampleText);
    driverManager.click(todoAddButton);
    driverManager.executeScript("document.getElementById(\"" + todoAddButton.value() + "\").click();");
    String actualText = driverManager.getText(todoNewEnteredText, 5);
    softAssert.assertTrue(actualText.contains(sampleText),
      "Entered text doesn't match. Expected: " + sampleText + " Actual: " + actualText);
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void uploadFile() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    driverManager.setLocalFileDetector();
    driverManager.getURL(FILE_UPLOAD_URL);
    driverManager.sendKeys(chooseFileButton, SAMPLE_TXT_FILE_PATH);
    driverManager.click(uploadFileButton);
    softAssert.assertTrue(driverManager.isDisplayed(uploadedFileHeading, 5), "Unable to upload file");
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void geolocation() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    driverManager.getURL(GEOLOCATION_VERIFICATION_URL);
    int retryCount = 0;
    while (retryCount < 5) {
      if (driverManager.isDisplayed(countryName, 10))
        break;
      retryCount++;
    }
    String actualCountryName = driverManager.getText(countryName);
    String expectedCountryName = TEST_VERIFICATION_DATA.get().get(testVerificationDataKeys.GEO_LOCATION).toString();
    softAssert.assertTrue(
      expectedCountryName.contains(actualCountryName) || actualCountryName.contains(expectedCountryName),
      "GeoLocation didn't match. Expected: " + expectedCountryName + " Actual: " + actualCountryName);
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void loginCacheCleanedCheckUsingLTLoginPage() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    driverManager.getURL(LT_LOGIN_URL);
    softAssert.assertTrue(driverManager.isDisplayed(ltLoginPageEmailInput, 5),
      "LT login page is not displayed, cache is not cleared in the machine.");
    if (!driverManager.isDisplayed(ltLoginPageEmailInput)) {
      driverManager.sendKeys(ltLoginPageEmailInput, USER_EMAIL);
      driverManager.sendKeys(ltLoginPagePasswordInput, USER_PASS);
      driverManager.click(ltLoginPageSubmitButton);
      softAssert.assertTrue(driverManager.isDisplayed(ltLoginSuccessVerification, 20),
        "Failed to login to LT website, cache verification will not be valid for next sessions.");
    }
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void throwNewError() {
    throw new RuntimeException("Something went wrong! This is a trial error :)");
  }

  private void testLocalUrlWithTunnel() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    int httpServerStatus = apiHelper.getStatusCode(LOCAL_URL, null, null, null, null);
    Assert.assertEquals(httpServerStatus, 200,
      "Please start http server on port 8000 to start verifying tunnel. Expected status code: 200, original status code: " + httpServerStatus);
    driverManager.getURL(LOCAL_URL);
    boolean localUrlStatus = driverManager.isDisplayed(localUrlHeading, 10);
    softAssert.assertTrue(localUrlStatus, "Local Url Not Displayed");
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  public void startSessionWithSpecificCapabilities(boolean quitTestDriver, String testCapability, String testActions) {
    String startTime = getCurrentTimeIST();
    createTestSession(testCapability);
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    String[] testActionsArray = testActions.split(",");
    if (IS_EXTENSION_TEST.get()) {
      waitForTime(20);
      driverManager.switchToTab(0);
    }
    for (String testAction : testActionsArray) {
      runTestActions(testAction);
    }
    stopWatch.stop();
    EnvSetup.TEST_REPORT.get().put(TEST_EXECUTION_TIME, String.valueOf(stopWatch.getTime() / 1000.00));
    stopWatch.reset();
    if (quitTestDriver) {
      stopWatch.start();
      try {
        driverManager.quit();
      } catch (Exception e) {
        e.printStackTrace();
      }
      stopWatch.stop();
      String stopTime = getCurrentTimeIST();
      EnvSetup.TEST_REPORT.get().put(TEST_STOP_TIME, String.valueOf(stopWatch.getTime() / 1000.00));
      EnvSetup.TEST_REPORT.get().put(TEST_END_TIMESTAMP, stopTime);
    }
    EnvSetup.TEST_REPORT.get().put(TEST_START_TIMESTAMP, startTime);
    EnvSetup.TEST_REPORT.get().put("test_verification_data", TEST_VERIFICATION_DATA.get());
    ltLogger.info("Test verification data: {}", TEST_VERIFICATION_DATA.get());
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

  public void uploadSampleTerminalLogs() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    String status = apiHelper.uploadTerminalLogs(EnvSetup.TEST_SESSION_ID.get());
    softAssert.assertTrue(status.equals("success"), "Failed to upload terminal logs");
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void waitForSomeTimeAfterTestCompletionForLogsToBeUploaded(int seconds) {
    String currentTime = getCurrentTimeIST();
    ltLogger.info("Time while checking for logs: {}", currentTime);
    String testEndTime = EnvSetup.TEST_REPORT.get().get(TEST_END_TIMESTAMP).toString();
    ltLogger.info("Time of test completion: {}", testEndTime);
    Duration durationTillTestEnded = getTimeDifference(testEndTime, currentTime, IST_TimeZone);
    if (durationTillTestEnded.getSeconds() <= seconds) {
      int requiredTime = (int) (seconds - Math.floor(durationTillTestEnded.getSeconds()));
      ltLogger.info("Waiting for {} before verifying the logs.", requiredTime);
      waitForTime(requiredTime);
    }
  }

  private boolean isWebdriverModeEnabled(String session_id, Map<String, Object> testCaps) {
    final String webDriverModeFlagKey = "ml_webdriver_mode";
    boolean isWebdriverModeEnabled = true;
    String isWebdriverModeFlagEnabled = apiHelper.getFeatureFlagValueOfSpecificSession(session_id,
      webDriverModeFlagKey);
    if (testCaps.getOrDefault(WEBDRIVER_MODE, isWebdriverModeFlagEnabled).toString()
      .equalsIgnoreCase("false") || testCaps.getOrDefault(SELENIUM_CDP, "false").toString()
      .equals("true") || testCaps.getOrDefault(SELENIUM_TELEMETRY_LOGS, "false").toString().equals("true"))
      isWebdriverModeEnabled = false;
    return isWebdriverModeEnabled;
  }

  public void verifyLogs(String logs) {
    // Wait for logs to be uploaded
    waitForSomeTimeAfterTestCompletionForLogsToBeUploaded(120);

    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    Map<String, Object> testCaps = TEST_CAPS_MAP.get();
    String testId = EnvSetup.TEST_SESSION_ID.get();

    // Check if logs verification is required based on capabilities
    boolean validLogsToCheck = areLogsVerificationRequired(logs, testCaps);

    // Handle special case for system logs
    if (logs.equals("selenium")) {
      boolean isWebDriverTest = isWebdriverModeEnabled(testId, testCaps);
      logs = isWebDriverTest ? "webDriver" : "selenium";
      validLogsToCheck = true; // Always verify selenium/webDriver logs
    }

    //    softAssert.assertTrue(validLogsToCheck,
    //      logs + " logs verification is skipped as required caps are not used. Required caps: " + testArtefactsToCapsMap.getOrDefault(
    //        logs, Collections.emptyMap()));

    if (!validLogsToCheck)
      System.err.println(
        logs + " logs verification is skipped as required caps are not used. Required caps: " + testArtefactsToCapsMap.getOrDefault(
          logs, Collections.emptyMap()));

    verifyLogsByType(logs, testId, softAssert);

    EnvSetup.SOFT_ASSERT.set(softAssert);

  }

  private boolean areLogsVerificationRequired(String logs, Map<String, Object> testCaps) {
    Map<String, Object> requiredCaps = testArtefactsToCapsMap.getOrDefault(logs, Collections.emptyMap());
    if (requiredCaps.isEmpty()) {
      return true;
    }

    return requiredCaps.entrySet().stream().allMatch(entry -> {
      String cap = entry.getKey();
      Object requiredValue = entry.getValue();
      Object actualValue = testCaps.get(cap);

      if (actualValue == null) {
        return false;
      }

      if (requiredValue instanceof Boolean || requiredValue instanceof String) {
        ltLogger.info("Checking if {} cap value is {} for logs {}", cap, requiredValue.toString(), logs);
        return actualValue.toString().equalsIgnoreCase(requiredValue.toString());
      } else if (requiredValue instanceof List) {
        ltLogger.info("Checking if {} cap value is among {} for logs {}", cap, requiredValue, logs);
        try {
          List<String> allowedValues = (List<String>) requiredValue;
          return allowedValues.contains(actualValue.toString().toLowerCase());
        } catch (ClassCastException e) {
          ltLogger.error("Invalid type in required values list for cap {}", cap);
          return false;
        }
      }
      return false;
    });
  }

  private void verifyLogsByType(String logs, String testId, CustomSoftAssert softAssert) {

    Map<String, Runnable> logVerificationMap = new HashMap<>();
    logVerificationMap.put("webDriver", () -> artefactsHelper.verifySystemLogs(logs, testId));
    logVerificationMap.put("selenium", () -> artefactsHelper.verifySystemLogs(logs, testId));
    logVerificationMap.put("command", () -> artefactsHelper.verifyCommandLogs(testId));
    logVerificationMap.put("console", () -> artefactsHelper.verifyConsoleLogs(testId));
    logVerificationMap.put("terminal", () -> artefactsHelper.verifyTerminalLogs(testId));
    logVerificationMap.put("network", () -> artefactsHelper.verifyNetworkLogs(testId));
    logVerificationMap.put("full.har", () -> artefactsHelper.verifyNetworkFullHarLogs(testId));
    logVerificationMap.put("exception", () -> artefactsHelper.exceptionCommandLogs(testId));
    logVerificationMap.put("video", () -> artefactsHelper.verifyTestVideo(testId));
    logVerificationMap.put("performance report", () -> artefactsHelper.verifyPerformanceReport(testId));

    Runnable verificationMethod = logVerificationMap.getOrDefault(logs,
      () -> softAssert.fail("Unable to find any matching logs with name: " + logs));
    verificationMethod.run();
  }

  public void stopRunningTest() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    String sessionId = EnvSetup.TEST_SESSION_ID.get();
    String statusBeforeStoppingTheTest = apiHelper.getStatusOfSessionViaAPI(sessionId);
    Assert.assertTrue(RUNNING.equalsIgnoreCase(statusBeforeStoppingTheTest),
      "Unable to initiate stop build as the build is not in Running state. Current state: " + statusBeforeStoppingTheTest);
    Response response = apiHelper.stopTestViaApi(sessionId);
    String status = response.jsonPath().get("status").toString();
    String message = response.jsonPath().get("message").toString();
    softAssert.assertTrue(status.equalsIgnoreCase("success"),
      "Unable to stop session with test stop api. Status: " + status + " Message: " + message);
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  public void verifyTestStatusViaAPI(String expectedStatus, int... customRetryCounts) {
    final String sessionId = EnvSetup.TEST_SESSION_ID.get();
    final int maxRetries = customRetryCounts.length > 0 ? customRetryCounts[0] : 2;
    String currentStatus = "";

    for (int attempt = 1; attempt <= maxRetries; attempt++) {
      currentStatus = apiHelper.getStatusOfSessionViaAPI(sessionId);

      if (expectedStatus.equalsIgnoreCase(currentStatus)) {
        handleSuccessfulSessionStatusMatch(expectedStatus, attempt);
        return;
      }

      ltLogger.info("Attempt {}/{}: Status mismatch. Expected: {}, Actual: {}", attempt, maxRetries, expectedStatus,
        currentStatus);

      if (attempt < maxRetries) {
        waitForTime(5);
      }
    }

    throw new AssertionError(
      String.format("Test status verification failed after %d attempts. Expected: %s, Actual: %s", maxRetries,
        expectedStatus, currentStatus));
  }

  private void handleSuccessfulSessionStatusMatch(String status, int attempt) {
    if (status.equalsIgnoreCase(STOPPED) || status.equalsIgnoreCase(IDLE_TIMEOUT_STATUS)) {
      EnvSetup.TEST_REPORT.get().put(TEST_END_TIMESTAMP, getCurrentTimeIST());
    }
    ltLogger.info("Status matched on attempt-{}: {}", attempt, status);
  }

  public void stopRunningBuild() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    String sessionId = EnvSetup.TEST_SESSION_ID.get();
    String buildId = apiHelper.getBuildIdFromSessionId(sessionId);
    String buildStatus = apiHelper.getStatusOfBuildViaAPI(buildId);
    Assert.assertTrue(RUNNING.equalsIgnoreCase(buildStatus),
      "Unable to initiate build stop as the build is not in Running state. Current state: " + buildStatus);
    Response response = apiHelper.stopBuildViaApi(buildId);
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  public void verifyBuildStatusViaAPI(String buildStatus_ind) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    String buildId = apiHelper.getBuildIdFromSessionId(EnvSetup.TEST_SESSION_ID.get());
    String buildStatus = apiHelper.getStatusOfBuildViaAPI(buildId);
    softAssert.assertTrue(STOPPED.equalsIgnoreCase(buildStatus),
      "Build status doesn't match. Expected: " + buildStatus + ", Actual: " + buildStatus_ind);
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  public void verifyTunnelStatusViaAPI(String tunnelName, String expectedTunnelStatus) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    Map<String, String> runningTunnelNameToTunnelIDMap = apiHelper.getAllRunningTunnels();
    switch (expectedTunnelStatus) {
    case RUNNING -> softAssert.assertTrue(runningTunnelNameToTunnelIDMap.containsKey(tunnelName),
      "There is no running tunnel with name: " + tunnelName);
    case STOPPED -> softAssert.assertFalse(runningTunnelNameToTunnelIDMap.containsKey(tunnelName),
      "The tunnel with name: " + tunnelName + " is in running state. But expected state is: " + expectedTunnelStatus);
    default -> throw new IllegalStateException("Unexpected value: " + expectedTunnelStatus);
    }
    String currentTunnelId = runningTunnelNameToTunnelIDMap.get(tunnelName);
    TEST_TUNNEL_ID.set(currentTunnelId);
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  public void stopRunningTunnelViaAPI(String tunnelID) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    String status = apiHelper.stopTunnel(tunnelID);
    softAssert.assertTrue(status.equalsIgnoreCase("success"),
      "Stop tunnel failed with tunnel id: " + tunnelID + ", Status: " + status);
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  public void waitForTestToGetIdleTimeout() {
    int timeForIdleTimeout = Integer.parseInt(TEST_CAPS_MAP.get().getOrDefault(IDLE_TIMEOUT, "120").toString());
    waitForTime(timeForIdleTimeout);
    ltLogger.info("Test should be idle timeout.");
  }
}