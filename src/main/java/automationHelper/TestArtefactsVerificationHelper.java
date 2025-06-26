package automationHelper;

import DTOs.Others.TestCommandPagesListApiResponseDTO;
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
import utility.CustomSoftAssert;
import utility.EnvSetup;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static factory.SoftAssertionMessages.*;
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

    @Getter
    private enum LogType {
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
                softAssertMessageFormat(UNABLE_TO_GET_LOGS_DOWNLOAD_URL_FROM_API_V2_ERROR_MESSAGE, message));

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
            softAssert.fail(softAssertMessageFormat(UNABLE_TO_VERIFY_PORT_NUMBER_FROM_SYSTEM_LOGS_ERROR_MESSAGE, logs));
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
                    softAssertMessageFormat(WEBDRIVER_MODE_PORT_MISMATCH_ERROR_IN_SYSTEM_LOGS_ERROR_MESSAGE, PORT_WEBDRIVER,
                            portNumber) :
                    softAssertMessageFormat(SELENIUM_MODE_PORT_MISMATCH_ERROR_IN_SYSTEM_LOGS_ERROR_MESSAGE, PORT_SELENIUM,
                            portNumber);

            softAssert.assertEquals(portNumber, expectedPortNumber, expectedMessage);

            ltLogger.info("Used Port: {} for session: {}", portNumber, sessionId);
        } else {
            softAssert.assertTrue(logs.contains(expectedPortNumber),
                    softAssertMessageFormat(EXPECTED_PORT_NUMBER_NOT_FOUND_IN_SYSTEM_LOGS_ERROR_MESSAGE, expectedPortNumber));
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
        if (logs == null || logs.isEmpty()) {
            softAssert.fail(softAssertMessageFormat(RECEIVED_LOGS_FROM_API_IS_NULL_OR_EMPTY_ERROR_MESSAGE, logsType, logs));
            EnvSetup.SOFT_ASSERT.set(softAssert);
            return;
        }
        Arrays.stream(expectedDataKeys).sequential().forEach(key -> {
            Queue<String> expectedData = (Queue<String>) TEST_VERIFICATION_DATA.get().get(key);
            String dataType = key.toString();
            ltLogger.info("Checking for the following {} in {} logs: {}", dataType, logsType, expectedData);
            expectedData.forEach(expectedValue -> {
                if (logsType.contains(NETWORK) && key.equals(testVerificationDataKeys.URL))
                    expectedValue = removeBasicAuthHeadersFromUrl(expectedValue);
                boolean isPresent = logs.contains(expectedValue);
                softAssert.assertTrue(isPresent,
                        softAssertMessageFormat(EXPECTED_DATA_IS_NOT_PRESENT_IN_LOGS_API_RESPONSE_ERROR_MESSAGE, expectedValue,
                                logsType));
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
                    new testVerificationDataKeys[]{testVerificationDataKeys.LOCATORS, testVerificationDataKeys.URL});
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
        ComparableVersion defaultSeleniumVersion = new ComparableVersion(DEFAULT_SELENIUM_VERSION);
        ComparableVersion expectedSeleniumVersion;
        String givenSeleniumVersionString = testCaps.getOrDefault(SELENIUM_VERSION,
                        testCaps.getOrDefault(SELENIUM_CDP, "false").equals("true") ? SELENIUM_4_VERSION_FLOOR_VALUE : "default")
                .toString();
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
        boolean isSeleniumFourUsed = expectedSeleniumVersion.compareTo(
                new ComparableVersion(SELENIUM_4_VERSION_FLOOR_VALUE)) >= 0;
        ltLogger.info("Using Selenium Version: {} and selenium four used status: {}", expectedSeleniumVersion,
                isSeleniumFourUsed);
        String actualSeleniumVersionFromSeleniumLogs = extractSeleniumVersionFromSeleniumLogs(logs, isSeleniumFourUsed);
        if (!actualSeleniumVersionFromSeleniumLogs.isEmpty()) {
            ComparableVersion actualSeleniumVersion = new ComparableVersion(actualSeleniumVersionFromSeleniumLogs);
            softAssert.assertTrue(expectedSeleniumVersion.equals(actualSeleniumVersion),
                    softAssertMessageFormat(SELENIUM_VERSION_MISMATCH_ERROR_MESSAGE, expectedSeleniumVersion,
                            actualSeleniumVersion));
        } else {
            ltLogger.info("Unable to extract selenium version from Selenium Logs");
            String expectedLogs = (isSeleniumFourUsed ?
                    seleniumFourExpectedLogLine :
                    seleniumThreeExpectedLogLine) + expectedSeleniumVersion;
            softAssert.assertTrue(logs.contains(expectedLogs),
                    softAssertMessageFormat(SELENIUM_VERSION_MISMATCH_FALLBACK_ERROR_MESSAGE, expectedLogs));
        }
        EnvSetup.SOFT_ASSERT.set(softAssert);
        return actualSeleniumVersionFromSeleniumLogs;
    }

    private void verifyLogLevelOfSystemLogs(String logs, String seleniumVersionString, String session_id) {
        CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
        ComparableVersion seleniumVersion = new ComparableVersion(seleniumVersionString);
        ComparableVersion thresholdVersionForLegacySeleniumFourLogs = new ComparableVersion(
                SELENIUM_4_VERSION_FLOOR_VALUE_FOR_LEGACY_LOGS);
        ComparableVersion thresholdVersionForSeleniumFour = new ComparableVersion(SELENIUM_4_VERSION_FLOOR_VALUE);

        if (seleniumVersion.compareTo(thresholdVersionForSeleniumFour) < 0) {
            checkForSpecificTestVerificationDataPresentInLogs(logs, "selenium 3",
                    new testVerificationDataKeys[]{testVerificationDataKeys.LOCATORS, testVerificationDataKeys.URL});
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
                softAssertMessageFormat(LEGACY_SELENIUM_4_LOGS_ERROR_MESSAGE_1, seleniumVersionString));
        softAssert.assertTrue(logs.contains(DEBUG),
                softAssertMessageFormat(LEGACY_SELENIUM_4_LOGS_ERROR_MESSAGE_2, seleniumVersionString));
        softAssert.assertTrue(logs.contains("DELETE /wd/hub/session/" + session_id),
                softAssertMessageFormat(LEGACY_SELENIUM_4_LOGS_ERROR_MESSAGE_3, seleniumVersionString));
        ltLogger.info("Debug level Selenium logs have been checked for selenium version: {}", seleniumVersionString);
    }

    private void verifyLogsForNewerVersions(String logs, String seleniumVersionString, String session_id,
                                            CustomSoftAssert softAssert) {
        final String STARTED_SELENIUM = "Started Selenium Standalone ";
        final String SESSION_CREATED_NODE = "Session created by the Node. Id: ";
        final String SESSION_CREATED_DISTRIBUTOR = "Session created by the Distributor. Id: ";
        final String[] SESSION_DELETION_LOGS = {"Deleted session from local Session Map, Id: ",
                "Releasing slot for session id ", "Stopping session "};
        String expectedLogString = STARTED_SELENIUM + seleniumVersionString;
        softAssert.assertTrue(logs.contains(expectedLogString),
                softAssertMessageFormat(NEW_SELENIUM_4_LOGS_ERROR_MESSAGE_1, seleniumVersionString));
        expectedLogString = SESSION_CREATED_NODE + session_id;
        softAssert.assertTrue(logs.contains(expectedLogString),
                softAssertMessageFormat(NEW_SELENIUM_4_LOGS_ERROR_MESSAGE_2, expectedLogString));
        expectedLogString = SESSION_CREATED_DISTRIBUTOR + session_id;
        softAssert.assertTrue(logs.contains(expectedLogString),
                softAssertMessageFormat(NEW_SELENIUM_4_LOGS_ERROR_MESSAGE_3, expectedLogString));

        for (String expectedLog : SESSION_DELETION_LOGS) {
            expectedLogString = expectedLog + session_id;
            softAssert.assertTrue(logs.contains(expectedLogString),
                    softAssertMessageFormat(NEW_SELENIUM_4_LOGS_ERROR_MESSAGE_4, expectedLogString));
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
                softAssertMessageFormat(CHECK_EXPECTED_URLS_PRESENT_ERROR_MESSAGE_1, logsSource, expectedDataClone.size(),
                        fetchedData.size()));
        while (!expectedDataClone.isEmpty() && !fetchedData.isEmpty() && fetchedData.size() == expectedDataClone.size()) {
            String actualUrl = fetchedData.remove();
            String expectedUrl = expectedDataClone.remove();
            softAssert.assertTrue(expectedUrl.equals(actualUrl),
                    softAssertMessageFormat(CHECK_EXPECTED_URLS_PRESENT_ERROR_MESSAGE_2, logsSource, expectedUrl, actualUrl));
        }
    }

    public void verifyCommandLogs(String session_id) {
        if (EnvSetup.SESSION_COMMAND_LOGS_COUNT_FROM_TEST_API.get() == null) {
            automationAPIHelper.getCommandCounts(session_id);
        }
        int expectedCommandLogsCount = EnvSetup.SESSION_COMMAND_LOGS_COUNT_FROM_TEST_API.get();
        this.verifyCommandLogsPagesListAndTimeStamps(session_id, expectedCommandLogsCount);
        this.verifyDifferentCommandLogs(session_id, ArtefactAPIVersions.API_V1, COMMAND_LOGS_API_V1_SCHEMA,
                expectedCommandLogsCount, LogType.COMMAND);
        this.verifyDifferentCommandLogs(session_id, ArtefactAPIVersions.API_V2, COMMAND_LOGS_API_V2_SCHEMA,
                expectedCommandLogsCount, LogType.COMMAND);
    }

    private void verifyCommandLogsPagesListAndTimeStamps(String session_id, int expectedCommandLogsCount,
                                                         String... customTestType) {
        CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();

        // Determine test type (default to "desktop")
        String testType = (customTestType.length > 0) ? customTestType[0] : "desktop";

        final String flagForRequestPageSize = "requestPageSize";

        // Fetch necessary values
        int commandsCountPerPage = Integer.parseInt(
                automationAPIHelper.getFeatureFlagValueOfSpecificSession(session_id, flagForRequestPageSize));
        int expectedNumberOfPages = (int) Math.ceil((double) expectedCommandLogsCount / commandsCountPerPage);
        String testStartTimeStamp = automationAPIHelper.getTestCreateTimeStampFromSessionId(session_id);
        String testID = automationAPIHelper.getTestIdFromSessionId(session_id);
        String orgID = automationAPIHelper.getOrgIDFromTestId(session_id);
        String testDate = testStartTimeStamp.split(" ")[0];

        // Construct API URL to fetch command logs pages list
        String uriToFetchCommandLogsPagesList = constructAPIUrl(EnvSetup.API_URL_BASE, COMMANDS_PAGES_LIST_API_ENDPOINT,
                testID, String.format("/request?testDate=%s&testType=%s&orgId=%s&commandLogv2=true", testDate, testType, orgID));

        ltLogger.info("Command logs pages list API URL: {}", uriToFetchCommandLogsPagesList);

        String commandListApiResponseString = getRequestWithBasicAuthAsString(uriToFetchCommandLogsPagesList,
                EnvSetup.testUserName.get(), EnvSetup.testAccessKey.get());

        if (commandListApiResponseString != null && !commandListApiResponseString.isEmpty()) {
            TestCommandPagesListApiResponseDTO commandLogsPagesListApiResponse = convertJsonStringToPojo(
                    commandListApiResponseString, new TypeToken<TestCommandPagesListApiResponseDTO>() {
                    });

            // Validate the number of pages
            int actualPages = commandLogsPagesListApiResponse.getData().size();
            softAssert.assertTrue(actualPages == expectedNumberOfPages,
                    softAssertMessageFormat(COMMAND_LOGS_NO_OF_PAGES_MISMATCH_ERROR_MESSAGE, expectedCommandLogsCount,
                            commandsCountPerPage, expectedNumberOfPages, actualPages));

            // Validate the page start time for each log
            commandLogsPagesListApiResponse.getData().forEach(commandLogFile -> {
                String pageStartTime = commandLogFile.getPage_start_time();
                softAssert.assertFalse(StringUtils.isNullOrEmpty(pageStartTime),
                        softAssertMessageFormat(COMMAND_LOGS_PAGE_START_TIME_NULL_OR_EMPTY_ERROR_MESSAGE, commandLogFile.getName()));
            });
        } else {
            softAssert.fail(softAssertMessageFormat(NULL_OR_EMPTY_API_RESPONSE_ERROR_MESSAGE, uriToFetchCommandLogsPagesList,
                    commandListApiResponseString));
        }

        EnvSetup.SOFT_ASSERT.set(softAssert);
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

        // Handle null response
        if (logsFromApi == null || logsFromApi.isEmpty()) {
            softAssert.fail(softAssertMessageFormat(RECEIVED_NULL_COMMAND_LOGS_IN_API_RESPONSE_ERROR_MESSAGE, logType.value,
                    apiVersion.toString(), logsFromApi));
            EnvSetup.SOFT_ASSERT.set(softAssert);
            return;
        }

        // Validate schema
        Set<String> schemaValidationErrors = validateSchema(logsFromApi, schemaFilePath);
        softAssert.assertTrue(schemaValidationErrors.isEmpty(),
                softAssertMessageFormat(SCHEMA_VALIDATION_FAILURE_FOR_LOGS_API_RESPONSE_ERROR_MESSAGE, logType.value,
                        apiVersion.toString(), schemaValidationErrors));

        // Parse logs JSON
        JsonElement logsJson = constructJsonFromString(logsFromApi);
        JsonArray commandsArray = apiVersion == ArtefactAPIVersions.API_V1 ?
                logsJson.getAsJsonObject().get("data").getAsJsonArray() :
                logsJson.getAsJsonArray();

        // Verify logs count
        softAssert.assertTrue(commandsArray.size() == expectedCommandLogsCount,
                softAssertMessageFormat(LOGS_COUNT_MISMATCH_ERROR_MESSAGE, logType.value, apiVersion, expectedCommandLogsCount,
                        commandsArray.size()));

        // Specific verifications based on log type
        if (logType == LogType.EXCEPTION) {
            checkForSpecificTestVerificationDataPresentInLogs(logsFromApi, "exception command",
                    new testVerificationDataKeys[]{testVerificationDataKeys.EXCEPTION_LOG});
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
                softAssertMessageFormat(EXPECTED_CONSOLE_LOGS_DATA_NOT_AVAILABLE_ERROR_MESSAGE));
        if (expectedConsoleLogs == null || expectedConsoleLogs.isEmpty()) {
            return;
        }

        for (ArtefactAPIVersions artefactAPIVersion : ArtefactAPIVersions.values()) {
            String version = artefactAPIVersion.equals(ArtefactAPIVersions.API_V1) ? "v1" : "v2";
            String logs = fetchLogs(LogType.CONSOLE.value, artefactAPIVersion, session_id);

            for (String expectedConsoleLog : expectedConsoleLogs) {
                ltLogger.info("Checking console log {}", expectedConsoleLog);
                softAssert.assertTrue(logs.contains(expectedConsoleLog),
                        softAssertMessageFormat(EXPECTED_CONSOLE_LOGS_ARE_NOT_AVAILABLE_ERROR_MESSAGE, expectedConsoleLog, version));
            }
        }

        EnvSetup.SOFT_ASSERT.set(softAssert);
    }

    public void verifyTerminalLogs(String session_id) {
        CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
        String expectedData = TEST_VERIFICATION_DATA.get().getOrDefault(testVerificationDataKeys.TERMINAL_LOG, "")
                .toString();
        softAssert.assertFalse(StringUtils.isNullOrEmpty(expectedData),
                softAssertMessageFormat(TERMINAL_LOGS_NOT_UPLOADED_ERROR_MESSAGE));
        if (!StringUtils.isNullOrEmpty(expectedData)) {
            for (ArtefactAPIVersions artefactAPIVersion : ArtefactAPIVersions.values()) {
                String version = artefactAPIVersion.equals(ArtefactAPIVersions.API_V1) ? "v1" : "v2";
                String logs = fetchLogs(LogType.TERMINAL.value, artefactAPIVersion, session_id);
                softAssert.assertTrue(logs.contains(expectedData),
                        softAssertMessageFormat(TERMINAL_LOGS_DATA_MISMATCH_ERROR_MESSAGE, version));
            }
        }
        EnvSetup.SOFT_ASSERT.set(softAssert);
    }

    public void verifyNetworkLogs(String session_id) {
        for (ArtefactAPIVersions artefactAPIVersion : ArtefactAPIVersions.values()) {
            String logs = fetchLogs(LogType.NETWORK.value, artefactAPIVersion, session_id);
            checkForSpecificTestVerificationDataPresentInLogs(logs, "network",
                    new testVerificationDataKeys[]{testVerificationDataKeys.URL});
        }
    }

    public void verifyNetworkFullHarLogs(String session_id) {
        for (ArtefactAPIVersions artefactAPIVersion : ArtefactAPIVersions.values()) {
            String logs = fetchLogs(LogType.FULL_HAR.value, artefactAPIVersion, session_id);
            checkForSpecificTestVerificationDataPresentInLogs(logs, "network full.har",
                    new testVerificationDataKeys[]{testVerificationDataKeys.URL});
        }
    }

    private String[] extractVideoUrlsFromAPIResponse(String session_id, CustomSoftAssert softAssert) {
        String videoAPIResponse = fetchLogs(VIDEO, ArtefactAPIVersions.API_V1, session_id);
        FetchVideoAPIResponseDTO fetchVideoAPIResponseDTO = convertJsonStringToPojo(videoAPIResponse,
                new TypeToken<FetchVideoAPIResponseDTO>() {
                });
        String status = fetchVideoAPIResponseDTO.getStatus();
        String message = fetchVideoAPIResponseDTO.getMessage();
        softAssert.assertTrue(status.equals("success"),
                softAssertMessageFormat(UNABLE_TO_EXTRACT_VIDEO_URLS_ERROR_MESSAGE, status, message));
        if (status.equals("success")) {
            String shareableVideoUrl = fetchVideoAPIResponseDTO.getView_video_url();
            String videoDownloadUrl = fetchVideoAPIResponseDTO.getUrl();
            ltLogger.info("Video download url: {}, video share url: {}", videoDownloadUrl, shareableVideoUrl);
            return new String[]{shareableVideoUrl, videoDownloadUrl};
        }
        return null;
    }

    private void verifyVideoMetaData(String completeFilePath, Map<String, Object> testCaps, CustomSoftAssert softAssert,
                                     String testStatus) {
        // Extract video metadata
        Map<String, Object> videoMetaData = extractMetaDataOfSpecificVideoFile(completeFilePath);
        ltLogger.info("Extracted video metadata: {}", videoMetaData);

        // Check if the video file is valid
        if (videoMetaData == null) {
            softAssert.fail(softAssertMessageFormat(VIDEO_NOT_GENERATED_ERROR_MESSAGE));
            return;
        }

        // Verify Resolution
        if (testCaps.containsKey(RESOLUTION)) {
            verifyResolution(videoMetaData, testCaps, softAssert);
        }

        // Verify Duration
        verifyDuration(videoMetaData, softAssert, testStatus);
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
                    softAssertMessageFormat(VIDEO_RESOLUTION_MISMATCH_ERROR_MESSAGE_1, expectedWidth, actualWidth));
            softAssert.assertTrue(Integer.parseInt(actualHeight) >= Integer.parseInt(expectedHeight),
                    softAssertMessageFormat(VIDEO_RESOLUTION_MISMATCH_ERROR_MESSAGE_2, expectedHeight, actualHeight));
        }
    }

    private void verifyDuration(Map<String, Object> videoMetaData, CustomSoftAssert softAssert, String testStatus) {
        int bufferTime = testStatus.equalsIgnoreCase(IDLE_TIMEOUT_STATUS) ?
                240 :
                60; // Buffer time in seconds, more time incase it is idle timeout
        double testExecutionTimeInSeconds = Double.parseDouble((String) TEST_REPORT.get().get(TEST_EXECUTION_TIME));
        double expectedVideoDurationLimit = testExecutionTimeInSeconds + bufferTime;

        double actualVideoDuration = Double.parseDouble(
                videoMetaData.get(videoMetadataTypes.DURATION_IN_SECONDS.getValue()).toString());

        ltLogger.info("Actual video duration: {}. Expected video duration limit: {}", actualVideoDuration,
                expectedVideoDurationLimit);

        softAssert.assertTrue(actualVideoDuration < expectedVideoDurationLimit,
                softAssertMessageFormat(VIDEO_DURATION_MISMATCH_ERROR_MESSAGE, expectedVideoDurationLimit, actualVideoDuration));
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
                    softAssertMessageFormat(UNABLE_TO_DOWNLOAD_VIDEO_ERROR_MESSAGE, videoFileName));
            if (isVideoDownloadSuccess) {
                String completeFilePath = TEST_LOGS_DOWNLOAD_DIRECTORY + videoFileName;
                String testStatus = automationAPIHelper.getStatusOfSessionViaAPI(session_id);
                ltLogger.info("Test status: {}", testStatus);
                verifyVideoMetaData(completeFilePath, testCaps, softAssert, testStatus);
                int statusCodeOfShareVideoUrl = getRequest(videoShareUrl).statusCode();
                softAssert.assertTrue(statusCodeOfShareVideoUrl == 200,
                        softAssertMessageFormat(VIDEO_SHARE_URL_NOT_VALID_ERROR_MESSAGE, videoShareUrl));
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
                softAssertMessageFormat(UNABLE_TO_FETCH_LIGHTHOUSE_REPORT_ERROR_MESSAGE, status, message));
        if (status.equalsIgnoreCase("success")) {
            String jsonReport = lighthouseReportDTO.getData().getJson_report();
            String htmlReport = lighthouseReportDTO.getData().getHtml_report();
            int jsonReportFetchStatusCode = getRequest(jsonReport).statusCode();
            int htmlReportFetchStatusCode = getRequest(htmlReport).statusCode();
            ltLogger.info("JSON report status: {} and HTML report status: {}", jsonReportFetchStatusCode,
                    htmlReportFetchStatusCode);
            softAssert.assertTrue(jsonReportFetchStatusCode == 200,
                    softAssertMessageFormat(UNABLE_TO_DOWNLOAD_JSON_LIGHTHOUSE_REPORT_ERROR_MESSAGE, jsonReportFetchStatusCode));
            softAssert.assertTrue(htmlReportFetchStatusCode == 200,
                    softAssertMessageFormat(UNABLE_TO_DOWNLOAD_HTML_LIGHTHOUSE_REPORT_ERROR_MESSAGE, htmlReportFetchStatusCode));
        }
        EnvSetup.SOFT_ASSERT.set(softAssert);
    }
}
