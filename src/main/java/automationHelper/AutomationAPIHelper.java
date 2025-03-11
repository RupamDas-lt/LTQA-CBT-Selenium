package automationHelper;

import DTOs.Others.BrowserVersionsFromCapsGenerator;
import DTOs.SwaggerAPIs.GetSessionResponseDTO;
import TestManagers.ApiManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.reflect.TypeToken;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.EnvSetup;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utility.FrameworkConstants.*;

public class AutomationAPIHelper extends ApiManager {

  // API end-points
  private static final String GEOLOCATIONS_API_ENDPOINT = "/api/v1/geolocation?unique=true";
  private static final String BROWSER_VERSIONS_API_ENDPOINT = "/api/v2/capability?grid=selenium&browser=<BROWSER_NAME>&os=<TEMPLATE>";

  private final Logger ltLogger = LogManager.getLogger(AutomationAPIHelper.class);

  public void updateSessionDetailsViaAPI(String session_id, HashMap<String, String> sessionDetails) {
    String sessionAPIUrl = constructAPIUrl(EnvSetup.API_URL_BASE, SESSIONS_API_ENDPOINT, session_id);
    ltLogger.info("Update Session Details: {}", sessionDetails);
    Response response = patchRequestWithBasicAuth(sessionAPIUrl, EnvSetup.testUserName.get(),
      EnvSetup.testAccessKey.get(), sessionDetails);
    ltLogger.info("Update Session Details Response Body: {}", response.getBody().asString());
    ltLogger.info("Update Session Details Response Code: {}", response.getStatusCode());
  }

  public String getSpecificSessionDetailsViaAPI(String session_id, String requiredDetail) {
    String sessionAPIUrl = constructAPIUrl(EnvSetup.API_URL_BASE, SESSIONS_API_ENDPOINT, session_id);
    String sessionResponse = getRequestWithBasicAuthAsString(sessionAPIUrl, EnvSetup.testUserName.get(),
      EnvSetup.testAccessKey.get());
    GetSessionResponseDTO getSessionResponseDTO = convertJsonStringToPojo(sessionResponse,
      new TypeToken<GetSessionResponseDTO>() {
      });
    GetSessionResponseDTO.Data data = getSessionResponseDTO.getData();
    try {
      Field field = GetSessionResponseDTO.Data.class.getDeclaredField(requiredDetail);
      field.setAccessible(true);
      return (String) field.get(data);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
      ltLogger.error("Unable to get the session details {} from the Sessions API response.", requiredDetail);
      return null;
    }
  }

  public void sendCustomDataToSumo(HashMap<String, Object> customData) {
    customData.put("message", "New_Framework_Testing-2");
    ltLogger.info("Custom data to push to SumoLogic: {}", customData);
    try {
      putRequestWithURLEncoding(SUMO_LOGIC_URL, customData);
    } catch (AssertionError e) {
      ltLogger.error("Got Exception while sending data to sumo", e);
      if (!e.getMessage().contains("Expected status code <200> but was <429>")) {
        throw e;
      }
    }
  }

  public void fetchAllGeoLocationsFromCapsGeneratorAndStoreInJsonFile() {
    String geoLocationFetchAPI = constructAPIUrl(EnvSetup.API_URL_BASE, GEOLOCATIONS_API_ENDPOINT);
    ltLogger.info("API for GEO location fetch: {}", geoLocationFetchAPI);
    fetchDataAndWriteResponseToFile(geoLocationFetchAPI, GEOLOCATION_DATA_PATH);
  }

