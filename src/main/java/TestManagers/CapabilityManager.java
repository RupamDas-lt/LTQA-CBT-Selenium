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
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariOptions;
import utility.BaseClass;
import utility.EnvSetup;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static utility.EnvSetup.*;
import static utility.FrameworkConstants.*;

/**
 * This class is responsible for managing the capabilities for different cloud platforms
 * This can take capabilities as a string and build the final capabilities object
 * The string can contain capabilities in the format of:
 * CAPABILITY_NAME=value,CAPABILITY_NAME2=value2,...
 * Always use getExtendedHashMapFromString while converting the string to a map as it provides support for nested capabilities
 * It supports both desired capabilities and first match capabilities
 */
public class CapabilityManager extends BaseClass {
  private final Logger ltLogger = LogManager.getLogger(CapabilityManager.class);
  private final String[] randomValueSupportedCaps = new String[] { "geoLocation", "resolution", "version", "timezone" };
  MutableCapabilities capabilities;
  String capsString;

  private void createTestCaps(Map<String, Object> capsHash, CapsType capsType, String capabilityRootName) {
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
        capabilities.setCapability(capabilityRootName, capsHash);
      }
    } else {
      DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
      desiredCapabilities.setCapability(capabilityRootName, capsHash);
      capabilities = desiredCapabilities;
    }
    ltLogger.info("Created test caps: {}", capabilities.asMap());
  }

  private String getTestBuildName(String purpose) {
    String buildNamePrefix = "CBT_Selenium_" + purpose;
    String currentDate = getCurrentTimeIST("yyyy-MM-dd");
    String pipelineJobIdentifier = System.getProperty(JOB_IDENTIFIER);
    String buildName = StringUtils.isNullOrEmpty(pipelineJobIdentifier) ?
      buildNamePrefix + "_" + currentDate :
      buildNamePrefix + "_" + pipelineJobIdentifier + "_" + currentDate;
    if (buildName.length() >= 255)
      // Build name should be less than 255 characters
      buildName = buildName.substring(0, 255);
    return buildName;
  }

  private void handleSpecialCasesForTestNameAndBuildName(Map<String, Object> capabilityMap) {
    String specialCaseIdentifier = "_randomName$";
    Pattern pattern = Pattern.compile(specialCaseIdentifier, Pattern.CASE_INSENSITIVE);
    if (pattern.matcher(capabilityMap.getOrDefault(BUILD_NAME, "").toString()).find())
      capabilityMap.put(BUILD_NAME,
        capabilityMap.get(BUILD_NAME).toString().replace("_randomName", getRandomAlphaNumericString(10)));
    if (pattern.matcher(capabilityMap.getOrDefault(TEST_NAME, "").toString()).find())
      capabilityMap.put(TEST_NAME,
        capabilityMap.get(TEST_NAME).toString().replace("_randomName", getRandomAlphaNumericString(10)));
  }

  private FirefoxProfile getFirefoxProfile(String profileName, Map<String, Object> capabilityMap) {
    FirefoxProfile profile = new FirefoxProfile();
    if (profileName.equals("USER_DEFINED_PROFILE_WITH_CUSTOM_DOWNLOAD_PATH")) {
      profile.setPreference("browser.download.folderList", 2);
      String downloadDirectory = capabilityMap.getOrDefault(PLATFORM_NAME, "").toString().toLowerCase()
        .contains("win") ?
        WINDOWS_USER_DATA_DIRECTORY_PATH + "\\Documents" :
        MAC_USER_DATA_DIRECTORY_PATH + "/Documents";
      profile.setPreference("browser.download.dir", downloadDirectory);
      // Disable download prompts for PDF, JPEG, and TXT files
      profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
        "text/csv,application/java-archive, application/x-msexcel,application/excel,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/x-excel,application/vnd.ms-excel,image/png,image/jpeg,text/html,text/plain,application/msword,application/xml,application/vnd.microsoft.portable-executable");
    }
    return profile;
  }

  private void handleFirefoxOptionsForBrowserCapabilities(Map<String, Object> capabilityMap) {
    if (capabilityMap.get(FIREFOX_OPTIONS) instanceof Map) {
      Map<String, Object> firefoxOptions = (Map<String, Object>) capabilityMap.get(FIREFOX_OPTIONS);
      // Handle Firefox profile if it is defined in the options
      if (firefoxOptions.containsKey(PROFILE_INSIDE_BROWSER_OPTIONS)) {
        final String userDefinedProfileValueIdentifier = "USER_DEFINED_PROFILE_WITH_CUSTOM_DOWNLOAD_PATH";
        ArrayList<String> profileArray = (ArrayList<String>) firefoxOptions.getOrDefault(PROFILE_INSIDE_BROWSER_OPTIONS,
          new ArrayList<>());
        String profileValue = !profileArray.isEmpty() ? profileArray.getFirst() : "";
        if (profileValue.equals(userDefinedProfileValueIdentifier)) {
          FirefoxProfile profile = getFirefoxProfile(profileValue, capabilityMap);
          firefoxOptions.put(PROFILE_INSIDE_BROWSER_OPTIONS, profile);
        }
      }
    }
  }

  private void handleEdgeOptionsForBrowserCapabilities(Map<String, Object> capabilityMap) {
    // Handle Edge options if needed
  }

  private void handleChromeOptionsForBrowserCapabilities(Map<String, Object> capabilityMap) {
    // Handle Chrome options if needed
  }

  private void handleSafariOptionsForBrowserCapabilities(Map<String, Object> capabilityMap) {
    // Handle Safari options if needed
  }

  private void handleSpecialCasesForBrowserOptions(Map<String, Object> capabilityMap) {
    if (capabilityMap.containsKey(FIREFOX_OPTIONS)) {
      handleFirefoxOptionsForBrowserCapabilities(capabilityMap);
    }
    if (capabilityMap.containsKey(EDGE_OPTIONS)) {
      handleEdgeOptionsForBrowserCapabilities(capabilityMap);
    }
    if (capabilityMap.containsKey(CHROME_OPTIONS)) {
      handleChromeOptionsForBrowserCapabilities(capabilityMap);
    }
    if (capabilityMap.containsKey(SAFARI_OPTIONS)) {
      handleSafariOptionsForBrowserCapabilities(capabilityMap);
    }
  }

  private void setCustomValues(@NonNull Map<String, Object> capabilityMap, String purpose) {
    purpose = purpose.toLowerCase().contains("client") ? "Client" : "Test";

    if (capabilityMap.getOrDefault(TUNNEL, "false").toString().equalsIgnoreCase("true") && StringUtils.isNullOrEmpty(
      (String) capabilityMap.getOrDefault(TUNNEL_NAME, "")) && EnvSetup.TEST_TUNNEL_NAME.get() != null)
      capabilityMap.put(TUNNEL_NAME, EnvSetup.TEST_TUNNEL_NAME.get());

    if (StringUtils.isNullOrEmpty((String) capabilityMap.getOrDefault(TEST_NAME, "")))
      capabilityMap.put(TEST_NAME, capsString);

    if (StringUtils.isNullOrEmpty((String) capabilityMap.getOrDefault(BUILD_NAME, "")))
      capabilityMap.put(BUILD_NAME, getTestBuildName(purpose));

    if (capabilityMap.getOrDefault(LOAD_PUBLIC_EXTENSION, "false").equals("true"))
      insertToMapWithRandom(capabilityMap, LOAD_PUBLIC_EXTENSION, DASHLANE_EXTENSION_PUBLIC_URL, String.class,
        String[].class);

    if (capabilityMap.getOrDefault(BROWSER_PROFILE, "").equals("RETRIEVED_S3_URL_PATH"))
      capabilityMap.put(BROWSER_PROFILE,
        TEST_VERIFICATION_DATA.get().getOrDefault(testVerificationDataKeys.BROWSER_PROFILE_S3_URL, ""));

    // Replaces the test name and build name with random values if they contain "_randomName$"
    handleSpecialCasesForTestNameAndBuildName(capabilityMap);

    // Handle special cases for browser options
    handleSpecialCasesForBrowserOptions(capabilityMap);
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
    String jobPurpose = System.getProperty(JOB_PURPOSE, "");
    capabilityMap.entrySet().stream().filter(entry -> entry.getValue().toString().equals(".*")).forEach(entry -> {
      String key = entry.getKey();
      String randomValue = switch (key) {
        case "timezone" -> getRandomTimeZone(capabilityMap.get("platform").toString(), jobPurpose);
        case "geoLocation" -> getRandomGeoLocation(jobPurpose);
        case "resolution" -> getRandomResolution(capabilityMap.get("platform").toString(), jobPurpose);
        case "version" -> getRandomBrowserVersionFromTopSix((String) capabilityMap.getOrDefault("browserName", ""));
        case "selenium_version" -> getRandomSelenium4Version();
        default -> throw new RuntimeException(
          key + " this capability doesn't support random value. Supported values: " + Arrays.asList(
            randomValueSupportedCaps));
      };
      ltLogger.info("Set {} caps value: {}", key, randomValue);
      if (randomValue != null) {
        capabilityMap.put(key, randomValue);
      } else {
        ltLogger.info("Removing {} from caps as random value is not applicable for this job {}", key,
          System.getProperty(JOB_PURPOSE, "NA"));
        capabilityMap.remove(key);
      }
    });
  }

  private String getRandomGeoLocation(String purpose) {
    String dataPath = purpose.equals(jobPurpose.SMOKE.getValue()) ?
      GEOLOCATIONS_FOR_SMOKE_DATA_PATH :
      GEOLOCATION_DATA_PATH;
    try {
      var objectMapper = new ObjectMapper();
      var rootNode = objectMapper.readTree(getFileWithFileLock(dataPath));
      var geoDataArray = rootNode.path("geoData");
      ltLogger.info("GeoLocation dataset: {}", geoDataArray);
      if (geoDataArray.isArray() && !geoDataArray.isEmpty()) {
        Random random = new Random();
        int randomIndex = random.nextInt(geoDataArray.size());
        JsonNode randomGeoObject = geoDataArray.get(randomIndex);
        TEST_VERIFICATION_DATA.get()
          .put(testVerificationDataKeys.GEO_LOCATION, randomGeoObject.path("countryName").asText());
        return randomGeoObject.path("countryCode").asText();
      }
    } catch (IOException e) {
      throw new RuntimeException("Unable to read geolocation data", e);
    }

    return null;
  }

  private String getRandomResolution(String platform, String purpose) {
    String dataPath = purpose.equals(jobPurpose.SMOKE.getValue()) ?
      jobPurpose.SMOKE.getValue() :
      jobPurpose.REGRESSION.getValue();
    try {
      var objectMapper = new ObjectMapper();
      var rootNode = objectMapper.readTree(getFileWithFileLock(RESOLUTION_DATA_PATH));
      JsonNode resDataArray = rootNode.path(platform.toLowerCase()).path(dataPath);
      if (resDataArray.isArray() && !resDataArray.isEmpty()) {
        ltLogger.info("Resolution dataset: {}", resDataArray);
        Random random = new Random();
        int randomIndex = random.nextInt(resDataArray.size());
        String res = resDataArray.get(randomIndex).asText();
        TEST_VERIFICATION_DATA.get().put(testVerificationDataKeys.RESOLUTION, res);
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
    String[] topFive = { "latest", "latest-1", "latest-2", "latest-3", "latest-4" };
    return topFive[new Random().nextInt(topFive.length)];
  }

  private String getRandomTimeZone(String platform, String purpose) {
    String timeZoneIndex = platform.toLowerCase().contains("win") ? "win" : "others";
    String dataPath = purpose.equals(jobPurpose.SMOKE.getValue()) ?
      jobPurpose.SMOKE.getValue() :
      jobPurpose.REGRESSION.getValue();
    ltLogger.info("timezone index: {} and data path: {}", timeZoneIndex, dataPath);
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      var rootNode = objectMapper.readTree(getFileWithFileLock(TIMEZONE_DATA_PATH));
      List<String> timezoneIds = new ArrayList<>();
      rootNode.path(timeZoneIndex).path(dataPath).fields().forEachRemaining(entry -> timezoneIds.add(entry.getKey()));
      ltLogger.info("Timezone dataset: {}", timezoneIds);
      Random random = new Random();
      int randomIndex = random.nextInt(timezoneIds.size());
      return timezoneIds.get(randomIndex);

    } catch (IOException e) {
      throw new RuntimeException("Unable to read timezone data", e);
    }
  }

  private String getRandomSelenium4Version() {
    //    String[] topFiveBasedOnCustomerTests = { "latest", "latest-1", "latest-2", "4.17.0", "4.13.0", "4.8.0", "4.0.0" };
    String[] topFive = { "latest", "latest-1", "latest-2", "latest-3", "latest-4" };
    return topFive[new Random().nextInt(topFive.length)];
  }

  private void buildCapabilities(String capabilityString, cloudPlatforms cloudPlatform, String purpose,
    String... capsType) {
    String expectedCapsType = capsType.length > 0 ? capsType[0] : "desiredCapabilities";
    Map<String, Object> capabilityMap = purpose.equals("client") ?
      buildCapabilityMap(cloudPlatform, capabilityString, CUSTOM_CLIENT_CAPS, REMOVE_CLIENT_TEST_CAPS) :
      buildCapabilityMap(cloudPlatform, capabilityString, CUSTOM_TEST_CAPS, REMOVE_TEST_CAPS);
    setFinalCapabilities(capabilityMap, expectedCapsType, purpose, cloudPlatform);
  }

  private Map<String, Object> buildCapabilityMap(cloudPlatforms cloudPlatform, String capabilityString,
    String customCapsSource, String... removeCapsSource) {
    capsString = capabilityString;
    // Get hashmap from caps string and build caps hashmap
    Map<String, Object> capabilityMap = new ConcurrentHashMap<>(getExtendedHashMapFromString(capsString));

    // Remove specific caps based on env value
    if (removeCapsSource != null && removeCapsSource.length > 0) {
      removeSpecificCaps(capabilityMap, removeCapsSource[0]);
    }

    // Set default custom values to caps map
    if (cloudPlatform == cloudPlatforms.LAMBDATEST) {
      setCustomValues(capabilityMap, customCapsSource);
    }

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
      capabilityMap.putAll(getExtendedHashMapFromString(customCaps));
    });
  }

  private void updatePlatform(Map<String, Object> capabilityMap) {
    String platform = (String) capabilityMap.getOrDefault("platform", "");
    if (StringUtils.isNullOrEmpty(platform))
      return;
    capabilityMap.put("platform", osKeywordToTemplateNameMap.getOrDefault(platform, platform));
    ltLogger.info("Updating platform: {}", platform);
  }

  private void setFinalCapabilities(Map<String, Object> capabilityMap, String expectedCapsType, String testEnv,
    cloudPlatforms cloudPlatform) {

    ThreadLocal<Map<String, Object>> capsMap = (testEnv.equals("client")) ? CLIENT_TEST_CAPS_MAP : TEST_CAPS_MAP;
    ThreadLocal<Boolean> isExtensionTest = (testEnv.equals("client")) ? IS_EXTENSION_CLIENT_TEST : IS_EXTENSION_TEST;
    ThreadLocal<MutableCapabilities> finalCaps = (testEnv.equals("client")) ? CLIENT_TEST_CAPS : TEST_CAPS;
    capsMap.set(capabilityMap);
    ltLogger.info("Final {} caps: {}", testEnv, capsMap.get());

    /// Check for extensions in capability map to handle new tab opening in the start of the session
    boolean hasExtensions = capabilityMap.containsKey(LOAD_PUBLIC_EXTENSION) || capabilityMap.containsKey(
      LOAD_PRIVATE_EXTENSION);
    isExtensionTest.set(hasExtensions);
    ltLogger.info("Is {} contains extensions: {}", testEnv, isExtensionTest.get());

    CapsType capsType = (TEST_ENV.equals("local") || expectedCapsType.equals("firstMatch")) ?
      CapsType.FIRST_MATCH :
      CapsType.DESIRED_CAPABILITIES;

    /// Handle different capability names for first match and put rest of the capabilities in Remote Platform specific options example: lt:options, bstack:options
    createTestCaps(new HashMap<>(capabilityMap), capsType, cloudPlatformsToCapabilityRootsMap.get(cloudPlatform));
    finalCaps.set(capabilities);
    ltLogger.info("{} caps set in LocalThread: {}", testEnv, finalCaps.get());
  }

  private enum CapsType {
    DESIRED_CAPABILITIES, FIRST_MATCH
  }

  public void buildTestCapability(String capabilityString, String... capsType) {
    buildCapabilities(capabilityString, cloudPlatforms.LAMBDATEST, "test", capsType);
  }

  public void buildClientTestCapability(String capabilityString, String... capsType) {
    buildCapabilities(capabilityString, cloudPlatforms.LAMBDATEST, "client", capsType);
  }

  public void buildTestCapabilityForBS(String capabilityString) {
    buildCapabilities(capabilityString, cloudPlatforms.BROWSERSTACK, "test");
  }

  public void buildTestCapabilityForSL(String capabilityString) {
    buildCapabilities(capabilityString, cloudPlatforms.SAUCELAB, "test", "firstMatch");
  }
}
