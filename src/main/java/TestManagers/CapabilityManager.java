package TestManagers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.cj.util.StringUtils;
import factory.BrowserType;
import lombok.NonNull;
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
import java.util.concurrent.ConcurrentHashMap;

import static utility.EnvSetup.*;
import static utility.FrameworkConstants.*;

public class CapabilityManager extends BaseClass {
  private final Logger ltLogger = LogManager.getLogger(CapabilityManager.class);
  private final String[] randomValueSupportedCaps = new String[] { "geoLocation", "resolution" };
  MutableCapabilities capabilities;
  String capsString;

  private void createTestCaps(Map<String, Object> capsHash, CapsType capsType) {
    if (capsType == CapsType.FIRST_MATCH) {
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
      if (!TEST_ENV.equals("local")) {
        capabilities.setCapability("platformName", platformName);
      }
      capabilities.setCapability("browserVersion", version);
      if (TEST_ENV.equals("local")) {
        capsHash.forEach(capabilities::setCapability);
      } else {
        capabilities.setCapability(LT_OPTIONS, capsHash);
      }
    } else {
      DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
      desiredCapabilities.setCapability(LT_OPTIONS, capsHash);
      capabilities = desiredCapabilities;
    }
    ltLogger.info("Created test caps: {}", capabilities.asMap());
  }

  private void setCustomValues(@NonNull Map<String, Object> capabilityMap) {
    if (!StringUtils.isNullOrEmpty((String) capabilityMap.getOrDefault(TUNNEL, "")))
      capabilityMap.put(TUNNEL_NAME, EnvSetup.TEST_TUNNEL_NAME.get());

    if (StringUtils.isNullOrEmpty((String) capabilityMap.getOrDefault(TEST_NAME, "")))
      capabilityMap.put(TEST_NAME, capsString);

    if (capabilityMap.getOrDefault(LOAD_PUBLIC_EXTENSION, "false").equals("true"))
      insertToMapWithRandom(capabilityMap, LOAD_PUBLIC_EXTENSION, DASHLANE_EXTENSION_PUBLIC_URL, String.class,
        String[].class);
  }

  private void setRandomValue(Map<String, Object> capabilityMap) {
    capabilityMap.entrySet().stream().filter(entry -> entry.getValue().toString().equals(".*")).forEach(entry -> {
      String key = entry.getKey();
      String randomValue = switch (key) {
        case "geoLocation" -> getRandomGeoLocation();
        case "resolution" -> getRandomResolution(capabilityMap.get("platform").toString());
        default -> throw new RuntimeException(
          key + " this capability doesn't support random value. Supported values: " + Arrays.asList(
            randomValueSupportedCaps));
      };
      ltLogger.info("Set {} caps value: {}", key, randomValue);
      capabilityMap.put(key, randomValue);
    });
  }

  private String getRandomGeoLocation() {
    try {
      var objectMapper = new ObjectMapper();
      var rootNode = objectMapper.readTree(readFileContent(GEOLOCATION_DATA_PATH));
      var geoDataArray = rootNode.path("geoData");
      if (geoDataArray.isArray() && !geoDataArray.isEmpty()) {
        Random random = new Random();
        int randomIndex = random.nextInt(geoDataArray.size());
        JsonNode randomGeoObject = geoDataArray.get(randomIndex);
        TEST_VERIFICATION_DATA.get().put("geoLocation", randomGeoObject.path("countryName").asText());
        return randomGeoObject.path("countryCode").asText();
      }
    } catch (IOException e) {
      throw new RuntimeException("Unable to read geolocation data", e);
    }

    return null;
  }

  private String getRandomResolution(String platform) {
    try {
      var objectMapper = new ObjectMapper();
      var rootNode = objectMapper.readTree(readFileContent(RESOLUTION_DATA_PATH));
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
      throw new RuntimeException("Unable to read resolution data", e);
    }
    return null;
  }

  public void buildTestCapability(String capabilityString, String... capsType) {
    String expectedCapsType = capsType.length > 0 ? capsType[0] : "desiredCapabilities";
    Map<String, Object> capabilityMap = buildCapabilityMap(capabilityString);
    setFinalCapabilities(capabilityMap, expectedCapsType);
  }

  private Map<String, Object> buildCapabilityMap(String capabilityString) {
    capsString = capabilityString;
    // Get hashmap from caps string and build caps hashmap
    Map<String, Object> capabilityMap = new ConcurrentHashMap<>(getHashMapFromString(capabilityString));
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

  private void setFinalCapabilities(Map<String, Object> capabilityMap, String expectedCapsType) {
    TEST_CAPS_MAP.set(capabilityMap);
    ltLogger.info("Final Test caps: {}", TEST_CAPS_MAP.get());
    boolean hasExtensions = capabilityMap.containsKey(LOAD_PUBLIC_EXTENSION) || capabilityMap.containsKey(
      LOAD_PRIVATE_EXTENSION);
    IS_EXTENSION_TEST.set(hasExtensions);
    ltLogger.info("Is test contains extensions: {}", IS_EXTENSION_TEST.get());
    CapsType capsType = (TEST_ENV.equals("local") || expectedCapsType.equals("firstMatch")) ?
      CapsType.FIRST_MATCH :
      CapsType.DESIRED_CAPABILITIES;
    createTestCaps(new HashMap<>(capabilityMap), capsType);

    TEST_CAPS.set(capabilities);
    ltLogger.info("Test caps set in LocalThread: {}", TEST_CAPS.get());
  }

  private enum CapsType {
    DESIRED_CAPABILITIES, FIRST_MATCH
  }
}
