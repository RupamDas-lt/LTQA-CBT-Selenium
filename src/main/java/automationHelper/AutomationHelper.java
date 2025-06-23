package automationHelper;

import TestManagers.CapabilityManager;
import TestManagers.DriverManager;
import TestManagers.TunnelManager;
import factory.Locator;
import factory.LocatorTypes;
import io.restassured.response.Response;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import utility.BaseClass;
import utility.CustomAssert;
import utility.CustomSoftAssert;
import utility.EnvSetup;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static factory.SoftAssertionMessages.*;
import static utility.EnvSetup.*;
import static utility.FrameworkConstants.*;
import static utility.FrameworkConstants.testVerificationDataKeys.*;
import static utility.UrlsAndLocators.*;

public class AutomationHelper extends BaseClass {
  // path for the network blocking script
  private static final String NETWORK_SCRIPT_PATH = "./Utility/Bash/NetworkBlockingUtils.sh";

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
      case "localWithCustomDomain":
        testLocalUrlWithCustomDomainForTunnel();
        break;
      case "throwError":
        throwNewError();
        break;
      case "tryoutDifferentAssertionErrors":
        testActionToTryoutDifferentAssertionErrors();
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
      case "publicWebsitesResolutionCheckForDefaultFlags":
        verifyWebsiteResolutionBasedOnFlags("default");
        break;
      case "autoHealBaseLineCapture":
        autoHealBaseLineCapture();
        restoreExistingBaseLinesIfNeeded();
        break;
      case "autoHealWithOldExistingLocators":
        autoHealWithOldExistingLocators();
        break;
      case "autoHealWithNewLocators":
        autoHealWithNewLocators();
        break;
      case "chromeProfile":
        verifyChromeProfileName();
        break;
      case "firefoxProfile":
        verifyFirefoxProfile();
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
    softAssert.assertTrue(pageHeading.equals("Basic Auth"), softAssertMessageFormat(BASIC_AUTH_FAILED_MESSAGE));
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
    softAssert.assertTrue(pageHeading.equals("Basic Auth"),
      softAssertMessageFormat(BASIC_AUTH_USING_KEYBOARD_EVENT_FAILED_MESSAGE));
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
      softAssertMessageFormat(SELF_SIGNED_CERT_ERROR_MESSAGE));
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
        softAssertMessageFormat(TIMEZONE_FAILURE_MESSAGE, testTimeZone, expectedTimeZone));
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
        softAssertMessageFormat(LOGIN_USING_KEYBOARD_EVENT_FAILURE_MESSAGE, afterLoginPageHeading));
    } else {
      softAssert.fail(softAssertMessageFormat(UNABLE_TO_OPEN_HEROKU_APP_LOGIN_PAGE_MESSAGE));
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
      softAssertMessageFormat(BROWSER_NAME_ERROR_MESSAGE, actualBrowserName, browserName));

    softAssert.assertTrue(browserVersion.contains(actualBrowserVersion),
      softAssertMessageFormat(BROWSER_VERSION_ERROR_MESSAGE, actualBrowserVersion, browserVersion));

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
      softAssertMessageFormat(EXTENSION_NOT_WORKING_ERROR_MESSAGE));
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
      softAssertMessageFormat(BASE_TEST_ERROR_MESSAGE, sampleText, actualText));
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void uploadFile() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    driverManager.setLocalFileDetector();
    driverManager.getURL(FILE_UPLOAD_URL);
    driverManager.sendKeys(chooseFileButton, SAMPLE_TXT_FILE_PATH);
    driverManager.click(uploadFileButton);
    softAssert.assertTrue(driverManager.isDisplayed(uploadedFileHeading, 5),
      softAssertMessageFormat(UPLOAD_FILE_ERROR_MESSAGE));
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
      softAssertMessageFormat(GEOLOCATION_ERROR_MESSAGE, expectedCountryName, actualCountryName));
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void loginCacheCleanedCheckUsingLTLoginPage() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();

    String ltDashboardUrl = Boolean.parseBoolean(IS_GDPR_TEST_CONFIG) ?
      getCorrespondingDashboardUrlForGDPRUser(LT_LOGIN_URL) :
      LT_LOGIN_URL;

    driverManager.getURL(ltDashboardUrl);

    boolean isLTPageOpened = driverManager.isDisplayed(ltPageHeading, 10);
    boolean isLoginFormDisplayed = driverManager.isDisplayed(ltLoginPageEmailInput, 5);

    if (!isLTPageOpened) {
      // If the LT page is not opened, it could be due to a network issue or the page being down.
      softAssert.fail(softAssertMessageFormat(UNABLE_TO_OPEN_LT_PAGE_ERROR_MESSAGE));
    } else if (!isLoginFormDisplayed) {
      // If the login form is not displayed, it could be due to a cache is not cleared, and it automatically redirects to the login success page.
      softAssert.fail(softAssertMessageFormat(LOGIN_CACHE_CHECK_FAILED_ERROR_MESSAGE));
    } else {
      // If the login form is displayed, it means the cache is cleared, and we can proceed with the login.
      driverManager.sendKeys(ltLoginPageEmailInput, USER_EMAIL);
      driverManager.sendKeys(ltLoginPagePasswordInput, USER_PASS);
      driverManager.click(ltLoginPageSubmitButton);
      softAssert.assertTrue(driverManager.isDisplayed(ltLoginSuccessVerification, 20),
        softAssertMessageFormat(LOGIN_CACHE_NOT_SET_FAILED_ERROR_MESSAGE));
    }
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void throwNewError() {
    throw new RuntimeException("Something went wrong! This is a trial error :)");
  }

  private void testLocalUrlWithTunnel() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    int httpServerStatus = apiHelper.getStatusCode(LOCAL_URL, null, null, null, null);
    CustomAssert.assertEquals(httpServerStatus, 200,
      "Please start http server on port 8000 to start verifying tunnel. Expected status code: 200, original status code: " + httpServerStatus);
    driverManager.getURL(LOCAL_URL);
    boolean localUrlStatus = driverManager.isDisplayed(localUrlHeading, 10);
    softAssert.assertTrue(localUrlStatus, softAssertMessageFormat(LOCAL_URL_CHECK_FAILED_WITH_TUNNEL_ERROR_MESSAGE));
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void runABTestIHA(CustomSoftAssert softAssert) {
    driverManager.getURL(INTERNET_HEROKU_APP_ABTEST);
    String abTestHeading = driverManager.getText(abtestHeadingIHA);
    softAssert.assertTrue(abTestHeading.equals("A/B Test Variation 1"),
      softAssertMessageFormat(AB_IHA_TEST_FAILED_ERROR_MESSAGE));
  }

  private void addOrRemoveElementIHA(CustomSoftAssert softAssert) {
    driverManager.getURL(INTERNET_HEROKU_APP_ADD_REMOVE_ELEMENT_URL);
    driverManager.click(addElementButtonIHA);
    softAssert.assertTrue(driverManager.isDisplayed(deleteElementButtonIHA, 5),
      softAssertMessageFormat(ADD_ELEMENT_IHA_TEST_FAILED_ERROR_MESSAGE));
  }

  private void testBrokenImagesIHA(CustomSoftAssert softAssert) {
    driverManager.getURL(INTERNET_HEROKU_APP_BROKEN_IMAGES_URL);
    List<WebElement> elements = driverManager.findElements(brokenImagesIHA);
    softAssert.assertFalse(elements.isEmpty(), softAssertMessageFormat(BROKEN_IMAGES_IHA_TEST_FAILED_ERROR_MESSAGE));
  }

  private void checkBoxIHA(CustomSoftAssert softAssert) {
    driverManager.getURL(INTERNET_HEROKU_APP_CHECK_BOXES_URL);
    driverManager.click(checkboxesIHA);
    softAssert.assertTrue(driverManager.isSelected(checkboxesIHA, 5),
      softAssertMessageFormat(CHECKBOXES_IHA_TEST_FAILED_ERROR_MESSAGE));
  }

  private void dynamicContentIHA(CustomSoftAssert softAssert) {
    driverManager.getURL(INTERNET_HEROKU_APP_DYNAMIC_CONTENT_URL);
    driverManager.click(dynamicContentClickIHA);
    String s1 = driverManager.getText(staticParagraphIHA);
    driverManager.refreshPage();
    String s2 = driverManager.getText(staticParagraphIHA);
    softAssert.assertTrue(s1.equals(s2), softAssertMessageFormat(DYNAMIC_CONTENT_IHA_TEST_FAILED_ERROR_MESSAGE));
  }

  private void dynamicControlsIHA(CustomSoftAssert softAssert) {
    driverManager.getURL(INTERNET_HEROKU_APP_DYNAMIC_CONTROLS_URL);
    driverManager.click(checkBoxSwapButtonIHA);
    softAssert.assertTrue(driverManager.getText(messageInDynamicControlsPageIHA, 10).equals("It's gone!"),
      softAssertMessageFormat(DYNAMIC_CONTROLS_IHA_TEST_FAILED_ERROR_MESSAGE_1));
    driverManager.click(checkBoxSwapButtonIHA);
    softAssert.assertTrue(driverManager.getText(messageInDynamicControlsPageIHA, 10).equals("It's back!"),
      softAssertMessageFormat(DYNAMIC_CONTROLS_IHA_TEST_FAILED_ERROR_MESSAGE_2));
    driverManager.click(textBoxEnableButtonIHA);
    softAssert.assertTrue(driverManager.getText(messageInDynamicControlsPageIHA, 10).equals("It's enabled!"),
      softAssertMessageFormat(DYNAMIC_CONTROLS_IHA_TEST_FAILED_ERROR_MESSAGE_3));
    driverManager.click(textBoxEnableButtonIHA);
    softAssert.assertTrue(driverManager.getText(messageInDynamicControlsPageIHA, 10).equals("It's disabled!"),
      softAssertMessageFormat(DYNAMIC_CONTROLS_IHA_TEST_FAILED_ERROR_MESSAGE_4));
  }

  private void loginFormFillUpIHA(CustomSoftAssert softAssert) {
    driverManager.getURL(INTERNET_HEROKU_APP_LOGIN_PAGE_URL);
    driverManager.sendKeys(userNameInputTHA, "tomsmith");
    driverManager.sendKeys(passwordInputTHA, "SuperSecretPassword!");
    driverManager.click(loginButtonTHA);
    softAssert.assertTrue(driverManager.getText(loginSuccessHeaderTHA).contains("Welcome to the Secure Area"),
      softAssertMessageFormat(FORM_LOGIN_IHA_TEST_FAILED_ERROR_MESSAGE));
    if (driverManager.isSelected(loginSuccessHeaderTHA)) {
      driverManager.click(logoutButtonTHA);
      softAssert.assertTrue(driverManager.getText(loginPageHeadingTHA).contains("Login Page"),
        softAssertMessageFormat(FORM_LOGOUT_IHA_TEST_FAILED_ERROR_MESSAGE));
    }
  }

  private void runDifferentHerokuAppTest() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    // runABTestIHA(softAssert);
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
    softAssert.assertTrue(status.equals("success"),
      softAssertMessageFormat(UPLOAD_SAMPLE_TERMINAL_LOGS_FAILED_ERROR_MESSAGE));
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

    String jobPurpose = System.getProperty(JOB_PURPOSE, "");
    // List of artefacts to skip for smoke tests
    if (jobPurpose.equalsIgnoreCase("smoke") && skipArtefactsForSmokeTests.contains(logs)) {
      ltLogger.info("Skipping api verification of {} logs for smoke tests", logs);
      System.err.printf("Skipping api verification of %s logs for smoke tests%n", logs);
      return;
    }

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

    // softAssert.assertTrue(validLogsToCheck,
    // logs + " logs verification is skipped as required caps are not used. Required
    // caps: " + testArtefactsToCapsMap.getOrDefault(
    // logs, Collections.emptyMap()));

    if (!validLogsToCheck) {
      System.err.println(
        logs + " logs verification is skipped as required caps are not used. Required caps: " + testArtefactsToCapsMap.getOrDefault(
          logs, Collections.emptyMap()));
      return;
    }

    // Start validating the logs
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
    CustomAssert.assertTrue(RUNNING.equalsIgnoreCase(statusBeforeStoppingTheTest),
      softAssertMessageFormat(UNABLE_TO_STOP_TEST_ERROR_MESSAGE, statusBeforeStoppingTheTest));
    Response response = apiHelper.stopTestViaApi(sessionId);
    String status = response.jsonPath().get("status").toString();
    String message = response.jsonPath().get("message").toString();
    softAssert.assertTrue(status.equalsIgnoreCase("success"),
      softAssertMessageFormat(STOP_TEST_SESSION_VIA_API_ERROR_MESSAGE, status, message));
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

    CustomAssert.assertTrue(expectedStatus.equalsIgnoreCase(currentStatus),
      softAssertMessageFormat(TEST_STATUS_MISMATCH_ERROR_MESSAGE, maxRetries, expectedStatus, currentStatus));
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
    CustomAssert.assertTrue(RUNNING.equalsIgnoreCase(buildStatus),
      softAssertMessageFormat(UNABLE_TO_STOP_BUILD_ERROR_MESSAGE, buildStatus));
    Response response = apiHelper.stopBuildViaApi(buildId);
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  public void verifyBuildStatusViaAPI(String buildStatus_ind) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    String buildId = apiHelper.getBuildIdFromSessionId(EnvSetup.TEST_SESSION_ID.get());
    String buildStatus = apiHelper.getStatusOfBuildViaAPI(buildId);
    softAssert.assertTrue(STOPPED.equalsIgnoreCase(buildStatus),
      softAssertMessageFormat(BUILD_STATUS_MISMATCH_ERROR_MESSAGE, buildStatus, buildStatus_ind));
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  public void verifyTunnelStatusViaAPI(String tunnelName, String expectedTunnelStatus) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    Map<String, String> runningTunnelNameToTunnelIDMap = apiHelper.getAllRunningTunnels();
    switch (expectedTunnelStatus) {
    case RUNNING -> softAssert.assertTrue(runningTunnelNameToTunnelIDMap.containsKey(tunnelName),
      softAssertMessageFormat(TUNNEL_NOT_IN_RUNNING_STATE_ERROR_MESSAGE, tunnelName));
    case STOPPED -> softAssert.assertFalse(runningTunnelNameToTunnelIDMap.containsKey(tunnelName),
      softAssertMessageFormat(TUNNEL_NOT_STOPPED_ERROR_MESSAGE, tunnelName, expectedTunnelStatus));
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
      softAssertMessageFormat(STOP_RUNNING_TUNNEL_FAILED_ERROR_MESSAGE, tunnelID, status));
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
      CustomAssert.assertEquals(httpServerStatus, 200,
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

  /// Expected expectedLocation values tunnelClient, tunnelServer, dc
  private void checkPublicWebsitesAreResolvedInExpectedLocation(String expectedLocation, String tunnelFlagName) {
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
    softAssert.assertTrue(locationWherePublicWebsiteResolved.equals(expectedLocation),
      softAssertMessageFormat(PUBLIC_WEBSITES_NOT_RESOLVED_IN_EXPECTED_PLACE_ERROR_MESSAGE, tunnelFlagName,
        expectedLocation, locationWherePublicWebsiteResolved, ipAndLocation[0], ipAndLocation[1]));
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  /**
   * For forceLocal flag all the websites should be resolved in tunnel client
   * For bypassHosts=*lambda* flag all the websites should be resolved in tunnel client except domains that match *lambda* that should be resolved in dc
   * For allowHosts=*lambda* flag all the websites should be resolved in dc except domains that match *lambda* that should be resolved in tunnel client
   * If none of [forceLocal, bypassHosts, allowHosts] the flags are not used then public websites are resolved in dc if ml_resolve_tunnel_website_in_dc is enabled else in tunnel client, only private websites will be resolved in tunnel client
   */
  private void verifyWebsiteResolutionBasedOnFlags(String tunnelFlagName) {
    final String flagName = "ml_resolve_tunnel_website_in_dc";
    String flagValue = apiHelper.getFeatureFlagValueOfSpecificSession(EnvSetup.TEST_SESSION_ID.get(), flagName);
    switch (tunnelFlagName) {
    case "forceLocal", "bypassHosts" ->
      checkPublicWebsitesAreResolvedInExpectedLocation("tunnelClient", tunnelFlagName);
    case "allowHosts" -> checkPublicWebsitesAreResolvedInExpectedLocation("dc", tunnelFlagName);
    default -> {
      if (flagValue.equalsIgnoreCase("true")) {
        checkPublicWebsitesAreResolvedInExpectedLocation("dc", tunnelFlagName);
      } else {
        checkPublicWebsitesAreResolvedInExpectedLocation("tunnelClient", tunnelFlagName);
      }
    }
    }
  }

  private void checkAllowHostFlagForTunnel() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    final Map<String, String> expectedLocalUrls = Map.of(LOCAL_PRIVATE_PROXY_URL,
      "Please update etc/hosts with value `127.0.0.1       localhost.lambdatest.com` and retry", LOCAL_LAMBDA_URL,
      "Please update etc/hosts with value `127.0.0.1       locallambda.com` and retry");
    checkLocalWebSitesAreReachable(expectedLocalUrls);
    driverManager.getURL(LOCAL_LAMBDA_URL);
    softAssert.assertTrue(driverManager.isDisplayed(localUrlHeading, 5),
      softAssertMessageFormat(ALLOW_HOSTS_TUNNEL_FLAG_NOT_WORKING_ERROR_MESSAGE_1, LOCAL_LAMBDA_URL));
    driverManager.getURL(LOCAL_PRIVATE_PROXY_URL);
    softAssert.assertFalse(driverManager.isDisplayed(localUrlHeading, 5),
      softAssertMessageFormat(ALLOW_HOSTS_TUNNEL_FLAG_NOT_WORKING_ERROR_MESSAGE_2, LOCAL_PRIVATE_PROXY_URL));
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void checkBypassHostFlagForTunnel() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    final Map<String, String> expectedLocalUrls = Map.of(LOCAL_PRIVATE_PROXY_URL,
      "Please update etc/hosts with value `127.0.0.1       localhost.lambdatest.com` and retry", LOCAL_LAMBDA_URL,
      "Please update etc/hosts with value `127.0.0.1       locallambda.com` and retry");
    checkLocalWebSitesAreReachable(expectedLocalUrls);
    driverManager.getURL(LOCAL_LAMBDA_URL);
    softAssert.assertFalse(driverManager.isDisplayed(localUrlHeading, 5),
      softAssertMessageFormat(BYPASS_HOSTS_TUNNEL_FLAG_NOT_WORKING_ERROR_MESSAGE_1, LOCAL_LAMBDA_URL));
    driverManager.getURL(LOCAL_PRIVATE_PROXY_URL);
    softAssert.assertTrue(driverManager.isDisplayed(localUrlHeading, 5),
      softAssertMessageFormat(BYPASS_HOSTS_TUNNEL_FLAG_NOT_WORKING_ERROR_MESSAGE_2, LOCAL_PRIVATE_PROXY_URL));
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void testLocalUrlWithCustomDomainForTunnel() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    final Map<String, String> expectedLocalUrls = Map.of(LOCAL_PRIVATE_PROXY_URL,
      "Please update etc/hosts with value `127.0.0.1       localhost.lambdatest.com` and retry", LOCAL_LAMBDA_URL,
      "Please update etc/hosts with value `127.0.0.1       locallambda.com` and retry");
    checkLocalWebSitesAreReachable(expectedLocalUrls);
    driverManager.getURL(LOCAL_LAMBDA_URL);
    softAssert.assertTrue(driverManager.isDisplayed(localUrlHeading, 5),
      softAssertMessageFormat(LOCAL_URL_NOT_WORKING_WITH_TUNNEL_ERROR_MESSAGE, LOCAL_LAMBDA_URL));
    driverManager.getURL(LOCAL_PRIVATE_PROXY_URL);
    softAssert.assertTrue(driverManager.isDisplayed(localUrlHeading, 5),
      softAssertMessageFormat(LOCAL_URL_NOT_WORKING_WITH_TUNNEL_ERROR_MESSAGE, LOCAL_PRIVATE_PROXY_URL));
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void testActionToTryoutDifferentAssertionErrors() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    softAssert.assertTrue(false, softAssertMessageFormat(BASIC_AUTH_FAILED_MESSAGE));
    CustomAssert.assertFalse(true,
      softAssertMessageFormat(LOGIN_USING_KEYBOARD_EVENT_FAILURE_MESSAGE, Thread.currentThread().threadId()));
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void autoHealBaseLineCapture() {
    final String expectedHeading = "Challenging DOM";
    String baselineCaptureSuccess = "false";
    String customClassName = "customClassNameForWork_" + getRandomAlphaNumericString(5);
    driverManager.getURL(CHALLENGING_DOM_PAGE_URL);
    if (driverManager.isDisplayed(challengingDomPageHeading) && driverManager.getText(challengingDomPageHeading)
      .equals(expectedHeading)) {

      // This mocks the class attribute of the element to be auto-healed
      driverManager.setCustomAttributeValue(challengingDomPageWorkOption, "class", customClassName);

      // Verify if the class attribute is set correctly
      if (driverManager.isDisplayed(new Locator(LocatorTypes.CLASS_NAME, customClassName))) {
        ltLogger.info("Element attribute is mocked successfully. Custom class name {} is already set on the element",
          customClassName);
        baselineCaptureSuccess = "true";
      }
      String finalBaselineCaptureSuccess = baselineCaptureSuccess;

      TEST_VERIFICATION_DATA.get().put(AUTO_HEAL_DATA, new HashMap<String, String>() {{
        put("baselineCaptureSuccess", finalBaselineCaptureSuccess);
        put("customClassName", customClassName);
      }});

    } else {
      CustomAssert.fail(softAssertMessageFormat(UNABLE_TO_NAVIGATE_TO_PUBLIC_URL_MESSAGE, CHALLENGING_DOM_PAGE_URL));
    }
  }

  /**
   * In this method we mock the original locators or baselines locators to some custom attribute values and access them.
   * When we get 200 response for the mocked locators, then the baseline is captured successfully.
   * Then in the auto-heal test we will try to find the same elements without the mocked attribute values so the locator should be auto-healed
   */
  private void restoreExistingBaseLinesIfNeeded() {
    if (System.getProperty(RESTORE_EXISTING_AUTO_HEAL_BASELINES, "false").equalsIgnoreCase("true")) {
      ltLogger.info("Restoring existing auto-heal baselines as per the system property");
      createBaseLineForAutoHealTestOfLongLocator();
      createBaseLineForAutoHealTestOfSampleIMDbWebsite();
    }
  }

  /// Create baseline for long locators
  private void createBaseLineForAutoHealTestOfLongLocator() {
    final String todoListPageHeading = "To Do List";
    driverManager.getURL(AUTO_HEAL_TO_DO_LIST_SAMPLE_URL);
    CustomAssert.assertTrue(driverManager.getText(autoHealToDoListSampleHeading).equals(todoListPageHeading),
      softAssertMessageFormat(UNABLE_TO_NAVIGATE_TO_PUBLIC_URL_MESSAGE, AUTO_HEAL_TO_DO_LIST_SAMPLE_URL));
    driverManager.sendKeys(autoHealToDoListSampleInput, "Task-1");
    driverManager.click(autoHealToDoListSampleAddButton);
    driverManager.acceptAlert();
    driverManager.setCustomAttributeValue(autoHealToDoListSampleListItemForBaseLine, "class", "custom-class-for-test");
    CustomAssert.assertTrue(driverManager.isDisplayed(autoHealToDoListSampleListItem),
      softAssertMessageFormat(UNABLE_TO_SET_BASELINE_FOR_AUTO_HEAL_TEST_ERROR_MESSAGE, "auto-heal-long-locators"));
  }

  /// Create baseline for auto-heal test of sample IMDb website
  private void createBaseLineForAutoHealTestOfSampleIMDbWebsite() {
    driverManager.getURL(AUTO_HEAL_IMDB_SAMPLE_URL);
    CustomAssert.assertTrue(driverManager.isDisplayed(autoHealImdbFavButton, 5),
      softAssertMessageFormat(UNABLE_TO_NAVIGATE_TO_PUBLIC_URL_MESSAGE, AUTO_HEAL_IMDB_SAMPLE_URL));
    driverManager.sendKeys(autoHealImdbInput, "Attack on Titan");
    driverManager.waitForElementToBeVisible(autoHealImdbSearchResultList, 20);
    driverManager.setCustomAttributeValue(autoHealImdbListItemBaseLine, "class", "custom-class-for-test-li");
    CustomAssert.assertTrue(driverManager.isDisplayed(autoHealImdbListItem),
      softAssertMessageFormat(UNABLE_TO_SET_BASELINE_FOR_AUTO_HEAL_TEST_ERROR_MESSAGE, autoHealImdbListItem.value()));
    driverManager.setCustomAttributeValue(autoHealImdbAddFavButtonBaseLine, "class", "custom-class-for-test-button");
    CustomAssert.assertTrue(driverManager.isDisplayed(autoHealImdbAddFavButton),
      softAssertMessageFormat(UNABLE_TO_SET_BASELINE_FOR_AUTO_HEAL_TEST_ERROR_MESSAGE,
        autoHealImdbAddFavButton.value()));
    driverManager.click(autoHealImdbAddFavButton);
    CustomAssert.assertFalse(driverManager.findElements(autoHealImdbFavList).isEmpty(),
      softAssertMessageFormat(UNABLE_TO_SET_BASELINE_FOR_AUTO_HEAL_TEST_ERROR_MESSAGE,
        "Unable to click on correct element, mocking failed for auto-heal imdb test"));
    driverManager.setCustomAttributeValue(autoHealImdbFavListItemBaseLine, "class", "custom-class-for-test-title");
    CustomAssert.assertTrue(driverManager.isDisplayed(autoHealImdbFavListItem),
      softAssertMessageFormat(UNABLE_TO_SET_BASELINE_FOR_AUTO_HEAL_TEST_ERROR_MESSAGE,
        autoHealImdbFavListItem.value()));
  }

  private void autoHealWithOldExistingLocators() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    testAutoHealOfLongLocators(softAssert);
    testBasicAutoHealOfSampleIMDbWebsite(softAssert);
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void testAutoHealOfLongLocators(CustomSoftAssert softAssert) {
    final String todoListPageHeading = "To Do List";
    driverManager.getURL(AUTO_HEAL_TO_DO_LIST_SAMPLE_URL);
    CustomAssert.assertTrue(driverManager.getText(autoHealToDoListSampleHeading, 5).equals(todoListPageHeading),
      softAssertMessageFormat(UNABLE_TO_NAVIGATE_TO_PUBLIC_URL_MESSAGE, AUTO_HEAL_TO_DO_LIST_SAMPLE_URL));
    driverManager.sendKeys(autoHealToDoListSampleInput, "Task-1");
    driverManager.click(autoHealToDoListSampleAddButton);
    driverManager.acceptAlert();

    // Check if the locator is auto-healed
    if (driverManager.isDisplayed(autoHealToDoListSampleListItem, 5)) {

      // Check if auto healed locator is expected one
      String addedItemString = driverManager.getText(autoHealToDoListSampleListItem);
      softAssert.assertTrue(addedItemString.contains("Task-1"),
        softAssertMessageFormat(AUTO_HEALED_LOCATOR_IS_LOCATING_SOME_OTHER_ELEMENT_THAN_EXPECTED_ERROR_MESSAGE,
          autoHealToDoListSampleListItem.value(), "Task-1", addedItemString));
    } else {
      softAssert.fail(softAssertMessageFormat(AUTO_HEAL_NOT_WORKING_FOR_LOCATORS_ERROR_MESSAGE,
        autoHealToDoListSampleListItem.value()));
    }
  }

  private void testBasicAutoHealOfSampleIMDbWebsite(CustomSoftAssert softAssert) {
    final String sampleImdbSearch = "Attack on Titan";
    driverManager.getURL(AUTO_HEAL_IMDB_SAMPLE_URL);
    CustomAssert.assertTrue(driverManager.isDisplayed(autoHealImdbFavButton, 5),
      softAssertMessageFormat(UNABLE_TO_NAVIGATE_TO_PUBLIC_URL_MESSAGE, AUTO_HEAL_IMDB_SAMPLE_URL));
    driverManager.sendKeys(autoHealImdbInput, sampleImdbSearch);
    driverManager.waitForElementToBeVisible(autoHealImdbSearchResultList, 20);

    // First auto heal check
    boolean firstAutoHealOfElementSuccess = driverManager.isDisplayed(autoHealImdbListItem, 5);
    if (firstAutoHealOfElementSuccess) {
      String searchResultText = driverManager.getText(autoHealImdbListItem, 5);
      softAssert.assertTrue(searchResultText.contains(sampleImdbSearch),
        softAssertMessageFormat(AUTO_HEALED_LOCATOR_IS_LOCATING_SOME_OTHER_ELEMENT_THAN_EXPECTED_ERROR_MESSAGE,
          autoHealImdbListItem.value(), sampleImdbSearch, searchResultText));
    } else {
      softAssert.fail(
        softAssertMessageFormat(AUTO_HEAL_NOT_WORKING_FOR_LOCATORS_ERROR_MESSAGE, autoHealImdbListItem.value()));
      return;
    }

    // Second auto heal check
    boolean secondAutoHealOfElementSuccess = driverManager.isDisplayed(autoHealImdbAddFavButton, 5);
    if (secondAutoHealOfElementSuccess) {
      driverManager.click(autoHealImdbAddFavButton);
    } else {
      softAssert.fail(
        softAssertMessageFormat(AUTO_HEAL_NOT_WORKING_FOR_LOCATORS_ERROR_MESSAGE, autoHealImdbAddFavButton.value()));
      return;
    }

    // Third auto heal check
    String favListText = driverManager.getText(autoHealImdbFavListItem, 5);
    softAssert.assertTrue(favListText.contains(sampleImdbSearch),
      softAssertMessageFormat(AUTO_HEALED_LOCATOR_IS_LOCATING_SOME_OTHER_ELEMENT_THAN_EXPECTED_ERROR_MESSAGE,
        autoHealImdbFavListItem.value(), sampleImdbSearch, favListText));
  }

  private void autoHealWithNewLocators() {
    final String expectedHeading = "Challenging DOM";
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    HashMap<String, String> autoHealBaseLineCaptureData = (HashMap<String, String>) TEST_VERIFICATION_DATA.get()
      .get(AUTO_HEAL_DATA);
    if (autoHealBaseLineCaptureData.get("baselineCaptureSuccess").equalsIgnoreCase("true")) {
      // Break the test early if baseline was not captured successfully

      driverManager.getURL(CHALLENGING_DOM_PAGE_URL);
      if (driverManager.isDisplayed(challengingDomPageHeading, 10) && driverManager.getText(challengingDomPageHeading)
        .equals(expectedHeading)) {

        String customClassName = autoHealBaseLineCaptureData.get("customClassName");
        boolean autoHealSuccess = driverManager.isDisplayed(new Locator(LocatorTypes.CLASS_NAME, customClassName), 10);
        softAssert.assertTrue(autoHealSuccess,
          softAssertMessageFormat(AUTO_HEAL_NOT_WORKING_FOR_LOCATORS_ERROR_MESSAGE, customClassName));

      } else {
        softAssert.fail(softAssertMessageFormat(UNABLE_TO_NAVIGATE_TO_PUBLIC_URL_MESSAGE, CHALLENGING_DOM_PAGE_URL));
      }
    } else {
      softAssert.fail(softAssertMessageFormat(AUTO_HEAL_BASELINE_CAPTURE_FAILED_ERROR_MESSAGE));
    }
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  public String getCurrentSshConnectionType() {
    try {
      String sshConnType = tunnelManager.getCurrentSshConnectionType();
      ltLogger.info("Retrieved SSH connection type from TunnelManager: {}", sshConnType);
      return sshConnType;
    } catch (Exception e) {
      ltLogger.error("Failed to get SSH connection type from TunnelManager: {}", e.getMessage());
      throw new RuntimeException("Failed to get SSH connection type", e);
    }
  }

  public void iVerifyTunnelConnectionUsesProtocol(String protocol) {
    ltLogger.info("Verifying tunnel connection uses {} protocol", protocol);

    waitForTime(3);

    String actualProtocol = tunnelManager.getCurrentTunnelMode();
    ltLogger.info("Protocol used in Tunnel: Expected={}, Actual={}", protocol, actualProtocol);
    CustomAssert.assertTrue(actualProtocol.equalsIgnoreCase(protocol),
      softAssertMessageFormat(TUNNEL_MODE_PORT_VERIFICATION_FAILURE_ERROR_MESSAGE, "protocol", protocol,
        actualProtocol));
    ltLogger.info("Tunnel protocol verification successful. Using protocol: {}", protocol);
  }

  public void iVerifyTunnelUsesConnection(String expectedConnectionType) {
    ltLogger.info("Verifying tunnel uses {} connection", expectedConnectionType);

    waitForTime(3);

    String actualSshConnType = tunnelManager.getCurrentSshConnectionType();
    ltLogger.info("SSH connection type verification result: Expected={}, Actual={}", expectedConnectionType,
      actualSshConnType);
    CustomAssert.assertTrue(expectedConnectionType.equalsIgnoreCase(actualSshConnType),
      softAssertMessageFormat(TUNNEL_MODE_PORT_VERIFICATION_FAILURE_ERROR_MESSAGE, "SSH connection type",
        expectedConnectionType, actualSshConnType));

    ltLogger.info("Tunnel SSH connection type verification successful. Using: {}", expectedConnectionType);
  }

  public void iVerifyTunnelConnectsOnPort(int port) {
    ltLogger.info("Verifying tunnel connects on port {}", port);

    waitForTime(3);

    int currentPort = tunnelManager.getCurrentTunnelPort();
    CustomAssert.assertTrue(currentPort == port,
      softAssertMessageFormat(TUNNEL_MODE_PORT_VERIFICATION_FAILURE_ERROR_MESSAGE, "expected port for connection", port,
        currentPort));

    ltLogger.info("Tunnel port verification successful. Connecting on port: {}", port);
  }

  public void iVerifyTunnelConnectsOnPortUsingSSH(int port) {
    ltLogger.info("Verifying tunnel connects on port {} using SSH", port);

    waitForTime(3);

    String actualTunnelMode = tunnelManager.getCurrentTunnelMode();
    ltLogger.info("Tunnel mode verification: Expected=ssh, Actual={}", actualTunnelMode);
    CustomAssert.assertTrue(actualTunnelMode.equalsIgnoreCase("ssh"),
      softAssertMessageFormat(TUNNEL_MODE_PORT_VERIFICATION_FAILURE_ERROR_MESSAGE, "mode", "ssh", actualTunnelMode));

    int currentPort = tunnelManager.getCurrentTunnelPort();
    ltLogger.error("Tunnel SSH port verification status. Expected port: {}, Actual port: {}", port, currentPort);
    CustomAssert.assertTrue(currentPort == port,
      softAssertMessageFormat(TUNNEL_MODE_PORT_VERIFICATION_FAILURE_ERROR_MESSAGE, "port", port, currentPort));
    ltLogger.info("Tunnel SSH port verification successful. Using SSH on port: {}", port);
  }

  public void iVerifyTunnelConnectsOnPortUsingTCP(int port) {
    ltLogger.info("Verifying tunnel connects on port {} using TCP", port);

    waitForTime(3);

    String currentMode = tunnelManager.getCurrentTunnelMode();
    ltLogger.info("Tunnel mode verification: Expected=tcp, Actual={}", currentMode);
    CustomAssert.assertTrue(currentMode.equalsIgnoreCase("tcp"),
      softAssertMessageFormat(TUNNEL_MODE_PORT_VERIFICATION_FAILURE_ERROR_MESSAGE, "mode", "tcp", currentMode));

    int currentPort = tunnelManager.getCurrentTunnelPort();
    ltLogger.info("Tunnel TCP port verification status. Expected port: {}, Actual port: {}", port, currentPort);
    CustomAssert.assertTrue(currentPort == port,
      softAssertMessageFormat(TUNNEL_MODE_PORT_VERIFICATION_FAILURE_ERROR_MESSAGE, "port", port, currentPort));

    ltLogger.info("Tunnel TCP port verification successful. Using TCP on port: {}", port);
  }

  public void iVerifyTunnelConnectsUsingWebSocket() {
    ltLogger.info("Verifying tunnel connects using WebSocket");

    waitForTime(3);

    String currentMode = tunnelManager.getCurrentTunnelMode();
    ltLogger.info("Tunnel mode verification: Expected=ws, Actual={}", currentMode);
    CustomAssert.assertTrue(currentMode.equalsIgnoreCase("ws"),
      softAssertMessageFormat(TUNNEL_MODE_PORT_VERIFICATION_FAILURE_ERROR_MESSAGE, "mode", "ws", currentMode));

    ltLogger.info("Tunnel WebSocket verification successful. Using WebSocket mode");
  }

  public void iVerifyTunnelUsesProtocol(String expectedProtocol) {
    ltLogger.info("Verifying tunnel uses {} protocol", expectedProtocol);

    waitForTime(3);

    String protocolToCheck = expectedProtocol;
    if (expectedProtocol.contains(":")) {
      protocolToCheck = expectedProtocol.split(":")[0];
    }

    String actualProtocol = tunnelManager.getCurrentTunnelMode();
    ltLogger.info("Tunnel protocol verification result: Expected protocol={}, Actual protocol={}", protocolToCheck,
      actualProtocol);
    CustomAssert.assertTrue(actualProtocol.equalsIgnoreCase(protocolToCheck),
      softAssertMessageFormat(TUNNEL_MODE_PORT_VERIFICATION_FAILURE_ERROR_MESSAGE, "protocol", expectedProtocol,
        actualProtocol));

    if (expectedProtocol.contains(":")) {
      String[] parts = expectedProtocol.split(":");
      if (parts.length == 2) {
        try {
          int expectedPort = Integer.parseInt(parts[1]);
          int currentPort = tunnelManager.getCurrentTunnelPort();
          CustomAssert.assertTrue(expectedPort == currentPort,
            softAssertMessageFormat(TUNNEL_MODE_PORT_VERIFICATION_FAILURE_ERROR_MESSAGE, "port", expectedPort,
              currentPort));
        } catch (NumberFormatException e) {
          ltLogger.warn("Could not parse port from protocol: {}", expectedProtocol);
        }
      }
    }

    ltLogger.info("Tunnel protocol verification successful. Using: {}", expectedProtocol);
  }

  public void iVerifyAllTunnelFlagsAreAppliedCorrectly() {
    ltLogger.info("Verifying all tunnel flags are applied correctly");

    waitForTime(3);

    try {
      String currentMode = tunnelManager.getCurrentTunnelMode();
      String currentSshConnType = getCurrentSshConnectionType();
      int currentPort = tunnelManager.getCurrentTunnelPort();

      ltLogger.info("Current tunnel configuration:");
      ltLogger.info("  Mode: {}", currentMode);
      ltLogger.info("  SSH Connection Type: {}", currentSshConnType);
      ltLogger.info("  Port: {}", currentPort);

      if (currentMode == null || currentMode.isEmpty())
        softAssertMessageFormat(NULL_OR_EMPTY_API_RESPONSE_ERROR_MESSAGE, "tunnel info API", currentMode);
      ltLogger.info("Tunnel flags verification completed successfully");
    } catch (Exception e) {
      ltLogger.error("Failed to verify tunnel flags: {}", e.getMessage());
      throw new RuntimeException("Failed to verify tunnel flags: " + e.getMessage());
    }
  }

  public void iRestartTunnel() {
    ltLogger.info("Restarting tunnel");
    stopTunnel();
    waitForTime(5);
    startTunnel();
    waitForTime(10);
  }

  public void iRestartTunnelWith(String flags) {
    ltLogger.info("Restarting tunnel with flags: {}", flags);
    stopTunnel();
    waitForTime(5);
    startTunnel(flags);
    waitForTime(10);
  }

  public void iVerifyTunnelReconnectionOccurs() {
    ltLogger.info("Verifying tunnel reconnection occurs");
    modifyNetworkRestrictions(clientSideNetworkOperations.FLUSH_ALL_RULES);
    waitForTime(10);

    try {
      String currentMode = tunnelManager.getCurrentTunnelMode();
      if (currentMode == null || currentMode.isEmpty())
        CustomAssert.fail(
          softAssertMessageFormat(NULL_OR_EMPTY_API_RESPONSE_ERROR_MESSAGE, "tunnel info API", currentMode));
      ltLogger.info("Tunnel reconnection verification successful. Current mode: {}", currentMode);
    } catch (Exception e) {
      ltLogger.error("Tunnel reconnection verification failed: {}", e.getMessage());
      throw new RuntimeException("Tunnel reconnection verification failed", e);
    }
  }

  public void modifyNetworkRestrictions(clientSideNetworkOperations restrictionType, String... additionalParams) {
    ltLogger.info("Modifying network restrictions: {}, with params: {}", restrictionType,
      Arrays.toString(additionalParams));

    // Ensure that necessary parameters are provided
    if (restrictionType == clientSideNetworkOperations.ENSURE_PORT_OPEN && (additionalParams.length == 0 || additionalParams[0].isEmpty())) {
      throw new IllegalArgumentException("Port number must be provided for ENSURE_PORT_OPEN operation");
    }

    // Determine the script and parameters to use based on the restriction type
    String[] params = (restrictionType == clientSideNetworkOperations.ENSURE_PORT_OPEN) ?
      new String[] { restrictionType.getValue(), additionalParams[0] } :
      new String[] { restrictionType.getValue() };

    // Run the bash script with the correct parameters
    runBashScriptWithFlags(NETWORK_SCRIPT_PATH, true, params);

    // Adding a small wait after the operation
    waitForTime(2);
  }

  public void createTestShareLinkAndStoreItToSessionReport(String sessionId) {
    String getTestShareLinkUrl = apiHelper.getTestShareLinkUrl(sessionId);
    TEST_VERIFICATION_DATA.get().put(TEST_SHARE_LINK, getTestShareLinkUrl);
  }

  public void createBuildShareLinkAndStoreItToSessionReport(String buildId) {
    String getBuildShareLinkUrl = apiHelper.getBuildShareLinkUrl(buildId);
    TEST_VERIFICATION_DATA.get().put(BUILD_SHARE_LINK, getBuildShareLinkUrl);
  }

  public void verifyShareLinkViaApi(String linkType) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();

    String shareLink = linkType.equalsIgnoreCase("test") ?
      TEST_VERIFICATION_DATA.get().get(TEST_SHARE_LINK).toString() :
      TEST_VERIFICATION_DATA.get().get(BUILD_SHARE_LINK).toString();

    int responseCode = apiHelper.getRequest(shareLink).statusCode();
    ltLogger.info("Verifying {} share link: {}, Response Code: {}", linkType, shareLink, responseCode);

    softAssert.assertTrue(responseCode == 200,
      softAssertMessageFormat(SHARE_LINK_VERIFICATION_FAILURE_ERROR_MESSAGE, linkType, responseCode, shareLink));

    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  public void getBuildIdFromSessionIdAndStoreItToEnvVar(String sessionId) {
    String buildId = apiHelper.getBuildIdFromSessionId(sessionId);
    if (buildId != null && !buildId.isEmpty()) {
      EnvSetup.BUILD_ID.set(buildId);
      ltLogger.info("Build ID retrieved from session ID {}: {}", sessionId, buildId);
    } else {
      ltLogger.warn("No Build ID found for session ID: {}", sessionId);
      throw new RuntimeException("No Build ID found for session ID: " + sessionId);
    }
  }

  public void verifyChromeProfileName() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    driverManager.getURL(CHROME_BROWSER_VERSION_DETAILS_URL);
    String profilePath = driverManager.getText(chromeBrowserProfilePath, 5);
    ltLogger.info("Chrome profile path: {}", profilePath);
    String expectedProfilePath = TEST_CAPS_MAP.get().getOrDefault(BROWSER_PROFILE, "default").toString();
    String expectedProfileName = expectedProfilePath.substring(expectedProfilePath.lastIndexOf("/") + 1,
      expectedProfilePath.lastIndexOf("."));
    softAssert.assertTrue(profilePath.contains(expectedProfileName),
      softAssertMessageFormat(CHROME_PROFILE_NOT_WORKING_ERROR_MESSAGE, expectedProfileName, profilePath));
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private String getDirectoryPathUrlInVM(String directoryName) {
    String pathWithPlaceHolder = TEST_CAPS_MAP.get().getOrDefault(PLATFORM_NAME, "").toString().toLowerCase()
      .contains("win") ? WIN_LT_USER_DIRECTORY_URL_FORMAT : MAC_LT_USER_DIRECTORY_URL_FORMAT;
    return String.format(pathWithPlaceHolder, directoryName);
  }

  private Locator getFileLocatorInVM(String fileName) {
    return new Locator(fileInLtUserDirectoryLocatorFormat.type(),
      String.format(fileInLtUserDirectoryLocatorFormat.value(), fileName));
  }

  public void verifyFirefoxProfile() {
    final String folderName = "Documents";
    final String file = "sample1";
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    driverManager.getURLUsingJs(DOWNLOAD_SAMPLE_FILE_URL);
    waitForTime(5);
    String filePathUrl = getDirectoryPathUrlInVM(folderName);
    Locator fileLocator = getFileLocatorInVM(file);
    driverManager.getURL(filePathUrl);
    softAssert.assertTrue(driverManager.isDisplayed(fileLocator, 5),
      softAssertMessageFormat(FIREFOX_PROFILE_NOT_WORKING_ERROR_MESSAGE));
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  public void uploadFileToLambdaStorage(String type, String filePath) {
    switch (type.toLowerCase()) {
    case "browser profile" -> {
      String[] s3urlAndLastUpdatedTime = apiHelper.uploadBrowserProfile(filePath);
      EnvSetup.TEST_VERIFICATION_DATA.get().put(BROWSER_PROFILE_S3_URL, s3urlAndLastUpdatedTime[0]);
      EnvSetup.TEST_VERIFICATION_DATA.get().put(BROWSER_PROFILE_LAST_UPDATED_TIME, s3urlAndLastUpdatedTime[1]);
    }
    case "extension" -> {
      //      String s3url = apiHelper.uploadExtension(filePath);
      //      EnvSetup.TEST_VERIFICATION_DATA.get().put(EXTENSION_S3_URL, s3url);
    }
    default -> throw new IllegalArgumentException("Unsupported file type for upload to lambda storage: " + type);
    }
  }

  public void verifyFileInLambdaStorage(String type, String fileName) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    switch (type.toLowerCase()) {
    case "browser profile" -> {
      verifyBrowserProfileUpload(type, fileName, softAssert);
    }
    case "extension" -> {
      //      apiHelper.verifyExtensionInLambdaStorage(fileName);
    }
    default -> throw new IllegalArgumentException("Unsupported file type for verification in lambda storage: " + type);
    }
  }

  public void deleteFileFromLambdaStorage(String type, String fileName) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    switch (type.toLowerCase()) {
    case "browser profile" -> {
      apiHelper.deleteBrowserProfile(fileName);
    }
    case "extension" -> {
      //      apiHelper.deleteExtension(fileName);
      //      softAssert.assertTrue(apiHelper.isExtensionDeleted(fileName),
      //        softAssertMessageFormat(FILE_NOT_DELETED_FROM_LAMBDA_STORAGE_ERROR_MESSAGE, type, fileName));
    }
    default -> throw new IllegalArgumentException("Unsupported file type for deletion from lambda storage: " + type);
    }
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void verifyBrowserProfileUpload(String type, String fileName, CustomSoftAssert softAssert) {
    String lastUpdatedTime = apiHelper.verifyBrowserProfileInLambdaStorageAndGetLastUpdatedTimeStamp(fileName);
    // Verify the last updated time only if it exists in the test verification data
    if (TEST_VERIFICATION_DATA.get().get(BROWSER_PROFILE_LAST_UPDATED_TIME) != null) {
      String expectedLastUpdatedTime = TEST_VERIFICATION_DATA.get().get(BROWSER_PROFILE_LAST_UPDATED_TIME).toString();
      Duration timeDiff = getTimeDifference(expectedLastUpdatedTime, lastUpdatedTime, UTC_TimeZone,
        UTC_DATE_TIME_FORMAT);
      ltLogger.info(
        "Expected last modified time; {}, actual last modified time: {}, Difference between expected and actual last updated time: {}",
        expectedLastUpdatedTime, lastUpdatedTime, timeDiff.toSeconds());

      // Assert that the time difference is less than 10 seconds
      softAssert.assertTrue(Integer.toUnsignedLong(Math.toIntExact(timeDiff.toSeconds())) < 10,
        softAssertMessageFormat(FILE_NOT_UPDATED_IN_LAMBDA_STORAGE_ERROR_MESSAGE, type, fileName, lastUpdatedTime,
          expectedLastUpdatedTime));

    } else {
      ltLogger.warn("No last updated time found for browser profile verification");
    }
  }

  @SuppressWarnings("unchecked")
  public void verifyTestTags() {
    verifyTags("Test", (List<String>) TEST_CAPS_MAP.get().getOrDefault(TEST_TAGS, Collections.emptyList()),
      () -> apiHelper.getTagsFromSessionId(TEST_SESSION_ID.get()));
  }

  @SuppressWarnings("unchecked")
  public void verifyBuildTags(String... givenBuildId) {
    String buildId = (givenBuildId == null || givenBuildId.length == 0) ?
      apiHelper.getBuildIdFromSessionId(TEST_SESSION_ID.get()) :
      givenBuildId[0];

    verifyTags("Build", (List<String>) TEST_CAPS_MAP.get().getOrDefault(BUILD_TAGS, Collections.emptyList()),
      () -> apiHelper.getBuildTagsViaAPI(buildId));
  }

  private void verifyTags(String tagType, List<String> expectedTags, Supplier<List<String>> actualTagsSupplier) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    List<String> actualTags;

    try {
      actualTags = actualTagsSupplier.get();
    } catch (Exception e) {
      ltLogger.error("Failed to retrieve {} tags: {}, using empty list as fallback", tagType.toLowerCase(),
        e.getMessage());
      actualTags = Collections.emptyList();
    }

    if (expectedTags.isEmpty() && actualTags.isEmpty()) {
      ltLogger.info("No {} tags set, skipping verification", tagType.toLowerCase());
      return;
    }

    // Sort for consistent comparison
    expectedTags = new ArrayList<>(expectedTags);
    actualTags = new ArrayList<>(actualTags);
    Collections.sort(expectedTags);
    Collections.sort(actualTags);

    ltLogger.info("Verifying {} tags. Expected: {}, Actual: {}", tagType.toLowerCase(), expectedTags, actualTags);

    softAssert.assertTrue(expectedTags.equals(actualTags),
      softAssertMessageFormat(TAGS_VERIFICATION_FAILURE_ERROR_MESSAGE, tagType, expectedTags, actualTags));

    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

}