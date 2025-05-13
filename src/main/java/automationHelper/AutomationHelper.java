package automationHelper;

import TestManagers.CapabilityManager;
import TestManagers.DriverManager;
import TestManagers.TunnelManager;
import io.restassured.response.Response;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import utility.BaseClass;
import utility.CustomSoftAssert;
import utility.EnvSetup;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utility.EnvSetup.*;
import static utility.FrameworkConstants.*;
import static utility.UrlsAndLocators.*;

public class AutomationHelper extends BaseClass {
  private final Logger ltLogger = LogManager.getLogger(AutomationHelper.class);
  CapabilityManager capabilityManager = new CapabilityManager();
  DriverManager driverManager = new DriverManager(true);
  TunnelManager tunnelManager;
  AutomationAPIHelper apiHelper = new AutomationAPIHelper();
  TestArtefactsVerificationHelper artefactsHelper = new TestArtefactsVerificationHelper();

  private void createTestSession(String testCapability, String... givenCloudPlatformName) {
    StopWatch stopWatch = new StopWatch();
    String cloudPlatformName = (givenCloudPlatformName != null && givenCloudPlatformName.length > 0) ?
      givenCloudPlatformName[0].toLowerCase() :
      cloudPlatforms.LAMBDATEST.name().toLowerCase();

    TEST_REPORT.get().put(CLOUD_PLATFORM_NAME, cloudPlatformName);

    switch (cloudPlatformName) {
    case "saucelab":
      capabilityManager.buildTestCapabilityForSL(testCapability);
      break;
    case "browserstack":
      capabilityManager.buildTestCapabilityForBS(testCapability);
      break;
    case "lambdatest":
    default:
      capabilityManager.buildTestCapability(testCapability);
      break;
    }

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

    LTHooks.startStepContext(driverManager, actionName);
    try {
      switch (actionName) {
      case "local":
        testLocalUrlWithTunnel();
        break;
      case "throwError":
        throwNewError();
        break;
      case "basicAuthentication":
        basicAuthenticationIHA();
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
      case "herokuAppAllTests":
        runDifferentHerokuAppTest();
        break;
      case "allowHost":
        checkAllowHostFlagForTunnel();
        break;
      case "bypassHost":
        checkBypassHostFlagForTunnel();
        break;
      case "publicWebsitesResolutionCheckForForceLocal":
        verifyWebsiteResolutionBasedOnFlags("forceLocal");
        break;
      case "publicWebsitesResolutionCheckForAllowHosts":
        verifyWebsiteResolutionBasedOnFlags("allowHosts");
        break;
      case "publicWebsitesResolutionCheckForBypassHosts":
        verifyWebsiteResolutionBasedOnFlags("bypassHosts");
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
    if (!actionName.toLowerCase().contains("timeout")) {
      LTHooks.endStepContext(driverManager, actionName);
    }
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void basicAuthenticationIHA() {
    String browserName = TEST_CAPS_MAP.get().getOrDefault(BROWSER_NAME, "chrome").toString();
    if (browserName.equalsIgnoreCase("safari")) {
      ltLogger.info("Basic auth test is not valid in Safari");
      return;
    }
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    driverManager.getURL(BASIC_AUTH);
    String pageHeading = driverManager.getText(basicAuthHeading);
    softAssert.assertTrue(pageHeading.equals("Basic Auth"), "Basic Authentication Failed");
    EnvSetup.SOFT_ASSERT.set(softAssert);

  }

  private void basicAuthenticationUsingKeyboardEvents() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    driverManager.getUrlWithoutTimeoutException(BASIC_AUTH_URL_WITHOUT_AUTH_HEADERS);
    waitForTime(5);
    LTHooks.setClipboard(driverManager, "admin");
    LTHooks.performKeyboardEvent(driverManager, LAMBDA_KEYBOARD_PASTE);
    waitForTime(2);
    LTHooks.performKeyboardEvent(driverManager, LAMBDA_KEYBOARD_TAB);
    waitForTime(2);
    LTHooks.performKeyboardEvent(driverManager, LAMBDA_KEYBOARD_PASTE);
    waitForTime(2);
    LTHooks.performKeyboardEvent(driverManager, LAMBDA_KEYBOARD_ENTER);
    waitForTime(5);
    String pageHeading = driverManager.getText(basicAuthHeading);
    softAssert.assertTrue(pageHeading.equals("Basic Auth"), "Basic Authentication Failed using keyboard events.");
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void addConsoleLogs() {
    driverManager.getURL(GOOGLE_URL);
    consoleLogs.keySet().forEach(consoleLogLevel -> {
      String logMessage = consoleLogs.get(consoleLogLevel);
      driverManager.executeScript(String.format("console.%s('%s')", consoleLogLevel, logMessage));
      ArrayList<String> consoleLogsList = (ArrayList<String>) TEST_VERIFICATION_DATA.get()
        .getOrDefault(testVerificationDataKeys.CONSOLE_LOG, new ArrayList<>());
      consoleLogsList.add(logMessage);
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
    softAssert.assertTrue(validSelfSignedValues.contains(Objects.requireNonNull(selfsignedText).trim()),
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
    driverManager.switchToTab(0);
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

  private void runABTestIHA(CustomSoftAssert softAssert) {
    driverManager.getURL(INTERNET_HEROKU_APP_ABTEST);
    String abTestHeading = driverManager.getText(abtestHeadingIHA);
    softAssert.assertTrue(abTestHeading.equals("A/B Test Variation 1"), "A/B Test Variation failed.");
  }

  private void addOrRemoveElementIHA(CustomSoftAssert softAssert) {
    driverManager.getURL(INTERNET_HEROKU_APP_ADD_REMOVE_ELEMENT_URL);
    driverManager.click(addElementButtonIHA);
    softAssert.assertTrue(driverManager.isDisplayed(deleteElementButtonIHA, 5), "Add element test verification failed");
  }

  private void testBrokenImagesIHA(CustomSoftAssert softAssert) {
    driverManager.getURL(INTERNET_HEROKU_APP_BROKEN_IMAGES_URL);
    List<WebElement> elements = driverManager.findElements(brokenImagesIHA);
    softAssert.assertFalse(elements.isEmpty(), "Broken Images are not displayed");
  }

  private void checkBoxIHA(CustomSoftAssert softAssert) {
    driverManager.getURL(INTERNET_HEROKU_APP_CHECK_BOXES_URL);
    driverManager.click(checkboxesIHA);
    softAssert.assertTrue(driverManager.isSelected(checkboxesIHA, 5), "Checkboxes are not selected");
  }

  private void dynamicContentIHA(CustomSoftAssert softAssert) {
    driverManager.getURL(INTERNET_HEROKU_APP_DYNAMIC_CONTENT_URL);
    driverManager.click(dynamicContentClickIHA);
    String s1 = driverManager.getText(staticParagraphIHA);
    driverManager.refreshPage();
    String s2 = driverManager.getText(staticParagraphIHA);
    softAssert.assertTrue(s1.equals(s2),
      "Dynamic Content test failed as static messages are not same after page refresh");
  }

  private void dynamicControlsIHA(CustomSoftAssert softAssert) {
    driverManager.getURL(INTERNET_HEROKU_APP_DYNAMIC_CONTROLS_URL);
    driverManager.click(checkBoxSwapButtonIHA);
    softAssert.assertTrue(driverManager.getText(messageInDynamicControlsPageIHA, 10).equals("It's gone!"),
      "First swap failed.");
    driverManager.click(checkBoxSwapButtonIHA);
    softAssert.assertTrue(driverManager.getText(messageInDynamicControlsPageIHA, 10).equals("It's back!"),
      "Second swap failed.");
    driverManager.click(textBoxEnableButtonIHA);
    softAssert.assertTrue(driverManager.getText(messageInDynamicControlsPageIHA, 10).equals("It's enabled!"),
      "Text box enable failed.");
    driverManager.click(textBoxEnableButtonIHA);
    softAssert.assertTrue(driverManager.getText(messageInDynamicControlsPageIHA, 10).equals("It's disabled!"),
      "Text box disable failed.");
  }

  private void loginFormFillUpIHA(CustomSoftAssert softAssert) {
    driverManager.getURL(INTERNET_HEROKU_APP_LOGIN_PAGE_URL);
    driverManager.sendKeys(userNameInputTHA, "tomsmith");
    driverManager.sendKeys(passwordInputTHA, "SuperSecretPassword!");
    driverManager.click(loginButtonTHA);
    softAssert.assertTrue(driverManager.getText(loginSuccessHeaderTHA).contains("Welcome to the Secure Area"),
      "Form login is not successful");
    if (driverManager.isSelected(loginSuccessHeaderTHA)) {
      driverManager.click(logoutButtonTHA);
      softAssert.assertTrue(driverManager.getText(loginPageHeadingTHA).contains("Login Page"), "Form logout failed.");
    }
  }

  private void runDifferentHerokuAppTest() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    //    runABTestIHA(softAssert);
    addOrRemoveElementIHA(softAssert);
    basicAuthenticationIHA();
    testBrokenImagesIHA(softAssert);
    checkBoxIHA(softAssert);
    dynamicContentIHA(softAssert);
    dynamicControlsIHA(softAssert);
    loginFormFillUpIHA(softAssert);
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private String multiplyTestActionsIfNeeded(String testActions) {
    StringBuilder updatedTestActions = new StringBuilder(testActions);
    String actionsRepeatCountString = System.getProperty(REPEAT_TEST_ACTIONS);
    ltLogger.info("REPEAT_TEST_ACTIONS value is: {}", actionsRepeatCountString);
    if (actionsRepeatCountString != null) {
      try {
        int actionsRepeatCount = Integer.parseInt(actionsRepeatCountString);
        for (int i = 0; i < actionsRepeatCount; i++) {
          updatedTestActions.append(",").append(testActions);
        }
      } catch (Exception e) {
        ltLogger.warn("Unable to parse repeat actions count string: {}", actionsRepeatCountString);
      }
      ltLogger.info("Updated test actions string: {}", updatedTestActions);
      return updatedTestActions.toString();
    }
    return testActions;
  }

  public void startSessionWithSpecificCapabilities(boolean quitTestDriver, String testCapability, String testActions,
    String... cloudPlatformName) {
    String startTime = getCurrentTimeIST();
    createTestSession(testCapability, cloudPlatformName);

    /// Check if user wants to repeat the same test actions multiple times
    testActions = multiplyTestActionsIfNeeded(testActions);

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

  public void startTunnel(String... givenArgs) {
    String args = (givenArgs != null && givenArgs.length > 0) ? givenArgs[0] : "";
    int maxTunnelStartRetry = 2;
    while (maxTunnelStartRetry > 0) {
      tunnelManager = new TunnelManager();
      tunnelManager.startTunnel(args);
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

  private boolean isWebdriverModeEnabled(String session_id, Map<String, Object> testCaps) {
    final String webDriverModeFlagKey = "ml_webdriver_mode";
    boolean isWebdriverModeEnabled = true;
    String isWebdriverModeFlagEnabled = apiHelper.getFeatureFlagValueOfSpecificSession(session_id,
      webDriverModeFlagKey);

    String webDriverModeCapsValue = testCaps.getOrDefault(WEBDRIVER_MODE, isWebdriverModeFlagEnabled).toString();
    String seCdpCapsValue = testCaps.getOrDefault(SELENIUM_CDP, "false").toString();
    String seTelemetryCapsValue = testCaps.getOrDefault(SELENIUM_TELEMETRY_LOGS, "false").toString();

    ltLogger.info(
      "WebDriver mode constraint values: \nwebDriverModeFlag: {}\nwebDriverModeCapsValue: {}\nseCdpCapsValue: {}\nseTelemetryCapsValue: {}",
      isWebdriverModeFlagEnabled, webDriverModeCapsValue, seCdpCapsValue, seTelemetryCapsValue);
    ltLogger.info("Test Capability used: {}", testCaps);

    if (webDriverModeCapsValue.equalsIgnoreCase("false") || seCdpCapsValue.equals(
      "true") || seTelemetryCapsValue.equals("true"))
      isWebdriverModeEnabled = false;
    ltLogger.info("isWebdriverModeEnabled status: {}", isWebdriverModeEnabled);
    return isWebdriverModeEnabled;
  }

  public void verifyLogs(String logs) {
    // Wait for logs to be uploaded
    waitForSomeTimeAfterTestCompletionForLogsToBeUploaded(EnvSetup.TEST_REPORT.get().get(TEST_END_TIMESTAMP).toString(),
      120);

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

    if (!validLogsToCheck) {
      System.err.println(
        logs + " logs verification is skipped as required caps are not used. Required caps: " + testArtefactsToCapsMap.getOrDefault(
          logs, Collections.emptyMap()));
      return;
    }

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

  private void checkLocalWebSitesAreReachable(Map<String, String> urlsAndVerificationMessage) {
    for (String url : urlsAndVerificationMessage.keySet()) {
      int httpServerStatus = apiHelper.getStatusCode(url, null, null, null, null);
      Assert.assertEquals(httpServerStatus, 200,
        String.format("%s. Expected status code: 200, original status code: %d", urlsAndVerificationMessage.get(url),
          httpServerStatus));
    }
  }

  private String[] getCurrentIPAndLocationFromUrlInTestSession() {
    driverManager.getURL(IP_INFO_IO_URL);
    String ip = driverManager.getText(ipInfoIOIP, 5);
    String location = driverManager.getText(ipInfoIOLocation, 5);
    ltLogger.info("Current IP: {}, Current location: {}", ip, location);
    return new String[] { ip, location };
  }

  private void checkPublicWebsitesAreResolvedInExpectedLocation(String expectedLocation, String tunnelFlagName) {
    /// Expected expectedLocation values tunnelClient, tunnelServer, dc
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    String localMachineIp = apiHelper.getCurrentIPFromAPI();
    String[] ipAndLocation = getCurrentIPAndLocationFromUrlInTestSession();
    String locationWherePublicWebsiteResolved;
    if (ipAndLocation[0].equals(localMachineIp)) {
      locationWherePublicWebsiteResolved = "tunnelClient";
    } else if (tunnelServerIPs.contains(ipAndLocation[0])) {
      locationWherePublicWebsiteResolved = "tunnelServer";
    } else {
      locationWherePublicWebsiteResolved = "dc";
    }
    softAssert.assertTrue(locationWherePublicWebsiteResolved.equals(expectedLocation), String.format(
      "Public websites are not resolved in expected location for tunnel flag %s. Expected: %s, Actual: %s, Fetched IP: %s, Fetched location: %s",
      tunnelFlagName, expectedLocation, locationWherePublicWebsiteResolved, ipAndLocation[0], ipAndLocation[1]));
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  /**
   * For forceLocal flag all the websites should be resolved in tunnel client
   * For bypassHosts=*lambda* flag all the websites should be resolved in tunnel client except domains that match *lambda* that should be resolved in tunnel server or dc based on ml_resolve_tunnel_website_in_dc flag
   * For allowHosts=*lambda* flag all the websites should be resolved in tunnel server or dc based on ml_resolve_tunnel_website_in_dc flag except domains that match *lambda*
   */
  private void verifyWebsiteResolutionBasedOnFlags(String tunnelFlagName) {
    final String flagName = "ml_resolve_tunnel_website_in_dc";
    String flagValue = apiHelper.getFeatureFlagValueOfSpecificSession(EnvSetup.TEST_SESSION_ID.get(), flagName);
    switch (tunnelFlagName) {
    case "forceLocal", "bypassHosts" ->
      checkPublicWebsitesAreResolvedInExpectedLocation("tunnelClient", tunnelFlagName);
    case "allowHosts" -> {
      if (flagValue.equalsIgnoreCase("true")) {
        checkPublicWebsitesAreResolvedInExpectedLocation("dc", tunnelFlagName);
      } else {
        checkPublicWebsitesAreResolvedInExpectedLocation("tunnelServer", tunnelFlagName);
      }
    }
    default -> throw new IllegalStateException("Unexpected tunnel flag name passed: " + tunnelFlagName);
    }
  }

  private void checkAllowHostFlagForTunnel() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    final Map<String, String> expectedLocalUrls = Map.of(LOCAL_URL,
      "Please start http server on port 8000 to start verifying tunnel.", LOCAL_LAMBDA_URL,
      "Please update etc/hosts with value `127.0.0.1       locallambda.com` and retry");
    checkLocalWebSitesAreReachable(expectedLocalUrls);
    driverManager.getURL(LOCAL_LAMBDA_URL);
    softAssert.assertTrue(driverManager.isDisplayed(localUrlHeading, 5),
      "allowHosts flag is not working. " + LOCAL_LAMBDA_URL + " should be resolved in tunnel client as allowHosts='*lambda*' flag is used, but unable to open it.");
    driverManager.getURL(LOCAL_URL);
    softAssert.assertFalse(driverManager.isDisplayed(localUrlHeading, 5),
      "allowHosts flag is not working. " + LOCAL_URL + " should be resolved in the DC or Tunnel Server not in Tunnel Client");
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void checkBypassHostFlagForTunnel() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    final Map<String, String> expectedLocalUrls = Map.of(LOCAL_URL,
      "Please start http server on port 8000 to start verifying tunnel.", LOCAL_LAMBDA_URL,
      "Please update etc/hosts with value `127.0.0.1       locallambda.com` and retry");
    checkLocalWebSitesAreReachable(expectedLocalUrls);
    driverManager.getURL(LOCAL_LAMBDA_URL);
    softAssert.assertFalse(driverManager.isDisplayed(localUrlHeading, 5),
      "bypassHosts flag is not working. " + LOCAL_LAMBDA_URL + " should be resolved in tunnel server or DC not in Tunnel Client as bypassHosts='*lambda*' flag is used, and it shouldn't be opened.");
    driverManager.getURL(LOCAL_URL);
    softAssert.assertTrue(driverManager.isDisplayed(localUrlHeading, 5),
      "bypassHosts flag is not working. " + LOCAL_URL + " should be resolved in tunnel client as bypassHosts='*lambda*' flag is used, but unable to open it.");
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }
}