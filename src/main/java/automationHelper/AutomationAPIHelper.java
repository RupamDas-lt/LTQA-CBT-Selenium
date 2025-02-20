package automationHelper;

import DTOs.SwaggerAPIs.GetSessionResponseDTO;
import TestManagers.ApiManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.reflect.TypeToken;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.EnvSetup;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utility.FrameworkConstants.*;

public class AutomationAPIHelper extends ApiManager {

  // API end-points
  private static final String SESSIONS_API_ENDPOINT = "/automation/api/v1/sessions/";
  private static final String GEOLOCATIONS_API_ENDPOINT = "/api/v1/geolocation?unique=true";
  private static final String BROWSER_VERSIONS_API_ENDPOINT = "/api/v2/capability?grid=selenium&browser=<BROWSER_NAME>&os=<TEMPLATE>";

  private final Logger ltLogger = LogManager.getLogger(AutomationAPIHelper.class);

  public String constructAPIUrl(String uriBase, String endpoint, String... sessionId) {
    String url = sessionId.length > 0 ? HTTPS + uriBase + endpoint + sessionId[0] : HTTPS + uriBase + endpoint;
    ltLogger.info("URL: {}", url);
    return url;
  }

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
    customData.put("message", "New_Framework_Testing");
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
    String filePath = BROWSER_VERSIONS_DATA_PATH.replace("<BROWSER_NAME>", browserName).replace("<TEMPLATE>", template);
    ltLogger.info("Browser versions data path: {}", filePath);

    String browserVersionFetchUrl = constructAPIUrl(EnvSetup.API_URL_BASE, BROWSER_VERSIONS_API_ENDPOINT).replace(
      "<BROWSER_NAME>", browserName).replace("<TEMPLATE>", osTemplateNameToKeywordMap.get(template));
    ltLogger.info("API for browser version fetch: {}", browserVersionFetchUrl);

    fetchDataAndWriteResponseToFile(browserVersionFetchUrl, filePath);

    try {
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode rootNode = objectMapper.readTree(readFileContent(filePath));
      JsonNode versionsData = rootNode.path("versions");

      Map<String, String> versionMap = new HashMap<>();
      List<String> stableVersions = new ArrayList<>();

      for (JsonNode version : versionsData) {
        String channelType = version.path("channel_type").asText().toLowerCase();
        String versionNumber = version.path("version").asText();

        switch (channelType) {
        case "dev" -> versionMap.put("dev", versionNumber);
        case "beta" -> versionMap.put("beta", versionNumber);
        default -> stableVersions.add(versionNumber);
        }
      }

      ltLogger.info("{} versions on template {}: Dev: {}, Beta: {}, Stable: {}", browserName, template,
        versionMap.get("dev"), versionMap.get("beta"), stableVersions);

      return switch (keyword.toLowerCase()) {
        case "dev" -> versionMap.get("dev");
        case "beta" -> versionMap.get("beta");
        case "latest" -> stableVersions.getFirst();
        default -> {
          String[] versionParams = keyword.split("-");
          int index = Integer.parseInt(versionParams[versionParams.length - 1]);
          yield stableVersions.get(index - 1);
        }
      };
    } catch (IOException e) {
      throw new RuntimeException("Failed to fetch or parse browser versions data", e);
    }
  }
}
