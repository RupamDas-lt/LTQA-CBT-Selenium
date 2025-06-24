package automationHelper;

import DTOs.Others.BrowserVersionsFromCapsGenerator;
import DTOs.Others.SeleniumVersionsDTO;
import DTOs.Others.TestShareAPIResponseDTO;
import DTOs.Others.TunnelsAPIResponseDTO;
import DTOs.SwaggerAPIs.GetBrowserProfileResponseDTO;
import DTOs.SwaggerAPIs.GetBuildResponseDTO;
import DTOs.SwaggerAPIs.GetSessionResponseDTO;
import DTOs.SwaggerAPIs.UploadBrowserProfileResponseDTO;
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
import utility.CustomAssert;
import utility.EnvSetup;
import utility.FileLockUtility;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static factory.SoftAssertionMessages.*;
import static utility.EnvSetup.*;
import static utility.FrameworkConstants.*;

public class AutomationAPIHelper extends ApiManager {

  private final Logger ltLogger = LogManager.getLogger(AutomationAPIHelper.class);

  private record APIConfig(String apiBase, String userName, String accessKey) {
  }

  private APIConfig getAPIConfigsBasedOnSessionType(boolean... isClientTest) {
    boolean isClient = isClientTest.length > 0 && isClientTest[0];
    ltLogger.info("Using API Configs for {} session", isClient ? "Client" : "Test");
    return new APIConfig(isClient ? CLIENT_API_URL_BASE : API_URL_BASE,
      isClient ? clientTestUserName.get() : testUserName.get(),
      isClient ? clientTestAccessKey.get() : testAccessKey.get());
  }

  public void updateSessionDetailsViaAPI(String session_id, HashMap<String, String> sessionDetails,
    boolean... isClientTest) {
    APIConfig apiConfig = getAPIConfigsBasedOnSessionType(isClientTest);
    String sessionAPIUrl = constructAPIUrl(apiConfig.apiBase(), SESSIONS_API_ENDPOINT, session_id);
    ltLogger.info("Update Session Details: {}", sessionDetails);
    Response response = patchRequestWithBasicAuth(sessionAPIUrl, apiConfig.userName(), apiConfig.accessKey(),
      sessionDetails);
    ltLogger.info("Update Session Details Response Body: {}", response.getBody().asString());
    ltLogger.info("Update Session Details Response Code: {}", response.getStatusCode());
  }

  /// Retrieves specific session details via Sessions API from Swagger based on session ID and required detail.
  public Object getSpecificSessionDetailsViaAPI(String sessionId, sessionDetailsAPIKeys requiredDetail,
    boolean... isClientTest) {
    APIConfig apiConfig = getAPIConfigsBasedOnSessionType(isClientTest);

    final int maxRetries = 10;
    final int retryDelaySecs = 5;
    Exception lastException = null;

    Field field;
    try {
      field = GetSessionResponseDTO.Data.class.getDeclaredField(requiredDetail.getValue());
      field.setAccessible(true);
    } catch (NoSuchFieldException e) {
      throw new IllegalArgumentException("Invalid field requested: " + requiredDetail, e);
    }

    for (int attempt = 1; attempt <= maxRetries; attempt++) {
      try {
        String sessionAPIUrl = constructAPIUrl(apiConfig.apiBase(), SESSIONS_API_ENDPOINT, sessionId);
        String sessionResponse = getRequestWithBasicAuthAsString(sessionAPIUrl, apiConfig.userName(),
          apiConfig.accessKey());

        GetSessionResponseDTO getSessionResponseDTO = convertJsonStringToPojo(sessionResponse,
          new TypeToken<GetSessionResponseDTO>() {
          });

        Object value = field.get(getSessionResponseDTO.getData());
        if (value != null) {
          return value;
        }
        throw new RuntimeException(
          "Field '" + requiredDetail + "' value is null in Session details API response. URL: " + sessionAPIUrl);

      } catch (Exception e) {
        lastException = e;
        ltLogger.error("Attempt {}/{} failed to get session details {}: {}", attempt, maxRetries, requiredDetail,
          e.getMessage());

        if (attempt < maxRetries) {
          waitForTime(retryDelaySecs);
        }
      }
    }

    String errorMessage = String.format(
      "Unable to extract %s detail from Session details api response after %d attempts", requiredDetail, maxRetries);
    ltLogger.error(errorMessage);
    throw new RuntimeException(errorMessage, lastException);
  }

