package automationHelper;

import DTOs.SwaggerAPIs.ArtefactsApiV2ResponseDTO;
import DTOs.SwaggerAPIs.FetchVideoAPIResponseDTO;
import DTOs.SwaggerAPIs.LighthouseReportDTO;
import TestManagers.ApiManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.mysql.cj.util.StringUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;
import utility.CustomSoftAssert;
import utility.EnvSetup;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utility.EnvSetup.*;
import static utility.FrameworkConstants.*;

public class TestArtefactsVerificationHelper extends ApiManager {

  private final Logger ltLogger = LogManager.getLogger(TestArtefactsVerificationHelper.class);

  private enum ArtefactAPIVersions {
    API_V1, API_V2
  }

  private enum ArtefactApiV2UrlStatus {
    SUCCESS, FAIL
  }

  @Getter private enum LogType {
    COMMAND("command"), SELENIUM("selenium"), WEBDRIVER("webdriver"), NETWORK("network"), CONSOLE("console"), TERMINAL(
      "terminal"), FULL_HAR("fullHar"), EXCEPTION("exception");
    private final String value;

    LogType(String value) {
      this.value = value;
    }
  }

  private static final String apiV2UrlGenerationSuccessMessage = "URL is succesfully generated";

  private static final String V2 = "/v2";
  private static final String seleniumThreeExpectedLogLine = "Selenium build info: version: '";
  private static final String seleniumFourExpectedLogLine = "Started Selenium Standalone ";

  private final AutomationAPIHelper automationAPIHelper = new AutomationAPIHelper();

  private String getFileName(String sessionID, String sessionDetail) {
    String outFileName = sessionID;
    if (sessionDetail.equals("selenium") || sessionDetail.equalsIgnoreCase("webdriver")) {
      outFileName = outFileName + ".log";
    } else if (sessionDetail.equals("network") || sessionDetail.equals("console")) {
      outFileName = outFileName + ".json";
    } else if (sessionDetail.equals("networkHar")) {
      outFileName = outFileName + ".har";
    } else {
      outFileName = outFileName + ".zip";
    }
    return outFileName;
  }

  private String constructArtefactsAPIUrl(String logType, ArtefactAPIVersions apiVersion, String session_id) {
    ltLogger.info("Constructing artefacts API URL with: \nLog type: {}, API version: {}, Session id: {}", logType,
      apiVersion.toString(), session_id);
    String uri = apiVersion.equals(ArtefactAPIVersions.API_V1) ?
      constructAPIUrlWithBasicAuth(EnvSetup.API_URL_BASE, SESSIONS_API_ENDPOINT, EnvSetup.testUserName.get(),
        EnvSetup.testAccessKey.get(), session_id, sessionApiEndpoints().get(logType)) :
      constructAPIUrlWithBasicAuth(EnvSetup.API_URL_BASE, SESSIONS_API_V2_ENDPOINT, EnvSetup.testUserName.get(),
        EnvSetup.testAccessKey.get(), session_id, V2 + sessionApiEndpoints().get(logType));
    ltLogger.info("Session API url to fetch artefacts {}: {}", logType, uri);
    return uri;
  }

  private String fetchLogs(String logType, ArtefactAPIVersions apiVersion, String sessionId) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    String apiUrl = constructArtefactsAPIUrl(logType, apiVersion, sessionId);
    String response = getRequestAsString(apiUrl);

    if (apiVersion.equals(ArtefactAPIVersions.API_V2)) {
      response = handleApiV2Response(response, sessionId, logType, softAssert);
      ltLogger.info("Fetched {} artefacts API URL response via API V2: {}", logType, response);
    } else {
      response = handleUnicodeEscapes(response);
      ltLogger.info("Fetched {} artefacts API URL response via API V1: {}", logType, response);
    }

