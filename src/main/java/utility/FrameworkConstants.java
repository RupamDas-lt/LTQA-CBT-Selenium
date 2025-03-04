package utility;

import factory.Locator;
import factory.LocatorTypes;

import java.util.*;
import java.util.stream.Collectors;

public class FrameworkConstants extends BaseClass {
  public static final String LT_OPTIONS = getRandomLowerUpperCaseOfSpecificString("LT:OPTIONS");

  public static final String HTTPS = "https://";
  public static final String HTTP = "http://";
  public static final String OS_NAME = "os.name";
  public static final String LOCAL_HOST_URL = "http://127.0.0.1:";
  public static final String DASHLANE_EXTENSION_PUBLIC_URL = "https://stage-lambda-devops-use-only.lambdatestinternal.com/magicleap/qa/dashlane-extension.zip";

  // API Constants
  public static final String TUNNEL_INFO_API_PATH = "/api/v1.0/info";
  public static final String SUMO_LOGIC_URL = "https://endpoint4.collection.sumologic.com/receiver/v1/http/ZaVnC4dhaV1BGey4GE8Y98SqULi08X1lrc4PXMlSghL_8tntkfgr38QuaSrgsvF44nqzlbKs38AVE7I0ras7--0sxak3LfUAkMk2UbSOrcjcOwQRBRMpjQ==";
  public static final String GET = "GET";
  public static final String GET_REDIRECT = "GET_REDIRECT";
  public static final String POST = "POST";
  public static final String PUT = "PUT";
  public static final String PATCH = "PATCH";
  public static final String DELETE = "DELETE";
  public static final String GET_WITHOUT_STATUS_CODE_VERIFICATION = "GET_WITHOUT_STATUS_CODE_VERIFICATION";
  public static final String POST_WITHOUT_STATUS_CODE_VERIFICATION = "POST_WITHOUT_STATUS_CODE_VERIFICATION";
  public static final String PUT_WITHOUT_STATUS_CODE_VERIFICATION = "PUT_WITHOUT_STATUS_CODE_VERIFICATION";
  public static final String PATCH_WITHOUT_STATUS_CODE_VERIFICATION = "PATCH_WITHOUT_STATUS_CODE_VERIFICATION";
  public static final String DELETE_WITHOUT_STATUS_CODE_VERIFICATION = "DELETE_WITHOUT_STATUS_CODE_VERIFICATION";

  // ENV variables
  public static final String CUSTOM_USER_NAME = "CUSTOM_USER_NAME";
  public static final String CUSTOM_USER_KEY = "CUSTOM_USER_KEY";
  public static final String CUSTOM_USER_EMAIL = "CUSTOM_USER_EMAIL";
  public static final String CUSTOM_USER_PASS = "CUSTOM_USER_PASS";
  public static final String CUSTOM_GRID_URL = "CUSTOM_GRID_URL";
  public static final String CUSTOM_TUNNEL_FLAGS = "CUSTOM_TUNNEL_FLAGS";
  public static final String TEST_PREREQUISITES = "TEST_PREREQUISITES";
  public static final String CUSTOM_TEST_CAPS = "CUSTOM_TEST_CAPS";
  public static final String CUSTOM_CLIENT_CAPS = "CUSTOM_CLIENT_CAPS";

  // Test Meta data
  public static final String TEST_SETUP_TIME = "test_setup_time";
  public static final String TEST_EXECUTION_TIME = "test_execution_time";
  public static final String TEST_STOP_TIME = "test_stop_time";
  public static final String SESSION_ID = "test_session_id";
  public static final String SESSION_ID_CLIENT = "client_session_id";

  // Test Status
  public static final String COMPLETED = "completed";
  public static final String IGNORED = "ignored";
  public static final String FAILED = "failed";
  public static final String PASSED = "passed";
  public static final String SKIPPED = "skipped";

  // Test caps
  public static final String LOAD_PUBLIC_EXTENSION = "loadExtension";
  public static final String LOAD_PRIVATE_EXTENSION = "lambda:loadExtension";
  public static final String TUNNEL = "tunnel";
  public static final String TUNNEL_NAME = "tunnelName";
  public static final String TEST_NAME = "name";