  public Object getSpecificBuildDetailsViaAPI(String build_id, buildDetailsAPIKeys requiredDetail) {
    String buildAPIUrl = constructAPIUrlWithBasicAuth(EnvSetup.API_URL_BASE, BUILDS_API_ENDPOINT,
      EnvSetup.testUserName.get(), EnvSetup.testAccessKey.get(), build_id);
    ltLogger.info("Get Build Details via API: {}", buildAPIUrl);
    String buildAPIResponse = getRequestAsString(buildAPIUrl);
    GetBuildResponseDTO getBuildResponseDTO = convertJsonStringToPojo(buildAPIResponse,
      new TypeToken<GetBuildResponseDTO>() {
      });
    GetBuildResponseDTO.Data data = getBuildResponseDTO.getData();
    try {
      Field field = GetBuildResponseDTO.Data.class.getDeclaredField(requiredDetail.getValue());
      field.setAccessible(true);
      return field.get(data);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
      ltLogger.error("Unable to get the build details {} from the Sessions API response.", requiredDetail);
      throw new RuntimeException("Unable to extract " + requiredDetail + " detail from Build details api response", e);
    }
  }

  public String getStatusOfSessionViaAPI(String session_id, boolean... isClientTest) {
    String status = getSpecificSessionDetailsViaAPI(session_id, sessionDetailsAPIKeys.STATUS_IND,
      isClientTest).toString();
    ltLogger.info("Status of session: {} is: {}", session_id, status);
    return status;
  }

  public String getBuildIdFromSessionId(String session_id) {
    String buildId;
    String cachedBuildId = EnvSetup.BUILD_ID.get();
    if (StringUtils.isNullOrEmpty(cachedBuildId)) {
      buildId = getSpecificSessionDetailsViaAPI(session_id, sessionDetailsAPIKeys.BUILD_ID).toString();
      ltLogger.info("Fetched Build ID from session details API response: {}", buildId);
      EnvSetup.BUILD_ID.set(buildId);
    } else {
      buildId = cachedBuildId;
      ltLogger.info("Using cached build id: {}", buildId);
    }
    return buildId;
  }

  public String getTestCreateTimeStampFromSessionId(String session_id) {
    String startTimeStamp = getSpecificSessionDetailsViaAPI(session_id,
      sessionDetailsAPIKeys.CREATE_TIMESTAMP).toString();
    ltLogger.info("Create timestamp of session: {} is: {}", session_id, startTimeStamp);
    return startTimeStamp;
  }

  @SuppressWarnings("unchecked")
  public List<String> getTagsFromSessionId(String session_id) {
    List<String> tags = (List<String>) getSpecificSessionDetailsViaAPI(session_id, sessionDetailsAPIKeys.TAGS);
    ltLogger.info("Tags of session: {} are: {}", session_id, tags);
    return tags;
  }

  public String getStatusOfBuildViaAPI(String build_id) {
    String status = getSpecificBuildDetailsViaAPI(build_id, buildDetailsAPIKeys.STATUS_IND).toString();
    ltLogger.info("Status of build: {} is: {}", build_id, status);
    return status;
  }

