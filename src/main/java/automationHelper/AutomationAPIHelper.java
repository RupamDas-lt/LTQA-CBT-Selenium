package automationHelper;

import DTOs.Others.BrowserVersionsFromCapsGenerator;
import DTOs.Others.SeleniumVersionsDTO;
import DTOs.Others.TunnelsAPIResponseDTO;
import DTOs.SwaggerAPIs.GetBuildResponseDTO;
import DTOs.SwaggerAPIs.GetSessionResponseDTO;
import TestManagers.ApiManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import com.google.gson.reflect.TypeToken;
import com.mysql.cj.util.StringUtils;
import io.restassured.http.Method;
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

import static utility.EnvSetup.*;
import static utility.FrameworkConstants.*;

public class AutomationAPIHelper extends ApiManager {

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
    final int maxRetries = 10;
    final int retryDelaySecs = 5;
    int attempt = 0;
    Exception lastException = null;

    while (attempt < maxRetries) {
      attempt++;
      try {
        String sessionAPIUrl = constructAPIUrl(EnvSetup.API_URL_BASE, SESSIONS_API_ENDPOINT, session_id);
        String sessionResponse = getRequestWithBasicAuthAsString(sessionAPIUrl, EnvSetup.testUserName.get(),
          EnvSetup.testAccessKey.get());

        GetSessionResponseDTO getSessionResponseDTO = convertJsonStringToPojo(sessionResponse,
          new TypeToken<GetSessionResponseDTO>() {
          });

        GetSessionResponseDTO.Data data = getSessionResponseDTO.getData();
        Field field = GetSessionResponseDTO.Data.class.getDeclaredField(requiredDetail);
        field.setAccessible(true);
        return field.get(data).toString();

      } catch (Exception e) {
        lastException = e;
        ltLogger.error("Attempt {}/{} failed to get session details {}: {}", attempt, maxRetries, requiredDetail,
          e.getMessage());

        if (attempt < maxRetries) {
          waitForTime(retryDelaySecs);
        }
      }
    }

