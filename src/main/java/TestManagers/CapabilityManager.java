package TestManagers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.cj.util.StringUtils;
import factory.BrowserType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariOptions;
import utility.BaseClass;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import static utility.EnvSetup.*;
import static utility.FrameworkConstants.GEOLOCATION_DATA_PATH;
import static utility.FrameworkConstants.LT_OPTIONS;

public class CapabilityManager extends BaseClass {
  private final Logger ltLogger = LogManager.getLogger(CapabilityManager.class);
  private final String[] randomValueSupportedCaps = new String[] { "geoLocation" };
  MutableCapabilities capabilities;
  String capsString;

  private void createTestCapsWithFirstMatch(HashMap<String, Object> capsHash) {
    String browserName = capsHash.get("browserName").toString();
    BrowserType browserType = BrowserType.valueOf(browserName.toUpperCase());
    capsHash.remove("browserName");
    capabilities = switch (browserType) {
      case CHROME -> new ChromeOptions();
      case FIREFOX -> new FirefoxOptions();
      case EDGE -> new EdgeOptions();
      case SAFARI -> new SafariOptions();
    };
    String platformName = capsHash.getOrDefault("platform", "win10").toString();
    capsHash.remove("platform");
    String version = capsHash.getOrDefault("version", "latest").toString();
    capsHash.remove("version");
    // platform and version are not valid W3C capabilities
    if (!TEST_ENV.equals("local")) {
      capabilities.setCapability("platformName", platformName);
    }
    capabilities.setCapability("browserVersion", version);
    if (TEST_ENV.equals("local")) {
      for (String key : capsHash.keySet())
        capabilities.setCapability(key, capsHash.get(key));
    } else {
      capabilities.setCapability(LT_OPTIONS, capsHash);
    }
    ltLogger.info("Created test caps with first match: {}", capabilities.asMap());
  }

  private void createTestCapsWithDesiredCaps(HashMap<String, Object> capsHash) {
    DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
    desiredCapabilities.setCapability(LT_OPTIONS, capsHash);
    ltLogger.info("Desired caps details: {}", desiredCapabilities.asMap());
    capabilities = desiredCapabilities;
  }

  private void setCustomValues(HashMap<String, Object> capabilityMap) {
    if (!StringUtils.isNullOrEmpty((String) capabilityMap.getOrDefault("tunnel", "")))
      capabilityMap.put("tunnelName", TUNNEL_NAME.get());
    if (StringUtils.isNullOrEmpty((String) capabilityMap.getOrDefault("name", "")))
      capabilityMap.put("name", capsString);
  }

  private void setRandomValue(HashMap<String, Object> capabilityMap) {
    for (String key : capabilityMap.keySet()) {
      if (capabilityMap.get(key).toString().equals(".*")) {
        String randomValue = "";
        switch (key) {
        case "geoLocation":
          randomValue = getRandomGeoLocation();
          break;
        case "browserName":
          //          randomValue = getRandomBrowser();
          break;
        default:
          throw new RuntimeException(
            key + " this capability doesn't support random value. Supported values: " + Arrays.asList(
              randomValueSupportedCaps));
        }
        capabilityMap.put(key, randomValue);
      }
    }
  }

  private String getRandomGeoLocation() {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode rootNode = objectMapper.readTree(new File(GEOLOCATION_DATA_PATH));
      JsonNode geoDataArray = rootNode.path("geoData");
      if (geoDataArray.isArray() && !geoDataArray.isEmpty()) {
        Random random = new Random();
        int randomIndex = random.nextInt(geoDataArray.size());
        JsonNode randomGeoObject = geoDataArray.get(randomIndex);
        TEST_VERIFICATION_DATA.get().put("geoLocation", randomGeoObject.path("countryName").asText());
        return randomGeoObject.path("countryCode").asText();
      }
    } catch (IOException e) {
      e.printStackTrace(); // Log error
    }

    return null; // Return null if something goes wrong
  }

  public void buildTestCapability(String capabilityString, String... capsType) {
    boolean randomCaps = false;
    capsString = capabilityString;
    if (capabilityString.contains(".*"))
      randomCaps = true;
    String expectedCapsType = capsType.length > 0 ? capsType[0] : "desiredCapabilities";
    HashMap<String, Object> capabilityMap = getHashMapFromString(capabilityString);
    setCustomValues(capabilityMap);
    if (randomCaps)
      setRandomValue(capabilityMap);
    GIVEN_TEST_CAPS_MAP.set(capabilityMap);
    if (TEST_ENV.equals("local") || expectedCapsType.equals("firstMatch"))
      createTestCapsWithFirstMatch((HashMap<String, Object>) capabilityMap.clone());
    else
      createTestCapsWithDesiredCaps(capabilityMap);
    TEST_CAPS.set(capabilities);
    ltLogger.info("Test caps set in LocalThread: {}", TEST_CAPS.get().toString());
  }
}