  public String getBrowserVersionBasedOnKeyword(String browserName, String keyword, String template) {
    String retrievedBrowserVersion = EnvSetup.TEST_VERIFICATION_DATA.get()
      .getOrDefault(testVerificationDataKeys.ACTUAL_BROWSER_VERSION, "").toString();
    if (!retrievedBrowserVersion.isEmpty())
      return retrievedBrowserVersion;
    String filePath = BROWSER_VERSIONS_DATA_PATH.replace("<BROWSER_NAME>", browserName).replace("<TEMPLATE>", template);
    ltLogger.info("Browser versions data path: {}", filePath);

    String browserVersionFetchUrl = constructAPIUrl(EnvSetup.API_URL_BASE, BROWSER_VERSIONS_API_ENDPOINT).replace(
      "<BROWSER_NAME>", browserName).replace("<TEMPLATE>", osTemplateNameToKeywordMap.get(template));
    ltLogger.info("API for browser version fetch: {}", browserVersionFetchUrl);

    fetchDataAndWriteResponseToFile(browserVersionFetchUrl, filePath);

    try {
      Map<String, String> versionMap = new HashMap<>();
      List<String> stableVersions = new ArrayList<>();

      BrowserVersionsFromCapsGenerator browserVersionsFromCapsGenerator = convertJsonStringToPojo(
        Files.readString(getFileWithFileLock(filePath).toPath()), new TypeToken<BrowserVersionsFromCapsGenerator>() {
        });
      ArrayList<BrowserVersionsFromCapsGenerator.VersionDTO> versionDTOS = browserVersionsFromCapsGenerator.getVersions();
      for (BrowserVersionsFromCapsGenerator.VersionDTO versionDTO : versionDTOS) {
        String channelType = versionDTO.getChannel_type();
        String versionNumber = versionDTO.getVersion();
        switch (channelType) {
        case "dev" -> versionMap.put("dev", versionNumber);
        case "beta" -> versionMap.put("beta", versionNumber);
        default -> stableVersions.add(versionNumber);
        }
      }

      ltLogger.info("{} versions on template {}: Dev: {}, Beta: {}, Stable: {}", browserName, template,
        versionMap.get("dev"), versionMap.get("beta"), stableVersions);

      retrievedBrowserVersion = switch (keyword.toLowerCase()) {
        case "dev" -> versionMap.get("dev").split("\\.")[0];
        case "beta" -> versionMap.get("beta").split("\\.")[0];
        case "latest" -> stableVersions.getFirst().split("\\.")[0];
        default -> {
          String[] versionParams = keyword.split("-");
          int index = Integer.parseInt(versionParams[versionParams.length - 1]);
          yield stableVersions.get(index).split("\\.")[0];
        }
      };
      ltLogger.info("Retrieved browser version: {}", retrievedBrowserVersion);
      EnvSetup.TEST_VERIFICATION_DATA.get()
        .put(testVerificationDataKeys.ACTUAL_BROWSER_VERSION, retrievedBrowserVersion);
      return retrievedBrowserVersion;
    } catch (IOException e) {
      throw new RuntimeException("Failed to fetch or parse browser versions data", e);
    }
  }

  public String uploadTerminalLogs(String session_id) {
    String urlToUploadTerminalLogs = constructAPIUrl(EnvSetup.API_URL_BASE, SESSIONS_API_ENDPOINT, session_id,
      sessionApiEndpoints().get("terminal"));
    ltLogger.info("Uploading terminal logs to: {}", urlToUploadTerminalLogs);
    HashMap<String, Object> multipartBody = new HashMap<>();
    multipartBody.put("contentType", REQUEST_BODY_CONTENT_TYPE_MULTIPART_FORM);
    multipartBody.put("file", new File(SAMPLE_TERMINAL_LOGS_FILE_PATH));
    Response response = postRequestWithBasicAuth(urlToUploadTerminalLogs, multipartBody, EnvSetup.testUserName.get(),
      EnvSetup.testAccessKey.get());
    return response.getBody().path("status").toString();
  }

  private JsonNode fetchTestDetails(String session_id) {
    JsonNode node = EnvSetup.TEST_DETAIL_API_RESPONSE.get();
    if (node == null) {
      String uri = constructAPIUrl(EnvSetup.API_URL_BASE, TEST_API_ENDPOINT, session_id);
      ltLogger.info("Fetching test details from: {}", uri);

      int maxRetryCount = 5;
      for (int retryCount = 1; retryCount <= maxRetryCount; retryCount++) {
        try {
          node = new ObjectMapper().readTree(
            getRequestWithBasicAuthAsString(uri, EnvSetup.testUserName.get(), EnvSetup.testAccessKey.get()));
          EnvSetup.TEST_DETAIL_API_RESPONSE.set(node);
          break;
        } catch (Exception e) {
          ltLogger.error("Trial: {} -> Failed to fetch test details from: {}. Error: {}", retryCount, uri,
            e.getMessage());
          if (retryCount == maxRetryCount) {
            throw new RuntimeException("Failed to fetch test details from: " + uri, e);
          }
        }
      }
    }
    return node;
  }

  public void getCommandCounts(String session_id) {
    int commandCount;
    int exceptionCount;
    int visualCommandCount;
    if (EnvSetup.SESSION_COMMAND_LOGS_COUNT_FROM_TEST_API.get() == null || EnvSetup.SESSION_EXCEPTION_LOGS_COUNT_FROM_TEST_API.get() == null || EnvSetup.SESSION_VISUAL_LOGS_COUNT_FROM_TEST_API.get() == null) {
      JsonNode testDetails = fetchTestDetails(session_id);
      commandCount = testDetails.get("commandCount").asInt(0);
      exceptionCount = testDetails.get("exceptionCount").asInt(0);
      visualCommandCount = testDetails.get("visualCommandCount").asInt(0);
      ltLogger.info(
        "Fetched command counts-> All commands count: {}, exception commands count: {}, visual commands count: {}",
        commandCount, exceptionCount, visualCommandCount);
      EnvSetup.SESSION_COMMAND_LOGS_COUNT_FROM_TEST_API.set(commandCount);
      EnvSetup.SESSION_EXCEPTION_LOGS_COUNT_FROM_TEST_API.set(exceptionCount);
      EnvSetup.SESSION_VISUAL_LOGS_COUNT_FROM_TEST_API.set(visualCommandCount);
    }
  }
}
