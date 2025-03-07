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
  private final String[] randomValueSupportedCaps = new String[] { "geoLocation", "resolution", "version", "timezone" };
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

  private void removeSpecificCaps(Map<String, Object> capsMap, String envVariable) {
    String capsToRemove = System.getProperty(envVariable, "NA");
    ltLogger.info("Removing specific caps: {}", capsToRemove);
    if (capsToRemove.equals("NA"))
      return;
    String[] capsToRemoveArray = capsToRemove.split(",");
    for (String capToRemove : capsToRemoveArray) {
      String regex = "(CAPABILITY_NAME=[^,]+(,|$))".replace("CAPABILITY_NAME", capToRemove);
      capsMap.remove(capToRemove);
      capsString = capsString.replaceAll(regex, "");
    }
    ltLogger.info("Updated caps map and string after removing {}, caps string: {}, caps map: {}", capsToRemove,
      capsString, capsMap);
  }

  private void setRandomValue(Map<String, Object> capabilityMap) {
    capabilityMap.entrySet().stream().filter(entry -> entry.getValue().toString().equals(".*")).forEach(entry -> {
      String key = entry.getKey();
      String randomValue = switch (key) {
        case "timezone" -> getRandomTimeZone(capabilityMap.get("platform").toString());
        case "geoLocation" -> getRandomGeoLocation();
        case "resolution" -> getRandomResolution(capabilityMap.get("platform").toString());
        case "version" -> getRandomBrowserVersionFromTopSix((String) capabilityMap.getOrDefault("browserName", ""));
        case "selenium_version" -> getRandomSelenium4Version();
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

  private String getRandomBrowserVersionFromTopSix(String browserName) {
    if (browserName.matches(".*(safari|opera|ie).*"))
      return "latest";
    String[] topFive = { "latest", "latest-1", "latest-2", "latest-3", "latest-4", "latest-5" };
    return topFive[new Random().nextInt(topFive.length)];
  }

  private String getRandomTimeZone(String platform) {
    String timeZoneIndex = platform.toLowerCase().contains("win") ? "win" : "others";
    ltLogger.info("timezone index: {}", timeZoneIndex);
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode rootNode = objectMapper.readTree(readFileContent(TIMEZONE_DATA_PATH));
      List<String> timezoneIds = new ArrayList<>();
      rootNode.path(timeZoneIndex).fields().forEachRemaining(entry -> timezoneIds.add(entry.getKey()));
      Random random = new Random();
      int randomIndex = random.nextInt(timezoneIds.size());
      return timezoneIds.get(randomIndex);

    } catch (IOException e) {
      throw new RuntimeException("Unable to read timezone data", e);
    }
  }

  private String getRandomSelenium4Version() {
    String[] topFive = { "latest", "latest-1", "latest-2", "4.13.0", "4.5.0", "4.0.0" };
    return topFive[new Random().nextInt(topFive.length)];
  }

  private void buildCapabilities(String capabilityString, String purpose, String... capsType) {
    String expectedCapsType = capsType.length > 0 ? capsType[0] : "desiredCapabilities";
    Map<String, Object> capabilityMap = purpose.equals("client") ?
      buildCapabilityMap(capabilityString, CUSTOM_CLIENT_CAPS, REMOVE_CLIENT_TEST_CAPS) :
      buildCapabilityMap(capabilityString, CUSTOM_TEST_CAPS, REMOVE_TEST_CAPS);
    setFinalCapabilities(capabilityMap, expectedCapsType, purpose);
  }

  public void buildTestCapability(String capabilityString, String... capsType) {
    buildCapabilities(capabilityString, "test", capsType);
  }

  public void buildClientTestCapability(String capabilityString, String... capsType) {
    buildCapabilities(capabilityString, "client", capsType);
  }

  private Map<String, Object> buildCapabilityMap(String capabilityString, String customCapsSource,
    String... removeCapsSource) {
    capsString = capabilityString;
    // Get hashmap from caps string and build caps hashmap
    Map<String, Object> capabilityMap = new ConcurrentHashMap<>(getHashMapFromString(capsString));

    // Remove specific caps based on env value
    if (removeCapsSource != null && removeCapsSource.length > 0) {
      removeSpecificCaps(capabilityMap, removeCapsSource[0]);
    }

    // Set default custom values to caps map
    setCustomValues(capabilityMap);

    // Set random values if applicable
    if (capsString.contains(".*")) {
      setRandomValue(capabilityMap);
    }

    // If user is passing any custom caps as env variable then set them to capabilityMap
    mergeCustomTestCaps(capabilityMap, customCapsSource);

    // Before creating caps object finally get the platform name based on platform keyword
    updatePlatform(capabilityMap);
    return capabilityMap;
  }

  private void mergeCustomTestCaps(Map<String, Object> capabilityMap, String customCapsSource) {
    Optional.ofNullable(System.getProperty(customCapsSource)).filter(caps -> !caps.isEmpty()).ifPresent(customCaps -> {
      ltLogger.info("Applying custom test capabilities: {}", customCaps);
      capabilityMap.putAll(getHashMapFromString(customCaps));
    });
  }

  private void updatePlatform(Map<String, Object> capabilityMap) {
    String platform = (String) capabilityMap.get("platform");
    capabilityMap.put("platform", osKeywordToTemplateNameMap.get(platform));
    ltLogger.info("Updating platform: {}", platform);
  }

  private void setFinalCapabilities(Map<String, Object> capabilityMap, String expectedCapsType, String testEnv) {

    ThreadLocal<Map<String, Object>> capsMap = (testEnv.equals("client")) ? CLIENT_TEST_CAPS_MAP : TEST_CAPS_MAP;
    ThreadLocal<Boolean> isExtensionTest = (testEnv.equals("client")) ? IS_EXTENSION_CLIENT_TEST : IS_EXTENSION_TEST;
    ThreadLocal<MutableCapabilities> finalCaps = (testEnv.equals("client")) ? CLIENT_TEST_CAPS : TEST_CAPS;
    capsMap.set(capabilityMap);
    ltLogger.info("Final {} caps: {}", testEnv, capsMap.get());
    boolean hasExtensions = capabilityMap.containsKey(LOAD_PUBLIC_EXTENSION) || capabilityMap.containsKey(
      LOAD_PRIVATE_EXTENSION);
    isExtensionTest.set(hasExtensions);
    ltLogger.info("Is {} contains extensions: {}", testEnv, isExtensionTest.get());
    CapsType capsType = (TEST_ENV.equals("local") || expectedCapsType.equals("firstMatch")) ?
      CapsType.FIRST_MATCH :
      CapsType.DESIRED_CAPABILITIES;
    createTestCaps(new HashMap<>(capabilityMap), capsType);
    finalCaps.set(capabilities);
    ltLogger.info("{} caps set in LocalThread: {}", testEnv, CLIENT_TEST_CAPS.get());
  }

  private enum CapsType {
    DESIRED_CAPABILITIES, FIRST_MATCH
  }
}