  @SuppressWarnings("unchecked")
  public List<String> getBuildTagsViaAPI(String buildID) {
    List<String> buildTags = (List<String>) getSpecificBuildDetailsViaAPI(buildID, buildDetailsAPIKeys.TAGS);
    ltLogger.info("Build tags of build: {} are: {}", buildID, buildTags);
    return buildTags;
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

          // If response is null or empty, retry
          if (node == null || node.isEmpty()) {
            ltLogger.error("Trial: {} -> Received null or empty response from: {}", retryCount, uri);
            throw new RuntimeException("Received null or empty response from: " + uri);
          }

          EnvSetup.TEST_DETAIL_API_RESPONSE.set(node);
          break;
        } catch (Exception e) {
          ltLogger.error("Trial: {} -> Failed to fetch test details from: {}. Error: {}", retryCount, uri,
            e.getMessage());
          waitForTime(10);
          if (retryCount == maxRetryCount) {
            throw new RuntimeException("Failed to fetch test details from: " + uri, e);
          }
        }
      }
    }
    return node;
  }

  public String getOrgIDFromTestId(String test_id) {
    final String orgIdKeyInTestDetailsAPI = "org_id";
    String orgId = fetchTestDetails(test_id).get(orgIdKeyInTestDetailsAPI).asText();
    ltLogger.info("Retrieved Org ID: {}", orgId);
    return orgId;
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

  public List<String> getTestAnnotationsFromSessionID(String session_id) {
    final String annotationsKeyInTestDetailsAPI = "annotations";
    JsonNode annotationsNode = fetchTestDetails(session_id).get(annotationsKeyInTestDetailsAPI);

    if (annotationsNode == null || !annotationsNode.isArray()) {
      ltLogger.warn("Annotations data is not an array or is null.");
      return Collections.emptyList();
    }

    List<String> annotations = StreamSupport.stream(annotationsNode.spliterator(), false)
      .filter(annotation -> annotation.has("name")).map(annotation -> annotation.get("name").asText())
      .collect(Collectors.toList());

    annotations.forEach(annotation -> ltLogger.debug("Found annotation: {}", annotation));
    ltLogger.info("Retrieved {} annotations", annotations.size());

    return annotations;
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

  public String getBearerTokenFromLoginAPI() {
    final String bearerTokenKey = "accessToken";
    Map<String, String> cookies = getCookiesFromLoginAPI();
    String bearerToken = cookies.get(bearerTokenKey);
    ltLogger.info("Bearer token fetched from login API: {}", bearerToken);
    if (StringUtils.isNullOrEmpty(bearerToken)) {
      throw new RuntimeException("Bearer token is null or empty. Please check your credentials.");
    }
    return bearerToken;
  }

  public String getBearerToken() {
    String user = testEmail.get();

    // Check if the token is already cached and return immediately if present
    String cachedToken = USER_TO_BEARER_TOKEN_MAP.get(user);
    if (cachedToken != null && !cachedToken.isEmpty()) {
      ltLogger.info("Using cached Bearer token: {}", cachedToken);
      return cachedToken;
    }

    // Acquire the lock only if token is not cached
    FileLockUtility.fileLock.lock();
    try {
      // Double-check if another thread has already fetched and cached the token while we were waiting for the lock
      cachedToken = USER_TO_BEARER_TOKEN_MAP.get(user);
      if (cachedToken != null && !cachedToken.isEmpty()) {
        ltLogger.info("Using cached Bearer token after lock: {}", cachedToken);
        return cachedToken;
      }

      // Token was not cached, fetch and cache it
      ltLogger.info("Fetching Bearer token from login API for user: {}", user);
      String bearerToken = getBearerTokenFromLoginAPI();
      USER_TO_BEARER_TOKEN_MAP.put(user, bearerToken);
      ltLogger.info("Bearer token set in Map: {}", bearerToken);
      return bearerToken;

    } finally {
      FileLockUtility.fileLock.unlock(); // Ensure lock is released even if an exception occurs
    }
  }

  public String getCurrentIPFromAPI() {
    String ip = getRequestAsString(API_TO_GET_IP);
    ltLogger.info("Current local machine IP fetched from API: {}", ip);
    return ip;
  }

  private HashMap<String, Object> getRequestBodyForShareLinks(String type, String id) {
    final int[] validExpiryDays = { 3, 7, 10, 30 };

    HashMap<String, Object> body = new HashMap<>();
    body.put("expiresAt", validExpiryDays[new Random().nextInt(validExpiryDays.length)]); // Optimized random generation
    body.put("themeVersion", "v2");
    body.put("isThemeEnabled", true);
    body.put("entityIds", new String[] { id });

    // Set entityType and selectedTab based on 'type'
    if ("test".equals(type)) {
      body.put("entityType", "Automation Test");
      body.put("selectedTab", "home");
    } else {
      body.put("entityType", "Automation Build");
    }

    return body;
  }

  private String getLSHSApiResponseForShareLinks(String entityType, String entityId) {
    String bearerToken = getBearerToken();
    HashMap<String, Object> body = getRequestBodyForShareLinks(entityType, entityId);
    String uri = constructAPIUrl(EnvSetup.API_URL_BASE, GENERATE_SHARE_LINK_API_ENDPOINT);
    ltLogger.info("Generating test share link for {} ID: {} with api: {}", entityType, entityId, uri);
    String response = postRequestWithBearerToken(uri, bearerToken, body).getBody().asString();
    ltLogger.info("Response from {} share link generation API: {}", entityType, response);

    if (response == null || response.isEmpty()) {
      throw new RuntimeException("Failed to generate " + entityType + " share link. Response is :" + response);
    }
    return response;
  }

  public String getTestShareLinkUrl(String sessionId) {
    String testID = getTestIdFromSessionId(sessionId);
    String response = getLSHSApiResponseForShareLinks("test", testID);

    TestShareAPIResponseDTO testShareLinkResponseDTO = convertJsonStringToPojo(response,
      new TypeToken<TestShareAPIResponseDTO>() {
      });

    String testShareUrl = testShareLinkResponseDTO.getShareIdUrl();
    ltLogger.info("Test share link generated successfully: {}", testShareUrl);
    return testShareUrl;
  }

  public String getBuildShareLinkUrl(String buildId) {
    String response = getLSHSApiResponseForShareLinks("build", buildId);

    TestShareAPIResponseDTO testShareLinkResponseDTO = convertJsonStringToPojo(response,
      new TypeToken<TestShareAPIResponseDTO>() {
      });

    String buildShareUrl = testShareLinkResponseDTO.getShareIdUrl();
    ltLogger.info("Build share link generated successfully: {}", buildShareUrl);
    return buildShareUrl;
  }

  public String[] uploadBrowserProfile(String filePath) {
    final String uploadSuccessMessage = "File have been uploaded successfully to our lambda storage";
    String uri = constructAPIUrl(EnvSetup.API_URL_BASE, UPLOAD_BROWSER_PROFILE_API_ENDPOINT);
    ltLogger.info("Uploading browser profile with api url: {}", uri);

    // Prepare the multipart body
    HashMap<String, Object> multipartBody = new HashMap<>();
    multipartBody.put("contentType", REQUEST_BODY_CONTENT_TYPE_MULTIPART_FORM);
    multipartBody.put("profile", new File(filePath));

    String responseString = postRequestWithBasicAuth(uri, multipartBody, EnvSetup.testUserName.get(),
      EnvSetup.testAccessKey.get()).getBody().asString();
    ltLogger.info("Browser profile upload response string: {}", responseString);

    UploadBrowserProfileResponseDTO response = convertJsonStringToPojo(responseString,
      new TypeToken<UploadBrowserProfileResponseDTO>() {
      });
    String status = response.getStatus();
    String message = response.getData().getFirst().getMessage();
    String error = response.getData().getFirst().getError();
    String url = response.getData().getFirst().getUrl();

    CustomAssert.assertTrue(status.equals("success") && message.equals(uploadSuccessMessage) && error.isEmpty(),
      softAssertMessageFormat(UNABLE_TO_UPLOAD_FILE_TO_LAMBDA_STORAGE_ERROR_MESSAGE, "browser profile", status, message,
        error));

    String updatedTime = getCurrentTimeUST();

    return new String[] { url, updatedTime };
  }

  public String verifyBrowserProfileInLambdaStorageAndGetLastUpdatedTimeStamp(String fileName) {
    String uri = constructAPIUrl(EnvSetup.API_URL_BASE, UPLOAD_BROWSER_PROFILE_API_ENDPOINT);
    ltLogger.info("Verifying browser profile in Lambda storage with api url: {}", uri);

    String responseString = getRequestWithBasicAuthAsString(uri, EnvSetup.testUserName.get(),
      EnvSetup.testAccessKey.get());

    GetBrowserProfileResponseDTO response = convertJsonStringToPojo(responseString,
      new TypeToken<GetBrowserProfileResponseDTO>() {
      });
    List<GetBrowserProfileResponseDTO.UploadData> data = response.getData();

    // Verify the existence of the file in Lambda storage
    boolean isFilePresent = data.stream().anyMatch(uploadData -> uploadData.getKey().equals(fileName));
    CustomAssert.assertTrue(isFilePresent,
      softAssertMessageFormat(FILE_NOT_FOUND_IN_LAMBDA_STORAGE_ERROR_MESSAGE, "Browser profile", fileName));

    // Return the last modified timestamp of the file
    return data.stream().filter(uploadData -> uploadData.getKey().equals(fileName)).findFirst()
      .map(GetBrowserProfileResponseDTO.UploadData::getLast_modified_at)
      .orElseThrow(() -> new RuntimeException("File not found in Lambda storage: " + fileName));
  }

  public void deleteBrowserProfile(String filename) {
    String deleteSuccessMessage = "File have been successfully deleted from our lambda storage";
    String uri = constructAPIUrl(EnvSetup.API_URL_BASE, UPLOAD_BROWSER_PROFILE_API_ENDPOINT);
    ltLogger.info("Deleting browser profile with api url: {}", uri);

    HashMap<String, Object> body = new HashMap<>();
    body.put("key", filename);

    Response response = deleteRequestWithBasicAuth(uri, EnvSetup.testUserName.get(), EnvSetup.testAccessKey.get(),
      body);
    ltLogger.info("Browser profile delete response: {}", response.getBody().asPrettyString());

    String status = response.getBody().path("status").toString();
    String message = response.getBody().path("message").toString();

    CustomAssert.assertTrue(status.equals("success") && message.equals(deleteSuccessMessage),
      softAssertMessageFormat(UNABLE_TO_DELETE_FILE_FROM_LAMBDA_STORAGE_ERROR_MESSAGE, "browser profile", status,
        message));
  }
}
