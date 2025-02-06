package automationHelper;

import DTOs.SwaggerAPIs.GetSessionResponseDTO;
import TestManagers.ApiManager;
import com.google.gson.reflect.TypeToken;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.EnvSetup;

import java.lang.reflect.Field;
import java.util.HashMap;

import static utility.FrameworkConstants.HTTPS;
import static utility.FrameworkConstants.SUMO_LOGIC_URL;

public class AutomationAPIHelper extends ApiManager {

  // API end-points
  private static final String SESSIONS_API_ENDPOINT = "/automation/api/v1/sessions/";
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
}
