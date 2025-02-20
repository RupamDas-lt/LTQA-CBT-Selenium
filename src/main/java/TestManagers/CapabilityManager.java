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
import utility.EnvSetup;

import java.io.IOException;
import java.util.*;

import static utility.EnvSetup.*;
import static utility.FrameworkConstants.*;

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
    if (!StringUtils.isNullOrEmpty((String) capabilityMap.getOrDefault(TUNNEL, "")))
      capabilityMap.put(TUNNEL_NAME, EnvSetup.TEST_TUNNEL_NAME.get());
    if (StringUtils.isNullOrEmpty((String) capabilityMap.getOrDefault(TEST_NAME, "")))
      capabilityMap.put(TEST_NAME, capsString);
    if (capabilityMap.getOrDefault(LOAD_PUBLIC_EXTENSION, "false").equals("true"))
      insertToMapWithRandom(capabilityMap, LOAD_PUBLIC_EXTENSION, DASHLANE_EXTENSION_PUBLIC_URL, String.class,
        String[].class);
  }

  private void setRandomValue(HashMap<String, Object> capabilityMap) {
    for (String key : capabilityMap.keySet()) {
      if (capabilityMap.get(key).toString().equals(".*")) {
        String randomValue = "";
        switch (key) {
        case "geoLocation":
          randomValue = getRandomGeoLocation();
          break;
        case "resolution":
          randomValue = getRandomResolution(capabilityMap.get("platform").toString());
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
      JsonNode rootNode = objectMapper.readTree(readFileContent(GEOLOCATION_DATA_PATH));
      JsonNode geoDataArray = rootNode.path("geoData");
      if (geoDataArray.isArray() && !geoDataArray.isEmpty()) {
        Random random = new Random();
        int randomIndex = random.nextInt(geoDataArray.size());
        JsonNode randomGeoObject = geoDataArray.get(randomIndex);
        TEST_VERIFICATION_DATA.get().put("geoLocation", randomGeoObject.path("countryName").asText());
        return randomGeoObject.path("countryCode").asText();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  private String getRandomResolution(String platform) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode rootNode = objectMapper.readTree(readFileContent(RESOLUTION_DATA_PATH));
      JsonNode resDataArray;
      if (platform.toLowerCase().contains("win"))
        resDataArray = rootNode.path("win");
      else if (platform.toLowerCase().contains("ubuntu"))
        resDataArray = rootNode.path("ubuntu");
      else
        resDataArray = rootNode.path("mac");
      if (resDataArray.isArray() && !resDataArray.isEmpty()) {
        Random random = new Random();
        int randomIndex = random.nextInt(resDataArray.size());
        String res = resDataArray.get(randomIndex).asText();
        TEST_VERIFICATION_DATA.get().put("resolution", res);
        return res;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void buildTestCapability(String capabilityString, String... capsType) {
    String expectedCapsType = capsType.length > 0 ? capsType[0] : "desiredCapabilities";
    HashMap<String, Object> capabilityMap = buildCapabilityMap(capabilityString);
    setFinalCapabilities(capabilityMap, expectedCapsType);
  }

  private HashMap<String, Object> buildCapabilityMap(String capabilityString) {
    capsString = capabilityString;
    // Get hashmap from caps string and build caps hashmap
    HashMap<String, Object> capabilityMap = getHashMapFromString(capabilityString);
    // Set default custom values to caps map
    setCustomValues(capabilityMap);
    // Set random values if applicable
    if (capabilityString.contains(".*")) {
      setRandomValue(capabilityMap);
    }
    // If user is passing any custom caps as env variable then set them to capabilityMap
    mergeCustomTestCaps(capabilityMap);
    // Before creating caps object finally get the platform name based on platform keyword
    updatePlatform(capabilityMap);
    return capabilityMap;
  }

  private void mergeCustomTestCaps(Map<String, Object> capabilityMap) {
    Optional.ofNullable(System.getProperty(CUSTOM_TEST_CAPS)).filter(caps -> !caps.isEmpty()).ifPresent(customCaps -> {
      ltLogger.info("Applying custom test capabilities: {}", customCaps);
      capabilityMap.putAll(getHashMapFromString(customCaps));
    });
  }

  private void updatePlatform(Map<String, Object> capabilityMap) {
    String platform = (String) capabilityMap.get("platform");
    capabilityMap.put("platform", osKeywordToTemplateNameMap.get(platform));
    ltLogger.info("Updating platform: {}", platform);
  }

  private void setFinalCapabilities(HashMap<String, Object> capabilityMap, String expectedCapsType) {
    TEST_CAPS_MAP.set(capabilityMap);
    ltLogger.info("Final Test caps: {}", TEST_CAPS_MAP.get());
    boolean hasExtensions = capabilityMap.containsKey(LOAD_PUBLIC_EXTENSION) || capabilityMap.containsKey(
      LOAD_PRIVATE_EXTENSION);
    IS_EXTENSION_TEST.set(hasExtensions);
    ltLogger.info("Is test contains extensions: {}", IS_EXTENSION_TEST.get());
    if (TEST_ENV.equals("local") || expectedCapsType.equals("firstMatch")) {
      createTestCapsWithFirstMatch(new HashMap<>(capabilityMap));
    } else {
      createTestCapsWithDesiredCaps(capabilityMap);
    }

    TEST_CAPS.set(capabilities);
    ltLogger.info("Test caps set in LocalThread: {}", TEST_CAPS.get());
  }
}
