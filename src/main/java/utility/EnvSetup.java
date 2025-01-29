package utility;

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
  public static final String GRID_URL = config.get("test_env_hub_url");

  // Test user configs
  public static final ThreadLocal<String> testUserName = new ThreadLocal<>();
  public static final ThreadLocal<String> testAccessKey = new ThreadLocal<>();
  public static final ThreadLocal<String> testEmail = new ThreadLocal<>();
  public static final ThreadLocal<String> testPassword = new ThreadLocal<>();
  public static final ThreadLocal<String> testGridUrl = new ThreadLocal<>();

  // Capabilities and Drivers
  public static final ThreadLocal<WebDriver> testDriver = new ThreadLocal<WebDriver>();
  public static final ThreadLocal<WebDriver> clientDriver = new ThreadLocal<WebDriver>();
  public static final ThreadLocal<MutableCapabilities> TEST_CAPS = new ThreadLocal<>();
  public static final ThreadLocal<HashMap<String, Object>> GIVEN_TEST_CAPS_MAP = new ThreadLocal<HashMap<String, Object>>();

  // Test utilities
  public static final ThreadLocal<CustomSoftAssert> SOFT_ASSERT = new ThreadLocal<>();
  public static final ThreadLocal<String> TEST_SESSION_ID = new ThreadLocal<>();
  public static final ThreadLocal<HashMap<String, Object>> TEST_REPORT = new ThreadLocal<>();
  public static final ThreadLocal<String> TUNNEL_NAME = new ThreadLocal<>();
  public static final ThreadLocal<String> TUNNEL_START_COMMAND = new ThreadLocal<>();

  public static Map<String, String> getEnvConfig() {
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
}