  // Lambda hooks [Ref: https://www.lambdatest.com/support/docs/lambda-hooks/]
  public static final String LAMBDA_STATUS = "lambda-status";
  public static final String LAMBDA_FILE_EXIST = "lambda-file-exists";
  public static final String LAMBDA_FILE_STATS = "lambda-file-stats";
  public static final String LAMBDA_FILE_CONTENT = "lambda-file-content";
  public static final String LAMBDA_FILE_LIST = "lambda-file-list";
  public static final String LAMBDA_NAME = "lambda-name";
  public static final String LAMBDA_BUILD = "lambda-build";
  public static final String LAMBDA_ACTION = "lambda-action";
  public static final String LAMBDA_PERFORM_KEYBOARD_EVENTS = "lambda-perform-keyboard-events";
  public static final String LAMBDA_BREAK_POINT = "lambda-breakpoint";
  public static final String LAMBDA_SCREENSHOT = "lambda-screenshot";
  public static final String LAMBDA_FILES_DELETE = "lambda-files-delete";
  public static final String LAMBDA_THROTTLE_NETWORK = "lambda-throttle-network";
  public static final String LAMBDA_PING = "lambda-ping";
  public static final String LAMBDA_EXCEPTIONS = "lambda-exceptions";
  public static final String LAMBDA_GET_CLIPBOARD = "lambda-get-clipboard";
  public static final String LAMBDA_SET_CLIPBOARD = "lambda-set-clipboard";
  public static final String LAMBDA_CLEAR_CLIPBOARD = "lambda-clear-clipboard";
  public static final String LAMBDA_UNBOUND_PING = "lambda-unbound-ping";
  public static final String LAMBDA_NETWORK = "lambda:network";
  public static final String LAMBDA_UPDATE_NAME = "lambdaUpdateName";
  public static final String LAMBDA_TEST_TAGS = "lambda-test-tags";
  public static final String LAMBDA_TEST_CASE_START = "lambda-testCase-start";
  public static final String LAMBDA_TEST_CASE_END = "lambda-testCase-end";

  // File paths
  public static final String GEOLOCATION_DATA_PATH = "src/test/resources/TestData/geoLocations.json";
  public static final String RESOLUTION_DATA_PATH = "src/test/resources/TestData/resolutions.json";
  public static final String BROWSER_VERSIONS_DATA_PATH = "src/test/resources/TestData/browser_versions/<BROWSER_NAME>_<TEMPLATE>.json";
  public static final String SAMPLE_TXT_FILE_PATH = "src/test/resources/TestFiles/LambdaTest.txt";

  // Test execution data
  public static final Set<String> validSelfSignedValues = new HashSet<>() {{
    add("self-signed.\nbadssl.com");
    add("untrusted-root.\nbadssl.com");
    add("self-signed.badssl.com");
    add("SSL.com - Test Website");
  }};
  public static final Set<String> consoleLogs = new HashSet<>() {{
    add("console log is working fine via log command");
    add("console log is working fine via error command");
    add("console log is working fine via warn command");
    add("console log is working fine via info command");
  }};
  public static final List<Locator> locatorsForExceptionLogs = new ArrayList<>() {{
    add(new Locator(LocatorTypes.CSS, "#invalid_css_selector"));
    add(new Locator(LocatorTypes.XPATH, "//div[class='invalid_XPATH_selector']"));
  }};
  public static final Map<String, String> osKeywordToTemplateNameMap = Map.ofEntries(Map.entry("win10", "Windows 10"),
    Map.entry("win11", "Windows 11"), Map.entry("win8.1", "Windows 8.1"), Map.entry("win8", "Windows 8"),
    Map.entry("win7", "Windows 7"), Map.entry("sequoia", "MacOS Sequoia"), Map.entry("sonoma", "MacOS Sonoma"),
    Map.entry("ventura", "MacOS Ventura"), Map.entry("monterey", "MacOS Monterey"),
    Map.entry("bigsur", "MacOS Big Sur"), Map.entry("catalina", "MacOS Catalina"), Map.entry("mojave", "macOS Mojave"),
    Map.entry("sierra", "macOS Sierra"), Map.entry("high_sierra", "macOS High Sierra"),
    Map.entry("elcapitan", "OS X El Capitan"), Map.entry("yosemite", "OS X El Yosemite"),
    Map.entry("mavericks", "OS X El Mavericks"), Map.entry("ubuntu", "ubuntu 20"));
  public static final Map<String, String> osTemplateNameToKeywordMap = osKeywordToTemplateNameMap.entrySet().stream()
    .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

  // JavaScripts
  public static final String jsForFetchBrowserDetails = "const browserData = navigator.userAgentData || {}; " + "const userAgent = navigator.userAgent.toLowerCase(); " + "let browserName = ''; " + "let browserVersion = ''; " + "if (userAgent.includes('firefox')) { " + "  browserName = 'firefox'; " + "} else if (userAgent.includes('edg')) { " + "  browserName = 'edge'; " + "} else if (userAgent.includes('chrome') && !userAgent.includes('chromium')) { " + "  browserName = 'chrome'; " + "} else if (userAgent.includes('safari')) { " + "  browserName = 'safari'; " + "} else if (userAgent.includes('opera') || userAgent.includes('opr')) { " + "  browserName = 'opera'; " + "} else if (userAgent.includes('chromium')) { " + "  browserName = 'chromium'; " + "} else { " + "  browserName = browserData.brands?.find(b => b.brand)?.brand || navigator.appName; " + "} " + "if (browserData.brands) { " + "  browserVersion = browserData.brands.find(b => b.brand)?.version || ''; " + "} else { " + "  const versionMatch = userAgent.match(/(firefox|edg|chrome|safari|opera|opr|chromium)[\\/ ]([\\d.]+)/i); " + "  browserVersion = versionMatch ? versionMatch[2] : navigator.appVersion; " + "} " + "return { name: browserName.toLowerCase(), version: browserVersion.trim() };";

}
