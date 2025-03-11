package automationHelper;

import DTOs.SwaggerAPIs.ArtefactsApiV2ResponseDTO;
import TestManagers.ApiManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.CustomSoftAssert;
import utility.EnvSetup;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import static utility.EnvSetup.TEST_VERIFICATION_DATA;
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
    COMMAND("command"), SELENIUM("selenium"), NETWORK("network"), CONSOLE("console"), TERMINAL("terminal");
    private final String value;

    LogType(String value) {
      this.value = value;
    }
  }

  private static final String apiV2UrlGenerationSuccessMessage = "URL is succesfully generated";

  private static final String V2 = "/v2";
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
    } else {
      ltLogger.info("Fetched artefacts API URL response via API V1: {}", response);
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

  public void verifySystemLogs(String logsType, String session_id) {
    String logsFromApiV1 = fetchLogs(logsType, ArtefactAPIVersions.API_V1, session_id);
    System.out.println("Selenium Logs from API v1: " + logsFromApiV1);
    String logsFromApiV2 = fetchLogs(logsType, ArtefactAPIVersions.API_V2, session_id);
    System.out.println("Selenium Logs from API v2: " + logsFromApiV2);
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

  private void verifyExpectedUrlsArePresent(Queue<String> fetchedData, String logsSource, CustomSoftAssert softAssert) {
    Queue<String> expectedData = (Queue<String>) TEST_VERIFICATION_DATA.get().get(testVerificationDataKeys.URL);
    assert expectedData != null;
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
    this.verifyCommandLogs(session_id, ArtefactAPIVersions.API_V1, COMMAND_LOGS_API_V1_SCHEMA,
      expectedCommandLogsCount);
    this.verifyCommandLogs(session_id, ArtefactAPIVersions.API_V2, COMMAND_LOGS_API_V2_SCHEMA,
      expectedCommandLogsCount);
  }

  private void verifyCommandLogs(String session_id, ArtefactAPIVersions apiVersion, String schemaFilePath,
    int expectedCommandLogsCount) {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    String logsFromApi = fetchLogs(LogType.COMMAND.value, apiVersion, session_id);
    Set<String> schemaValidationErrors = validateSchema(logsFromApi, schemaFilePath);
    softAssert.assertTrue(schemaValidationErrors.isEmpty(),
      "Schema validation failed for command logs from " + apiVersion.toString() + ". Errors: " + schemaValidationErrors);
    JsonElement logsJson = constructJsonFromString(logsFromApi);
    JsonArray commandsArray = apiVersion == ArtefactAPIVersions.API_V1 ?
      logsJson.getAsJsonObject().get("data").getAsJsonArray() :
      logsJson.getAsJsonArray();
    softAssert.assertTrue(commandsArray.size() == expectedCommandLogsCount,
      "Command logs count fetched from " + apiVersion + " doesn't match. Expected: " + expectedCommandLogsCount + ", Actual: " + commandsArray.size());
    Queue<String> fetchedUrlsFromCommandLogs = extractUrlsAPIResponse(commandsArray);
    verifyExpectedUrlsArePresent(fetchedUrlsFromCommandLogs, "command " + apiVersion, softAssert);
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  public void verifyConsoleLogs(String session_id) {

  }

  public void verifyTerminalLogs(String session_id) {

  }

  public void verifyNetworkLogs(String session_id) {
    String logsFromApiV1 = fetchLogs(LogType.NETWORK.value, ArtefactAPIVersions.API_V1, session_id);
    System.out.println("Network Logs from API v1: " + logsFromApiV1);
    String logsFromApiV2 = fetchLogs(LogType.NETWORK.value, ArtefactAPIVersions.API_V2, session_id);
    System.out.println("Network Logs from API v2: " + logsFromApiV2);
  }

  public void verifyNetworkFullHarLogs(String session_id) {

  }
}