    ltLogger.error("Failed to get session details {} after {} attempts", requiredDetail, maxRetries);
    throw new RuntimeException(
      "Unable to extract " + requiredDetail + " detail from Session details api response after " + maxRetries + " attempts",
      lastException);
  }

  public String getSpecificBuildDetailsViaAPI(String build_id, String requiredDetail) {
    String buildAPIUrl = constructAPIUrlWithBasicAuth(EnvSetup.API_URL_BASE, BUILDS_API_ENDPOINT,
      EnvSetup.testUserName.get(), EnvSetup.testAccessKey.get(), build_id);
    ltLogger.info("Get Build Details via API: {}", buildAPIUrl);
    String buildAPIResponse = getRequestAsString(buildAPIUrl);
    GetBuildResponseDTO getBuildResponseDTO = convertJsonStringToPojo(buildAPIResponse,
      new TypeToken<GetBuildResponseDTO>() {
      });
    GetBuildResponseDTO.Data data = getBuildResponseDTO.getData();
    try {
      Field field = GetBuildResponseDTO.Data.class.getDeclaredField(requiredDetail);
      field.setAccessible(true);
      return (String) field.get(data);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
      ltLogger.error("Unable to get the build details {} from the Sessions API response.", requiredDetail);
      throw new RuntimeException("Unable to extract " + requiredDetail + " detail from Build details api response", e);
    }
  }

  public String getStatusOfSessionViaAPI(String session_id) {
    final String keyForSessionStatus = "status_ind";
    String status = getSpecificSessionDetailsViaAPI(session_id, keyForSessionStatus);
    ltLogger.info("Status of session: {} is: {}", session_id, status);
    return status;
  }

  public String getBuildIdFromSessionId(String session_id) {
    final String keyForBuildId = "build_id";
    String buildId;
    String cachedBuildId = EnvSetup.BUILD_ID.get();
    if (StringUtils.isNullOrEmpty(cachedBuildId)) {
      buildId = getSpecificSessionDetailsViaAPI(session_id, keyForBuildId);
      ltLogger.info("Fetched Build ID from session details API response: {}", buildId);
    } else {
      buildId = cachedBuildId;
      ltLogger.info("Using cached build id: {}", buildId);
    }
    return buildId;
  }

  public String getStatusOfBuildViaAPI(String build_id) {
    final String keyForStatus = "status_ind";
    String status = getSpecificBuildDetailsViaAPI(build_id, keyForStatus);
    ltLogger.info("Status of build: {} is: {}", build_id, status);
    return status;
  }

  public void sendCustomDataToSumo(HashMap<String, Object> customData) {
    customData.put("message", "New_Framework_Testing-2");
    customData.put("attempt", System.getProperty(TEST_ATTEMPT, "first"));
    String jsonString = new JSONObject(customData).toString();
    ltLogger.info("Custom data to push to SumoLogic: {}", jsonString);
    try {
      Response response = postRequestWithURLEncoding(SUMO_LOGIC_URL, jsonString);
      ltLogger.info("Response of send data to sumo: {}", response.asString());
      assert response.statusCode() == 200;
    } catch (AssertionError e) {
      ltLogger.error("Got Exception while sending data to sumo", e);
      if (!e.getMessage().contains("Expected status code <200> but was <429>")) {
        throw e;
      }
    }
  }

  public void fetchAllGeoLocationsFromCapsGeneratorAndStoreInJsonFile() {
    String geoLocationFetchAPI = constructAPIUrl(EnvSetup.API_URL_BASE, GEOLOCATIONS_API_ENDPOINT);
    geoLocationFetchAPI = handleCapabilityGeneratorAPIsForGDPRUser(geoLocationFetchAPI);
    ltLogger.info("API for GEO location fetch: {}", geoLocationFetchAPI);
    fetchDataAndWriteResponseToFile(geoLocationFetchAPI, GEOLOCATION_DATA_PATH);
  }

  public String getSeleniumVersionBasedOnKeyWord(String seleniumKeyWord, String browserName, String browserVersion,
    String template) {
    if (template.contains("ubuntu")) {
      template = "ubuntu-20";
    }
    String actualBrowserVersion = browserVersion.contains("latest") ?
      getBrowserVersionBasedOnKeyword(browserName, browserVersion, template) :
      browserVersion;
    String browserVersionId = EnvSetup.TEST_VERIFICATION_DATA.get().get(testVerificationDataKeys.BROWSER_VERSION_ID)
      .toString();
    String seleniumVersionFetchUrl = constructAPIUrl(EnvSetup.API_URL_BASE, SELENIUM_VERSIONS_API_ENDPOINT).replace(
        "<BROWSER_NAME>", browserName).replace("<TEMPLATE>", osTemplateNameToKeywordMap.getOrDefault(template, template))
      .replace("<BROWSER_VERSION>", actualBrowserVersion).replace("<BROWSER_VERSION_ID>", browserVersionId);
    seleniumVersionFetchUrl = handleCapabilityGeneratorAPIsForGDPRUser(seleniumVersionFetchUrl);
    ltLogger.info("URI to fetch Selenium Versions from capability generator api: {}", seleniumVersionFetchUrl);
    String response = getRequestAsString(seleniumVersionFetchUrl);
    SeleniumVersionsDTO seleniumVersionsDTO = convertJsonStringToPojo(
      new JSONArray(response).getJSONObject(0).toString(), new TypeToken<SeleniumVersionsDTO>() {
      });
    String[] seleniumCapsParams = seleniumKeyWord.split("-");
    String retrievedSeleniumVersion = seleniumCapsParams.length == 1 ?
      seleniumVersionsDTO.getSelenium_version().getLast() :
      seleniumVersionsDTO.getSelenium_version()
        .get(seleniumVersionsDTO.getSelenium_version().size() - Integer.parseInt(seleniumCapsParams[1]) - 1);
    ltLogger.info("Retrieved Selenium Version: {}", retrievedSeleniumVersion);
    return retrievedSeleniumVersion;
  }

  public String getBrowserVersionBasedOnKeyword(String browserName, String keyword, String template) {
    String retrievedBrowserVersion = EnvSetup.TEST_VERIFICATION_DATA.get()
      .getOrDefault(testVerificationDataKeys.ACTUAL_BROWSER_VERSION, "").toString();
    if (template.contains("ubuntu")) {
      template = "ubuntu-20";
    }
    if (!retrievedBrowserVersion.isEmpty())
      return retrievedBrowserVersion;
    String filePath = BROWSER_VERSIONS_DATA_PATH.replace("<BROWSER_NAME>", browserName).replace("<TEMPLATE>", template);
    ltLogger.info("Browser versions data path: {}", filePath);

    String browserVersionFetchUrl = constructAPIUrl(EnvSetup.API_URL_BASE, BROWSER_VERSIONS_API_ENDPOINT).replace(
      "<BROWSER_NAME>", browserName).replace("<TEMPLATE>", osTemplateNameToKeywordMap.getOrDefault(template, template));
    browserVersionFetchUrl = handleCapabilityGeneratorAPIsForGDPRUser(browserVersionFetchUrl);
    ltLogger.info("API for browser version fetch: {}", browserVersionFetchUrl);

    fetchDataAndWriteResponseToFile(browserVersionFetchUrl, filePath);

    try {
      Map<String, String> versionMap = new HashMap<>();
      Map<String, String> versionIdMap = new HashMap<>();
      List<String> stableVersions = new ArrayList<>();
      List<String> stableVersionIds = new ArrayList<>();

      BrowserVersionsFromCapsGenerator browserVersionsFromCapsGenerator = convertJsonStringToPojo(
        Files.readString(getFileWithFileLock(filePath).toPath()), new TypeToken<BrowserVersionsFromCapsGenerator>() {
        });
      ArrayList<BrowserVersionsFromCapsGenerator.VersionDTO> versionDTOS = browserVersionsFromCapsGenerator.getVersions();
      for (BrowserVersionsFromCapsGenerator.VersionDTO versionDTO : versionDTOS) {
        String channelType = versionDTO.getChannel_type();
        String versionNumber = versionDTO.getVersion();
        String versionId = versionDTO.getId();
        switch (channelType) {
        case "dev" -> {
          versionMap.put("dev", versionNumber);
          versionIdMap.put("dev", versionId);
        }
        case "beta" -> {
          versionMap.put("beta", versionNumber);
          versionIdMap.put("beta", versionId);
        }
        default -> {
          stableVersions.add(versionNumber);
          stableVersionIds.add(versionId);
        }
        }
      }

      ltLogger.info("{} versions on template {}: Dev: {}, Beta: {}, Stable: {}", browserName, template,
        versionMap.get("dev"), versionMap.get("beta"), stableVersions);

      String retrievedBrowserVersionId;

      retrievedBrowserVersion = switch (keyword.toLowerCase()) {
        case "dev" -> {
          String version = versionMap.get("dev").split("\\.")[0];
          retrievedBrowserVersionId = versionIdMap.get("dev");
          yield version;
        }
        case "beta" -> {
          String version = versionMap.get("beta").split("\\.")[0];
          retrievedBrowserVersionId = versionIdMap.get("beta");
          yield version;
        }
        case "latest" -> {
          String version = stableVersions.getFirst().split("\\.")[0];
          retrievedBrowserVersionId = stableVersionIds.getFirst();
          yield version;
        }
        default -> {
          String[] versionParams = keyword.split("-");
          int index = Integer.parseInt(versionParams[versionParams.length - 1]);
          retrievedBrowserVersionId = stableVersionIds.get(index);
          yield stableVersions.get(index).split("\\.")[0];
        }
      };
      ltLogger.info("Retrieved browser version: {}", retrievedBrowserVersion);
      ltLogger.info("Retrieved browser version id: {}", retrievedBrowserVersionId);
      EnvSetup.TEST_VERIFICATION_DATA.get()
        .put(testVerificationDataKeys.ACTUAL_BROWSER_VERSION, retrievedBrowserVersion);
      EnvSetup.TEST_VERIFICATION_DATA.get().put(testVerificationDataKeys.BROWSER_VERSION_ID, retrievedBrowserVersionId);
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
    if (response.getStatusCode() == 200) {
      try {
        TEST_VERIFICATION_DATA.get()
          .put(testVerificationDataKeys.TERMINAL_LOG, readFileData(SAMPLE_TERMINAL_LOGS_FILE_PATH));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return response.getBody().path("status").toString();
  }

  private JsonNode fetchTestDetails(String session_id) {
    JsonNode node = EnvSetup.TEST_DETAIL_API_RESPONSE.get();
    if (node == null || !node.isEmpty()) {
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

  public String getSessionIDFromTestId(String test_id) {
    final String sessionIdKeyInTestDetailsAPI = "session_id";
    String sessionId = fetchTestDetails(test_id).get(sessionIdKeyInTestDetailsAPI).asText();
    ltLogger.info("Retrieved session ID: {}", sessionId);
    return sessionId;
  }

  public String getTestIdFromSessionId(String session_id) {
    final String testIdKeyInTestDetailsAPI = "id";
    String testId = fetchTestDetails(session_id).get(testIdKeyInTestDetailsAPI).asText();
    ltLogger.info("Retrieved test ID: {}", testId);
    return testId;
  }

  private JsonNode fetchFeatureFlagDetailsOfSpecificSession(String session_id) {
    final String featureFlagKeyInTestDetailsAPI = "feature_flag";
    if (EnvSetup.TEST_FEATURE_FLAG_DETAILS.get() == null) {
      JsonNode node = fetchTestDetails(session_id).get(featureFlagKeyInTestDetailsAPI);
      EnvSetup.TEST_FEATURE_FLAG_DETAILS.set(node);
      try {
        ltLogger.info("Feature flag: {}", new ObjectMapper().writerWithDefaultPrettyPrinter()
          .writeValueAsString(EnvSetup.TEST_FEATURE_FLAG_DETAILS.get()));
      } catch (JsonProcessingException e) {
        ltLogger.warn("Unable to Process Json response. Exception {}", e.getMessage());
      }
    }
    return EnvSetup.TEST_FEATURE_FLAG_DETAILS.get();
  }

  public String getFeatureFlagValueOfSpecificSession(String session_id, String flagName) {
    JsonNode featureFlagsDetailsOfCurrentSession = fetchFeatureFlagDetailsOfSpecificSession(session_id);
    String flagStatus = featureFlagsDetailsOfCurrentSession.has(flagName) ?
      featureFlagsDetailsOfCurrentSession.get(flagName).asText() :
      "false";
    ltLogger.info("Session id: {} :- {} feature flag status: {}", session_id, flagName, flagStatus);
    return flagStatus;
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

  public Response stopTestViaApi(String session_id) {
    String uri = constructAPIUrlWithBasicAuth(EnvSetup.API_URL_BASE, SESSIONS_API_ENDPOINT, EnvSetup.testUserName.get(),
      EnvSetup.testAccessKey.get(), session_id, sessionApiEndpoints().get("stop"));
    ltLogger.info("Stopping test Via API: {}", uri);
    Response response = putRequest(uri);
    ltLogger.info("Test stop API response: {}", response.body().asString());
    waitForTime(5);
    return response;
  }

  public Response stopBuildViaApi(String build_id) {
    String uri = constructAPIUrlWithBasicAuth(EnvSetup.API_URL_BASE, BUILD_STOP_API_ENDPOINT,
      EnvSetup.testUserName.get(), EnvSetup.testAccessKey.get(), build_id);
    ltLogger.info("Stopping build Via API: {}", uri);
    Response response = putRequest(uri);
    ltLogger.info("Build stop API response: {}", response.body().asString());
    waitForTime(5);
    return response;
  }

  public Map<String, String> getAllRunningTunnels() {
    String uri = constructAPIUrlWithBasicAuth(EnvSetup.API_URL_BASE, TUNNELS_API_ENDPOINT, EnvSetup.testUserName.get(),
      EnvSetup.testAccessKey.get());
    ltLogger.info("Fetching Tunnel details via API: {}", uri);
    String responseString = getRequestAsString(uri);
    TunnelsAPIResponseDTO tunnelsAPIResponseDTO = convertJsonStringToPojo(responseString,
      new TypeToken<TunnelsAPIResponseDTO>() {
      });
    assert tunnelsAPIResponseDTO != null && tunnelsAPIResponseDTO.getStatus().equals("success");
    ArrayList<TunnelsAPIResponseDTO.TunnelData> data = tunnelsAPIResponseDTO.getData();
    Map<String, String> tunnelNameToTunnelIDMap = new HashMap<>();
    for (TunnelsAPIResponseDTO.TunnelData tunnelData : data) {
      String name = tunnelData.getTunnel_name();
      String id = Integer.toString(tunnelData.getTunnel_id());
      String status = tunnelData.getStatus_ind();
      if (status.equalsIgnoreCase(RUNNING)) {
        tunnelNameToTunnelIDMap.put(name, id);
      }
    }
    ltLogger.info("Retrieved Running Tunnel Details: {}", tunnelNameToTunnelIDMap);
    return tunnelNameToTunnelIDMap;
  }

  public String stopTunnel(String tunnel_id) {
    String uri = constructAPIUrlWithBasicAuth(EnvSetup.API_URL_BASE, TUNNELS_API_ENDPOINT, EnvSetup.testUserName.get(),
      EnvSetup.testAccessKey.get(), "/" + tunnel_id);
    ltLogger.info("Stopping Tunnel with API: {}", uri);
    Response response = deleteRequest(uri);
    ltLogger.info("Tunnel stop API response: {}", response.body().asString());
    return response.getBody().jsonPath().get("status").toString();
  }

  public Map<String, String> getCookiesFromLoginAPI() {
    Map<String, String> body = Map.of("email", testEmail.get(), "password", testPassword.get());
    String authApiBase = AUTH_API_BASE.get(TEST_ENV.contains("stage") ? "stage" : "prod");
    String uri = constructAPIUrl(authApiBase, AUTH_API_ENDPOINT);
    return getCookiesFromResponse(uri, Method.POST, body, null, null);
  }

  public String getCurrentIPFromAPI() {
    String ip = getRequestAsString(API_TO_GET_IP);
    ltLogger.info("Current local machine IP fetched from API: {}", ip);
    return ip;
  }
}
