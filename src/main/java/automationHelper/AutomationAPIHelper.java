package automationHelper;

import TestManagers.ApiManager;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.EnvSetup;

import java.util.HashMap;

import static utility.FrameworkConstants.HTTPS;

public class AutomationAPIHelper extends ApiManager {
  // API sample payloads
  public static final String TEST_STATUS_UPDATE_PAYLOAD = "{\"status_ind\":\"<UPDATED_TEST_STATUS>\",\"reason\":\"<UPDATED_TEST_REMARKS>\"}";
  // API end-points
  private static final String SESSIONS_API_ENDPOINT = "/automation/api/v1/sessions/";
  private final Logger ltLogger = LogManager.getLogger(AutomationAPIHelper.class);
  // Json paths
  private final String SESSION_DETAILS_API_STATUS_PATH = "status";
  private final String SESSION_DETAILS_API_SESSION_STATUS_PATH = "data.status_ind";

  public String constructAPIUrl(String uriBase, String endpoint, String... sessionId) {
    String url = sessionId.length > 0 ? HTTPS + uriBase + endpoint + sessionId[0] : HTTPS + uriBase + endpoint;
    ltLogger.info("URL: {}", url);
    return url;
  }

  public void updateSessionDetailsViaAPI(String session_id, HashMap<String, String> sessionDetails) {
    String sessionAPIUrl = constructAPIUrl(EnvSetup.API_URL_BASE, SESSIONS_API_ENDPOINT, session_id);
    String payload = createStringBodyFromHashMap(sessionDetails);
    ltLogger.info("Update Session Details: {}", payload);
    Response response = patchRequestWithBasicAuth(sessionAPIUrl, EnvSetup.testUserName.get(),
      EnvSetup.testAccessKey.get(), payload);
    ltLogger.info("Response Code: {}", response.getStatusCode());
    System.out.println("Response Code: " + response.getStatusCode());
  }

  public String getSpecificSessionDetailsViaAPI(String session_id) {
    String sessionAPIUrl = constructAPIUrl(EnvSetup.API_URL_BASE, SESSIONS_API_ENDPOINT, session_id);
    String testStatus = getRequestAndExtractJsonPath(sessionAPIUrl, SESSION_DETAILS_API_SESSION_STATUS_PATH,
      EnvSetup.testUserName.get(), EnvSetup.testAccessKey.get());
    ltLogger.info("Test Status : {}", testStatus);
    return testStatus;
  }
}
