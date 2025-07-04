package utility;

import com.fasterxml.jackson.databind.JsonNode;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EnvSetup {

  public static final String TEST_ENV = System.getProperty("ENV");
  public static final Map<String, String> config = getEnvConfig();
  public static final String USER_NAME = config.get("test_env_username");
  public static final String ACCESS_KEY = config.get("test_env_key");
  public static final String USER_EMAIL = config.get("test_env_email");
  public static final String USER_PASS = config.get("test_env_pass");
  public static final String CLIENT_ENV_USER_NAME = config.get("client_env_username");
  public static final String CLIENT_ENV_ACCESS_KEY = config.get("client_env_key");
  public static final String GRID_URL = config.get("test_env_hub_url");
  public static final String CLIENT_GRID_URL = config.get("client_env_hub_url");
  public static final String API_URL_BASE = config.get("test_env_api_url");
  public static final String CLIENT_API_URL_BASE = config.get("client_env_api_url");
  public static final String TEST_DASHBOARD_URL_BASE = config.get("test_dashboard_url");
  public static final String CLIENT_DASHBOARD_URL_BASE = config.get("client_dashboard_url");
  public static final String TEST_RETINA_URL_BASE = config.get("test_retina_url");
  public static final String CLIENT_RETINA_URL_BASE = config.get("client_retina_url");
  public static final String IS_GDPR_TEST_CONFIG = config.getOrDefault("is_gdpr", "false");

  // Test user configs
  public static final ThreadLocal<String> testUserName = new ThreadLocal<>();
  public static final ThreadLocal<String> testAccessKey = new ThreadLocal<>();
  public static final ThreadLocal<String> testEmail = new ThreadLocal<>();
  public static final ThreadLocal<String> testPassword = new ThreadLocal<>();
  public static final ThreadLocal<String> testGridUrl = new ThreadLocal<>();
  public static final ThreadLocal<String> clientTestUserName = new ThreadLocal<>();
  public static final ThreadLocal<String> clientTestAccessKey = new ThreadLocal<>();
  public static final ThreadLocal<String> clientTestGridUrl = new ThreadLocal<>();

  // Capabilities and Drivers
  public static final ThreadLocal<WebDriver> testDriver = new ThreadLocal<WebDriver>();
  public static final ThreadLocal<WebDriver> clientDriver = new ThreadLocal<WebDriver>();
  public static final ThreadLocal<MutableCapabilities> TEST_CAPS = new ThreadLocal<>();
  public static final ThreadLocal<HashMap<String, Object>> CUSTOM_TEST_CAPS_MAP_FROM_ENV = new ThreadLocal<HashMap<String, Object>>();
  public static final ThreadLocal<Map<String, Object>> TEST_CAPS_MAP = new ThreadLocal<Map<String, Object>>();
  public static final ThreadLocal<MutableCapabilities> CLIENT_TEST_CAPS = new ThreadLocal<>();
  public static final ThreadLocal<Map<String, Object>> CLIENT_TEST_CAPS_MAP = new ThreadLocal<>();

  // Test utilities
  public static final ThreadLocal<CustomSoftAssert> SOFT_ASSERT = ThreadLocal.withInitial(CustomSoftAssert::new);
  public static final ThreadLocal<CustomSoftAssert> CLIENT_SOFT_ASSERT = ThreadLocal.withInitial(CustomSoftAssert::new);
  public static final ThreadLocal<Boolean> IS_UI_VERIFICATION_ENABLED = ThreadLocal.withInitial(() -> false);
  public static final ThreadLocal<String> TEST_SESSION_ID = ThreadLocal.withInitial(() -> "");
  public static final ThreadLocal<String> TEST_TEST_ID = ThreadLocal.withInitial(() -> "");
  public static final ThreadLocal<String> BUILD_ID = ThreadLocal.withInitial(() -> "");
  public static final ThreadLocal<String> CLIENT_SESSION_ID = new ThreadLocal<>();
  public static final ThreadLocal<HashMap<String, Object>> TEST_REPORT = ThreadLocal.withInitial(HashMap::new);
  public static final ThreadLocal<String> TEST_TUNNEL_NAME = new ThreadLocal<>();
  public static final ThreadLocal<String> TEST_TUNNEL_ID = new ThreadLocal<>();
  public static final ThreadLocal<String> TUNNEL_START_COMMAND = new ThreadLocal<>();
  public static final ThreadLocal<HashMap<FrameworkConstants.testVerificationDataKeys, Object>> TEST_VERIFICATION_DATA = ThreadLocal.withInitial(
    HashMap::new);
  public static final ThreadLocal<Boolean> IS_EXTENSION_TEST = new ThreadLocal<>();
  public static final ThreadLocal<Boolean> IS_EXTENSION_CLIENT_TEST = new ThreadLocal<>();
  public static final ThreadLocal<String> TEST_SCENARIO_NAME = new ThreadLocal<>();
  public static final ThreadLocal<Integer> SESSION_COMMAND_LOGS_COUNT_FROM_TEST_API = new ThreadLocal<>();
  public static final ThreadLocal<Integer> SESSION_EXCEPTION_LOGS_COUNT_FROM_TEST_API = new ThreadLocal<>();
  public static final ThreadLocal<Integer> SESSION_VISUAL_LOGS_COUNT_FROM_TEST_API = new ThreadLocal<>();
  public static final ThreadLocal<JsonNode> TEST_DETAIL_API_RESPONSE = new ThreadLocal<>();
  public static final ThreadLocal<JsonNode> TEST_FEATURE_FLAG_DETAILS = new ThreadLocal<>();

  private static Map<String, String> getEnvConfig() {
    if (TEST_ENV.equalsIgnoreCase("local")) {
      return new HashMap<>();
    }
    File yamlFile = new File("src/test/resources/cucumber.yaml");
    Yaml ymlFileReader = new Yaml();
    try (InputStream inStr = new FileInputStream(yamlFile)) {
      Map<String, Object> ymlObj = ymlFileReader.load(inStr);
      Object envValue = ymlObj.get(TEST_ENV);
      if (envValue instanceof Map) {
        @SuppressWarnings("unchecked") Map<String, String> envConfig = (Map<String, String>) envValue;
        return envConfig;
      } else {
        throw new RuntimeException(
          String.format("ERROR: Expected a Map<String, String> but got %s", envValue.getClass().getName()));
      }
    } catch (FileNotFoundException e) {
      throw new RuntimeException(String.format("ERROR: cucumber.yml file is not found.%n%s", e));
    } catch (ClassCastException e) {
      throw new RuntimeException(
        String.format("ERROR: The loaded value cannot be cast to Map<String, String>.%n%s", e));
    } catch (Exception e) {
      throw new RuntimeException(
        String.format("ERROR: Unexpected error occurred while loading the configuration.%n%s", e));
    }
  }

  // Test Report
  public static final ThreadLocal<Map<String, Map<String, String>>> ASSERTION_ERROR_TO_HASH_KEY_MAP = ThreadLocal.withInitial(
    HashMap::new);
  public static final ThreadLocal<Map<String, String>> FAILED_ASSERTION_ERROR_TO_HASH_KEY_MAP = ThreadLocal.withInitial(
    HashMap::new);
}
