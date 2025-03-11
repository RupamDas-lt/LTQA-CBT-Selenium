package automationHelper;

import DTOs.SwaggerAPIs.ArtefactsApiV2ResponseDTO;
import TestManagers.ApiManager;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.CustomSoftAssert;
import utility.EnvSetup;

import static utility.FrameworkConstants.*;

public class TestArtefactsVerificationHelper extends ApiManager {

  private final Logger ltLogger = LogManager.getLogger(TestArtefactsVerificationHelper.class);

  private enum ArtefactAPIVersions {
    API_V1, API_V2
  }

  private enum ArtefactApiV2UrlStatus {
    SUCCESS, FAIL
  }

  private static final String apiV2UrlGenerationSuccessMessage = "URL is succesfully generated";

  private static final String V2 = "/v2";
  private final boolean apiV2logsDownloadStatusSuccess = true;

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

  public void verifyCommandLogs(String session_id) {
    String logsFromApiV1 = fetchLogs("command", ArtefactAPIVersions.API_V1, session_id);
    System.out.println("Command Logs from API v1: " + logsFromApiV1);
    String logsFromApiV2 = fetchLogs("command", ArtefactAPIVersions.API_V2, session_id);
    System.out.println("Command Logs from API v2: " + logsFromApiV2);
  }

  public void verifyConsoleLogs(String session_id) {

  }

  public void verifyTerminalLogs(String session_id) {

  }

  public void verifyNetworkLogs(String session_id) {
    String logsFromApiV1 = fetchLogs("network", ArtefactAPIVersions.API_V1, session_id);
    System.out.println("Network Logs from API v1: " + logsFromApiV1);
    String logsFromApiV2 = fetchLogs("network", ArtefactAPIVersions.API_V2, session_id);
    System.out.println("Network Logs from API v2: " + logsFromApiV2);
  }

  public void verifyNetworkFullHarLogs(String session_id) {

  }
}