    EnvSetup.SOFT_ASSERT.set(softAssert);
    return response;
  }

  private String handleApiV2Response(String response, String sessionId, String logType, CustomSoftAssert softAssert) {
    ltLogger.info("Fetched artefacts API URL response via API V2: {}", response);

    ArtefactsApiV2ResponseDTO artefactsApiV2ResponseDTO = convertJsonStringToPojo(response,
      new TypeToken<ArtefactsApiV2ResponseDTO>() {
      });

    String status = artefactsApiV2ResponseDTO.getStatus();
    String message = artefactsApiV2ResponseDTO.getMessage();

    boolean isDownloadSuccess = isApiV2DownloadSuccessful(status, message);
    softAssert.assertTrue(isDownloadSuccess,
      "Unable to retrieve artefacts API download URL from API V2. Message: " + message);

    if (isDownloadSuccess) {
      String logsDownloadUrl = artefactsApiV2ResponseDTO.getUrl();
      return downloadFileFromUrlAndExtractContentAsString(logsDownloadUrl, getFileName(sessionId, logType),
        TEST_LOGS_DOWNLOAD_DIRECTORY);
    } else {
      return null;
    }
  }

  private boolean isApiV2DownloadSuccessful(String status, String message) {
    return status.equalsIgnoreCase(ArtefactApiV2UrlStatus.SUCCESS.toString()) && message.equalsIgnoreCase(
      apiV2UrlGenerationSuccessMessage);
  }

  private void verifyPortNumber(String sessionId, String logs, boolean isWebDriverEnabled, String browserName) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    if (logs == null || logs.isEmpty()) {
      softAssert.fail("Unable to verify port number. Received API response: " + logs);
      return;
    }

    final String PORT_WEBDRIVER = "4445";
    final String PORT_SELENIUM = "41000";

    String expectedPortNumber = isWebDriverEnabled ? PORT_WEBDRIVER : PORT_SELENIUM;
    ltLogger.info("Expected port number for webdriver mode status {} is: {}", isWebDriverEnabled, expectedPortNumber);
    String regEx = getPortNumberRegex(browserName, isWebDriverEnabled);

    // Extract port number using regex
    String portNumber = extractPortNumberFromSystemLogs(logs, regEx);

    if (portNumber != null) {
      String expectedMessage = isWebDriverEnabled ?
        "With webdriver mode, expected port number is " + PORT_WEBDRIVER :
        "Expected port number is " + PORT_SELENIUM + " for Selenium Driver";

      softAssert.assertEquals(portNumber, expectedPortNumber, expectedMessage + ". But used port is: " + portNumber);

      ltLogger.info("Used Port: {} for session: {}", portNumber, sessionId);
    } else {
      softAssert.assertTrue(logs.contains(expectedPortNumber),
        "Expected port number is not present in the Selenium logs. Expected port number is: " + expectedPortNumber);
      ltLogger.error("Port number not found in the Selenium logs or Debug level logs are missing.");
    }
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private String getPortNumberRegex(String browserName, boolean isWebDriverEnabled) {
    if (isWebDriverEnabled) {
      return browserName.equalsIgnoreCase("firefox") ? "Listening on 127\\.0\\.0\\.1:(\\d+)" : "on port (\\d+)";
    } else {
      return browserName.equalsIgnoreCase("firefox") ?
        "Listening on 127\\.0\\.0\\.1:(\\d+)" :
        "Host: 127\\.0\\.0\\.1:(\\d+)";
    }
  }

  private String extractPortNumberFromSystemLogs(String text, String regex) {
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(text);
    return matcher.find() ? matcher.group(1) : null;
  }

  private boolean isWebDriverVerboseLoggingEnabled(String session_id, Map<String, Object> testCaps) {
    String webDriverVerboseLoggingEnabledFlag = "ml_verbose_webdriver_logging";
    String flagValue = automationAPIHelper.getFeatureFlagValueOfSpecificSession(session_id,
      webDriverVerboseLoggingEnabledFlag);
    return Boolean.parseBoolean(flagValue) || Boolean.parseBoolean(
      testCaps.getOrDefault(VERBOSE_WEBDRIVER_LOGGING, "false").toString());
  }

  private void checkForSpecificTestVerificationDataPresentInLogs(String logs, String logsType,
    testVerificationDataKeys[] expectedDataKeys) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    Arrays.stream(expectedDataKeys).sequential().forEach(key -> {
      Queue<String> expectedData = (Queue<String>) TEST_VERIFICATION_DATA.get().get(key);
      String dataType = key.toString();
      ltLogger.info("Checking for the following {} in {} logs: {}", dataType, logsType, expectedData);
      expectedData.forEach(expectedValue -> {
        if (logsType.contains(NETWORK) && key.equals(testVerificationDataKeys.URL))
          expectedValue = removeBasicAuthHeadersFromUrl(expectedValue);
        boolean isPresent = logs.contains(expectedValue);
        softAssert.assertTrue(isPresent, expectedValue + " is not present in the " + logsType + " logs.");
        ltLogger.info("{} '{}' {} present in the {} logs.", dataType, expectedValue, isPresent ? "is" : "is not",
          logsType);
      });
    });
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void verifyWebDriverLogs(String session_id, String logs, Map<String, Object> testCaps) {
    boolean isWebDriverVerboseLoggingEnabled = isWebDriverVerboseLoggingEnabled(session_id, testCaps);
    if (isWebDriverVerboseLoggingEnabled) {
      checkForSpecificTestVerificationDataPresentInLogs(logs, "webdriver",
        new testVerificationDataKeys[] { testVerificationDataKeys.LOCATORS, testVerificationDataKeys.URL });
    }
  }

  private String extractSeleniumVersionFromSeleniumLogs(String logs, boolean isSeleniumFourUsed) {
    String retrievedVersionFromSeleniumLogs = "";
    String retrievedVersionContainingStringFromSeleniumLogs = "";
    Pattern pattern = isSeleniumFourUsed ?
      Pattern.compile(seleniumFourExpectedLogLine + "[\\d.]+") :
      Pattern.compile(seleniumThreeExpectedLogLine + "([\\d\\.]+)'");
    Matcher matcher = pattern.matcher(logs);
    if (matcher.find()) {
      retrievedVersionContainingStringFromSeleniumLogs = isSeleniumFourUsed ? matcher.group(0) : matcher.group(1);
    }
    Pattern versionPattern = Pattern.compile("\\d+\\.\\d+\\.\\d+");
    Matcher versionPatternMatcher = versionPattern.matcher(retrievedVersionContainingStringFromSeleniumLogs);
    if (versionPatternMatcher.find()) {
      retrievedVersionFromSeleniumLogs = versionPatternMatcher.group(0).trim();
    }
    return retrievedVersionFromSeleniumLogs;
  }

  private String verifySeleniumVersion(String logs, Map<String, Object> testCaps) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    ComparableVersion defaultSeleniumVersion = new ComparableVersion("3.13.0");
    ComparableVersion expectedSeleniumVersion;
    String givenSeleniumVersionString = testCaps.getOrDefault(SELENIUM_VERSION, "default").toString();
    ltLogger.info("Used Selenium Version in test caps: {}", givenSeleniumVersionString);
    if (givenSeleniumVersionString.contains("latest")) {
      String browserName = testCaps.get(BROWSER_NAME).toString();
      String browserVersion = testCaps.get(BROWSER_VERSION).toString();
      String templateName = testCaps.get(PLATFORM_NAME).toString();
      givenSeleniumVersionString = automationAPIHelper.getSeleniumVersionBasedOnKeyWord(givenSeleniumVersionString,
        browserName, browserVersion, templateName);
      expectedSeleniumVersion = new ComparableVersion(givenSeleniumVersionString);
    } else
      //      Either use the given numeric value of selenium_version in test caps or use the default version
      expectedSeleniumVersion = givenSeleniumVersionString.equals("default") ?
        defaultSeleniumVersion :
        new ComparableVersion(givenSeleniumVersionString);
    boolean isSeleniumFourUsed = expectedSeleniumVersion.compareTo(new ComparableVersion("4.0.0")) >= 0;
    ltLogger.info("Using Selenium Version: {} and selenium four used status: {}", expectedSeleniumVersion,
      isSeleniumFourUsed);
    String actualSeleniumVersionFromSeleniumLogs = extractSeleniumVersionFromSeleniumLogs(logs, isSeleniumFourUsed);
    if (!actualSeleniumVersionFromSeleniumLogs.isEmpty()) {
      ComparableVersion actualSeleniumVersion = new ComparableVersion(actualSeleniumVersionFromSeleniumLogs);
      softAssert.assertTrue(expectedSeleniumVersion.equals(actualSeleniumVersion),
        "Expected Selenium version is " + expectedSeleniumVersion + " but found " + actualSeleniumVersion);
    } else {
      ltLogger.info("Unable to extract selenium version from Selenium Logs");
      String expectedLogs = (isSeleniumFourUsed ?
        seleniumFourExpectedLogLine :
        seleniumThreeExpectedLogLine) + expectedSeleniumVersion;
      softAssert.assertTrue(logs.contains(expectedLogs), "Selenium logs does not contain " + expectedLogs);
    }
    EnvSetup.SOFT_ASSERT.set(softAssert);
    return actualSeleniumVersionFromSeleniumLogs;
  }

  private void verifyLogLevelOfSystemLogs(String logs, String seleniumVersionString, String session_id) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    ComparableVersion seleniumVersion = new ComparableVersion(seleniumVersionString);
    ComparableVersion thresholdVersionForLegacySeleniumFourLogs = new ComparableVersion("4.28.0");
    ComparableVersion thresholdVersionForSeleniumFour = new ComparableVersion("4.0.0");

    if (seleniumVersion.compareTo(thresholdVersionForSeleniumFour) < 0) {
      checkForSpecificTestVerificationDataPresentInLogs(logs, "selenium 3",
        new testVerificationDataKeys[] { testVerificationDataKeys.LOCATORS, testVerificationDataKeys.URL });
    } else if (seleniumVersion.compareTo(thresholdVersionForLegacySeleniumFourLogs) < 0) {
      verifyLogsForOlderVersions(logs, seleniumVersionString, session_id, softAssert);
    } else {
      verifyLogsForNewerVersions(logs, seleniumVersionString, session_id, softAssert);
    }

    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void verifyLogsForOlderVersions(String logs, String seleniumVersionString, String session_id,
    CustomSoftAssert softAssert) {
    final String LOG_LEVEL = "\"log-level\": \"";
    final String INFO = LOG_LEVEL + "INFO\"";
    final String DEBUG = LOG_LEVEL + "DEBUG\"";
    softAssert.assertTrue(logs.contains(INFO),
      "Info level logs are missing for Selenium version " + seleniumVersionString);
    softAssert.assertTrue(logs.contains(DEBUG),
      "Debug level logs are missing for Selenium version " + seleniumVersionString);
    softAssert.assertTrue(logs.contains("DELETE /wd/hub/session/" + session_id),
      "Selenium " + seleniumVersionString + " logs are incomplete.");
    ltLogger.info("Debug level Selenium logs have been checked for selenium version: {}", seleniumVersionString);
  }

  private void verifyLogsForNewerVersions(String logs, String seleniumVersionString, String session_id,
    CustomSoftAssert softAssert) {
    final String STARTED_SELENIUM = "Started Selenium Standalone ";
    final String SESSION_CREATED_NODE = "Session created by the Node. Id: ";
    final String SESSION_CREATED_DISTRIBUTOR = "Session created by the Distributor. Id: ";
    final String[] SESSION_DELETION_LOGS = { "Deleted session from local Session Map, Id: ",
      "Releasing slot for session id ", "Stopping session " };
    String expectedLogString = STARTED_SELENIUM + seleniumVersionString;
    softAssert.assertTrue(logs.contains(expectedLogString),
      "Expected log 'Started Selenium Standalone' missing for Selenium " + seleniumVersionString);
    expectedLogString = SESSION_CREATED_NODE + session_id;
    softAssert.assertTrue(logs.contains(expectedLogString),
      "Session creation log with correct session ID missing. Expected logs: " + expectedLogString);
    expectedLogString = SESSION_CREATED_DISTRIBUTOR + session_id;
    softAssert.assertTrue(logs.contains(expectedLogString),
      "Distributor session creation log with correct session ID missing. Expected logs: " + expectedLogString);

    for (String expectedLog : SESSION_DELETION_LOGS) {
      expectedLogString = expectedLog + session_id;
      softAssert.assertTrue(logs.contains(expectedLogString),
        "Expected session log missing or incorrect session ID: " + expectedLogString);
    }
  }

  private void verifySeleniumLogs(String session_id, String logs, Map<String, Object> testCaps) {
    logs = logs.replace("\\\"", "\"");
    String seleniumVersion = verifySeleniumVersion(logs, testCaps);
    session_id = automationAPIHelper.getSessionIDFromTestId(session_id);
    verifyLogLevelOfSystemLogs(logs, seleniumVersion, session_id);
  }

  public void verifySystemLogs(String logsType, String session_id) {
    boolean isWebDriverEnabled = logsType.equalsIgnoreCase("webdriver");
    Map<String, Object> testCaps = EnvSetup.TEST_CAPS_MAP.get();
    String browserName = testCaps.get(BROWSER_NAME).toString();
    for (ArtefactAPIVersions artefactAPIVersion : ArtefactAPIVersions.values()) {
      String version = artefactAPIVersion.equals(ArtefactAPIVersions.API_V1) ? "v1" : "v2";
      String logs = fetchLogs(logsType, artefactAPIVersion, session_id);
      ltLogger.info("Selenium Logs from API {}: {}", version, logs);
      verifyPortNumber(session_id, logs, isWebDriverEnabled, browserName);
      if (isWebDriverEnabled) {
        verifyWebDriverLogs(session_id, logs, testCaps);
      } else {
        verifySeleniumLogs(session_id, logs, testCaps);
      }
    }
  }

  @SneakyThrows
  private Queue<String> extractUrlsAPIResponse(JsonArray response) {
    Queue<String> urlsAPIResponse = new LinkedList<>();
    for (JsonElement element : response) {
      String requestPath = element.getAsJsonObject().get("Value").getAsJsonObject().get("requestPath").getAsString();
      String method = element.getAsJsonObject().get("Value").getAsJsonObject().get("requestMethod").getAsString();
      if (method.equals("POST") && requestPath.endsWith("/url")) {
        String requestBody = element.getAsJsonObject().get("Value").getAsJsonObject().get("requestBody").getAsString();
        ltLogger.info("Request body: {}", requestBody);
        JsonNode jsonNode = new ObjectMapper().readTree(requestBody);
        urlsAPIResponse.add(jsonNode.get("url").asText());
      }
    }
    ltLogger.info("URLs API response: {}", urlsAPIResponse);
    return urlsAPIResponse;
  }

  private void verifyExpectedUrlsArePresentWithSpecificSequence(Queue<String> fetchedData, String logsSource,
    CustomSoftAssert softAssert) {
    Queue<String> expectedData = (Queue<String>) TEST_VERIFICATION_DATA.get().get(testVerificationDataKeys.URL);
    Assert.assertNotNull(expectedData, "Test data for verifying artefacts is null");
    Queue<String> expectedDataClone = new LinkedList<>(expectedData);
    ltLogger.info("Verifying expected urls from {} to {} for log source {}", fetchedData, expectedDataClone,
      logsSource);
    softAssert.assertTrue(fetchedData.size() == expectedDataClone.size(),
      "Number of urls present in the " + logsSource + " logs are not same. Expected: " + expectedDataClone.size() + ", Actual: " + fetchedData.size());
    while (!expectedDataClone.isEmpty() && !fetchedData.isEmpty() && fetchedData.size() == expectedDataClone.size()) {
      String actualUrl = fetchedData.remove();
      String expectedUrl = expectedDataClone.remove();
      softAssert.assertTrue(expectedUrl.equals(actualUrl),
        "Mismatch found in " + logsSource + ". Expected URL: " + expectedUrl + " but got URL: " + actualUrl);
    }
  }

  public void verifyCommandLogs(String session_id) {
    if (EnvSetup.SESSION_COMMAND_LOGS_COUNT_FROM_TEST_API.get() == null) {
      automationAPIHelper.getCommandCounts(session_id);
    }
    int expectedCommandLogsCount = EnvSetup.SESSION_COMMAND_LOGS_COUNT_FROM_TEST_API.get();
    this.verifyDifferentCommandLogs(session_id, ArtefactAPIVersions.API_V1, COMMAND_LOGS_API_V1_SCHEMA,
      expectedCommandLogsCount, LogType.COMMAND);
    this.verifyDifferentCommandLogs(session_id, ArtefactAPIVersions.API_V2, COMMAND_LOGS_API_V2_SCHEMA,
      expectedCommandLogsCount, LogType.COMMAND);
  }

  public void exceptionCommandLogs(String session_id) {
    if (EnvSetup.SESSION_EXCEPTION_LOGS_COUNT_FROM_TEST_API.get() == null) {
      automationAPIHelper.getCommandCounts(session_id);
    }
    int expectedExceptionCommandLogsCount = EnvSetup.SESSION_EXCEPTION_LOGS_COUNT_FROM_TEST_API.get();
    this.verifyDifferentCommandLogs(session_id, ArtefactAPIVersions.API_V1, COMMAND_LOGS_API_V1_SCHEMA,
      expectedExceptionCommandLogsCount, LogType.EXCEPTION);
    this.verifyDifferentCommandLogs(session_id, ArtefactAPIVersions.API_V2, COMMAND_LOGS_API_V2_SCHEMA,
      expectedExceptionCommandLogsCount, LogType.EXCEPTION);
  }

  private void verifyDifferentCommandLogs(String session_id, ArtefactAPIVersions apiVersion, String schemaFilePath,
    int expectedCommandLogsCount, LogType logType) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    String logsFromApi = fetchLogs(logType.value, apiVersion, session_id);

    // Validate schema
    Set<String> schemaValidationErrors = validateSchema(logsFromApi, schemaFilePath);
    softAssert.assertTrue(schemaValidationErrors.isEmpty(),
      "Schema validation failed for " + logType.value + " logs from " + apiVersion.toString() + ". Errors: " + schemaValidationErrors);

    // Parse logs JSON
    JsonElement logsJson = constructJsonFromString(logsFromApi);
    JsonArray commandsArray = apiVersion == ArtefactAPIVersions.API_V1 ?
      logsJson.getAsJsonObject().get("data").getAsJsonArray() :
      logsJson.getAsJsonArray();

    // Verify logs count
    softAssert.assertTrue(commandsArray.size() == expectedCommandLogsCount,
      logType.value + " logs count fetched from " + apiVersion + " doesn't match. Expected: " + expectedCommandLogsCount + ", Actual: " + commandsArray.size());

    // Specific verifications based on log type
    if (logType == LogType.EXCEPTION) {
      checkForSpecificTestVerificationDataPresentInLogs(logsFromApi, "exception command",
        new testVerificationDataKeys[] { testVerificationDataKeys.EXCEPTION_LOG });
    } else if (logType == LogType.COMMAND) {
      Queue<String> fetchedUrlsFromCommandLogs = extractUrlsAPIResponse(commandsArray);
      verifyExpectedUrlsArePresentWithSpecificSequence(fetchedUrlsFromCommandLogs, logType.value + apiVersion,
        softAssert);
    }

    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  public void verifyConsoleLogs(String session_id) {
    String browserName = TEST_CAPS_MAP.get().get(BROWSER_NAME).toString();
    if (!"chrome".equals(browserName)) {
      ltLogger.warn("Console logs verification is not valid for browser: {}", browserName);
      return;
    }

    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    ArrayList<String> expectedConsoleLogs = (ArrayList<String>) TEST_VERIFICATION_DATA.get()
      .get(testVerificationDataKeys.CONSOLE_LOG);
    softAssert.assertFalse(expectedConsoleLogs == null || expectedConsoleLogs.isEmpty(),
      "Expected logs to verify console logs are missing.");
    if (expectedConsoleLogs == null || expectedConsoleLogs.isEmpty()) {
      return;
    }

    for (ArtefactAPIVersions artefactAPIVersion : ArtefactAPIVersions.values()) {
      String version = artefactAPIVersion.equals(ArtefactAPIVersions.API_V1) ? "v1" : "v2";
      String logs = fetchLogs(LogType.CONSOLE.value, artefactAPIVersion, session_id);

      for (String expectedConsoleLog : expectedConsoleLogs) {
        ltLogger.info("Checking console log {}", expectedConsoleLog);
        softAssert.assertTrue(logs.contains(expectedConsoleLog),
          "Expected log: " + expectedConsoleLog + " is missing from the console logs fetched from API version: " + version);
      }
    }

    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  public void verifyTerminalLogs(String session_id) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    String expectedData = TEST_VERIFICATION_DATA.get().getOrDefault(testVerificationDataKeys.TERMINAL_LOG, "")
      .toString();
    softAssert.assertFalse(StringUtils.isNullOrEmpty(expectedData),
      "Expected terminal logs data is empty, please upload terminal logs before verifying terminal logs");
    if (!StringUtils.isNullOrEmpty(expectedData)) {
      for (ArtefactAPIVersions artefactAPIVersion : ArtefactAPIVersions.values()) {
        String version = artefactAPIVersion.equals(ArtefactAPIVersions.API_V1) ? "v1" : "v2";
        String logs = fetchLogs(LogType.TERMINAL.value, artefactAPIVersion, session_id);
        softAssert.assertTrue(logs.contains(expectedData),
          "Terminal logs data doesn't match for the logs data fetched from API version: " + version);
      }
    }
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  public void verifyNetworkLogs(String session_id) {
    for (ArtefactAPIVersions artefactAPIVersion : ArtefactAPIVersions.values()) {
      String logs = fetchLogs(LogType.NETWORK.value, artefactAPIVersion, session_id);
      checkForSpecificTestVerificationDataPresentInLogs(logs, "network",
        new testVerificationDataKeys[] { testVerificationDataKeys.URL });
    }
  }

  public void verifyNetworkFullHarLogs(String session_id) {
    for (ArtefactAPIVersions artefactAPIVersion : ArtefactAPIVersions.values()) {
      String logs = fetchLogs(LogType.FULL_HAR.value, artefactAPIVersion, session_id);
      checkForSpecificTestVerificationDataPresentInLogs(logs, "network full.har",
        new testVerificationDataKeys[] { testVerificationDataKeys.URL });
    }
  }

  private String[] extractVideoUrlsFromAPIResponse(String session_id, SoftAssert softAssert) {
    String videoAPIResponse = fetchLogs(VIDEO, ArtefactAPIVersions.API_V1, session_id);
    FetchVideoAPIResponseDTO fetchVideoAPIResponseDTO = convertJsonStringToPojo(videoAPIResponse,
      new TypeToken<FetchVideoAPIResponseDTO>() {
      });
    String status = fetchVideoAPIResponseDTO.getStatus();
    String message = fetchVideoAPIResponseDTO.getMessage();
    softAssert.assertTrue(status.equals("success"),
      "Unable to extract video urls from API response. Status: " + status + " Message: " + message);
    if (status.equals("success")) {
      String shareableVideoUrl = fetchVideoAPIResponseDTO.getView_video_url();
      String videoDownloadUrl = fetchVideoAPIResponseDTO.getUrl();
      ltLogger.info("Video download url: {}, video share url: {}", videoDownloadUrl, shareableVideoUrl);
      return new String[] { shareableVideoUrl, videoDownloadUrl };
    }
    return null;
  }

  private void verifyVideoMetaData(String completeFilePath, Map<String, Object> testCaps, CustomSoftAssert softAssert) {
    // Extract video metadata
    Map<String, Object> videoMetaData = extractMetaDataOfSpecificVideoFile(completeFilePath);
    ltLogger.info("Extracted video metadata: {}", videoMetaData);

    // Check if the video file is valid
    if (videoMetaData == null) {
      softAssert.fail("Extracted video metadata is null. Possible cause: Downloaded video file is corrupted.");
      return;
    }

    // Verify Resolution
    if (testCaps.containsKey(RESOLUTION)) {
      verifyResolution(videoMetaData, testCaps, softAssert);
    }

    // Verify Duration
    verifyDuration(videoMetaData, softAssert);
  }

  private void verifyResolution(Map<String, Object> videoMetaData, Map<String, Object> testCaps,
    CustomSoftAssert softAssert) {
    String actualResolution = videoMetaData.get(videoMetadataTypes.RESOLUTION.getValue()).toString();
    String expectedResolution = testCaps.get(RESOLUTION).toString();

    ltLogger.info("Actual video resolution: {}. Expected video resolution: {}", actualResolution, expectedResolution);

    if (!actualResolution.equals(expectedResolution)) {
      String[] expectedDimensions = expectedResolution.split("x");
      String[] actualDimensions = actualResolution.split("x");

      String expectedWidth = expectedDimensions[0];
      String expectedHeight = expectedDimensions[1];
      String actualWidth = actualDimensions[0];
      String actualHeight = actualDimensions[1];

      softAssert.assertTrue(Integer.parseInt(actualWidth) >= Integer.parseInt(expectedWidth),
        "Actual video width is not greater than expected width. Expected: " + expectedWidth + ", Actual: " + actualWidth);
      softAssert.assertTrue(Integer.parseInt(actualHeight) >= Integer.parseInt(expectedHeight),
        "Actual video height is not greater than expected height. Expected: " + expectedHeight + ", Actual: " + actualHeight);
    }
  }

  private void verifyDuration(Map<String, Object> videoMetaData, CustomSoftAssert softAssert) {
    int bufferTime = 60; // Buffer time in seconds
    double testExecutionTimeInSeconds = Double.parseDouble((String) TEST_REPORT.get().get(TEST_EXECUTION_TIME));
    double expectedVideoDurationLimit = testExecutionTimeInSeconds + bufferTime;

    double actualVideoDuration = Double.parseDouble(
      videoMetaData.get(videoMetadataTypes.DURATION_IN_SECONDS.getValue()).toString());

    ltLogger.info("Actual video duration: {}. Expected video duration limit: {}", actualVideoDuration,
      expectedVideoDurationLimit);

    softAssert.assertTrue(actualVideoDuration < expectedVideoDurationLimit,
      "Test video duration is greater than the expected video duration [1min + test execution time]. Expected duration: " + expectedVideoDurationLimit + ", Actual: " + actualVideoDuration);
  }

  public void verifyTestVideo(String session_id) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    String[] videoUrls = extractVideoUrlsFromAPIResponse(session_id, softAssert);
    Map<String, Object> testCaps = EnvSetup.TEST_CAPS_MAP.get();
    if (videoUrls != null) {
      String videoDownloadUrl = videoUrls[1];
      String videoShareUrl = videoUrls[0];
      String videoFileName = TEST_SESSION_ID.get() + "_" + System.currentTimeMillis() + ".mp4";
      boolean isVideoDownloadSuccess = downloadFile(videoDownloadUrl, videoFileName, TEST_LOGS_DOWNLOAD_DIRECTORY);
      softAssert.assertTrue(isVideoDownloadSuccess,
        "Unable to download video file for the session with name: " + videoFileName);
      if (isVideoDownloadSuccess) {
        String completeFilePath = TEST_LOGS_DOWNLOAD_DIRECTORY + videoFileName;
        verifyVideoMetaData(completeFilePath, testCaps, softAssert);
        int statusCodeOfShareVideoUrl = getRequest(videoShareUrl).statusCode();
        softAssert.assertTrue(statusCodeOfShareVideoUrl == 200, "Video share url is not valid. Url: " + videoShareUrl);
      }
    }
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  public void verifyPerformanceReport(String session_id) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    String uri = constructAPIUrlWithBasicAuth(EnvSetup.API_URL_BASE, SESSION_LIGHTHOUSE_REPORT_ENDPOINT,
      EnvSetup.testUserName.get(), EnvSetup.testAccessKey.get(), session_id);
    ltLogger.info("URI to fetch lighthouse reports: {}", uri);
    String response = getRequestAsString(uri);
    LighthouseReportDTO lighthouseReportDTO = convertJsonStringToPojo(response, new TypeToken<LighthouseReportDTO>() {
    });
    String status = lighthouseReportDTO.getStatus();
    String message = lighthouseReportDTO.getMessage();
    softAssert.assertTrue(lighthouseReportDTO.getStatus().equalsIgnoreCase("success"),
      "Unable to fetch lighthouse reports. Status: " + status + ", Message: " + message);
    if (status.equalsIgnoreCase("success")) {
      String jsonReport = lighthouseReportDTO.getData().getJson_report();
      String htmlReport = lighthouseReportDTO.getData().getHtml_report();
      int jsonReportFetchStatusCode = getRequest(jsonReport).statusCode();
      int htmlReportFetchStatusCode = getRequest(htmlReport).statusCode();
      ltLogger.info("JSON report status: {} and HTML report status: {}", jsonReportFetchStatusCode,
        htmlReportFetchStatusCode);
      softAssert.assertTrue(jsonReportFetchStatusCode == 200,
        "Unable to download Lighthouse report (JSON). Status: " + jsonReportFetchStatusCode);
      softAssert.assertTrue(htmlReportFetchStatusCode == 200,
        "Unable to download Lighthouse report (HTML). Status: " + htmlReportFetchStatusCode);
    }
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }
}
