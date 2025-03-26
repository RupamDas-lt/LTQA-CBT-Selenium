package utility;

import factory.Locator;
import factory.LocatorTypes;
import lombok.Getter;

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

  public static HashMap<String, String> sessionApiEndpoints() {
    HashMap<String, String> sessionApiUriPart2 = new HashMap<>();
    sessionApiUriPart2.put("console", "/log/console");
    sessionApiUriPart2.put("network", "/log/network");
    sessionApiUriPart2.put("selenium", "/log/selenium");
    sessionApiUriPart2.put("webDriver", "/log/selenium");
    sessionApiUriPart2.put("command", "/log/command");
    sessionApiUriPart2.put("exception", "/log/command?isExceptionLog=true");
    sessionApiUriPart2.put("terminal", "/terminal-logs");
    sessionApiUriPart2.put("video", "/video");
    sessionApiUriPart2.put("screenshots", "/screenshots");
    sessionApiUriPart2.put("session_details", "");
    sessionApiUriPart2.put("stop", "/stop");
    sessionApiUriPart2.put("networkHar", "/log/network.har");
    sessionApiUriPart2.put("fullHar", "/log/full-har");
    return sessionApiUriPart2;
  }

  public static final String TEST_API_ENDPOINT = "/api/v1/test/";
  public static final String SESSIONS_API_ENDPOINT = "/automation/api/v1/sessions/";
  public static final String BUILDS_API_ENDPOINT = "/automation/api/v1/builds/";
  public static final String TUNNELS_API_ENDPOINT = "/automation/api/v1/tunnels";
  public static final String SESSIONS_API_V2_ENDPOINT = "/automation/api/v2/sessions/";
  public static final String GEOLOCATIONS_API_ENDPOINT = "/api/v1/geolocation?unique=true";
  public static final String BROWSER_VERSIONS_API_ENDPOINT = "/api/v2/capability?grid=selenium&browser=<BROWSER_NAME>&os=<TEMPLATE>";
  public static final String SELENIUM_VERSIONS_API_ENDPOINT = "/api/v2/capability?grid=selenium&browser=<BROWSER_NAME>&version=<BROWSER_VERSION>&os=<TEMPLATE>&browser_version_id=<BROWSER_VERSION_ID>";
  public static final String BUILD_STOP_API_ENDPOINT = "/api/v1/test/stop/?buildId=";
  public static final String SESSION_LIGHTHOUSE_REPORT_ENDPOINT = "/automation/api/v1/lighthouse/report/";

  public static final String REQUEST_BODY_CONTENT_TYPE_MULTIPART_FORM = "multipart/form-data";
  public static final String REQUEST_BODY_CONTENT_TYPE_BINARY = "application/octet-stream";
  public static final String REQUEST_BODY_CONTENT_TYPE_URLENC = "application/x-www-form-urlencoded";
  public static final String REQUEST_BODY_CONTENT_TYPE_HTML = "text/html";
  public static final String REQUEST_BODY_CONTENT_TYPE_XML = "application/xml";
  public static final String REQUEST_BODY_CONTENT_TYPE_JSON = "application/json";
  public static final String REQUEST_BODY_CONTENT_TYPE_TEXT = "text/plain";

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
  public static final String REMOVE_TEST_CAPS = "REMOVE_TEST_CAPS";
  public static final String REMOVE_CLIENT_TEST_CAPS = "REMOVE_CLIENT_TEST_CAPS";

  // Test Meta data
  public static final String TEST_SETUP_TIME = "test_setup_time";
  public static final String TEST_EXECUTION_TIME = "test_execution_time";
  public static final String TEST_STOP_TIME = "test_stop_time";
  public static final String SESSION_ID = "test_session_id";
  public static final String SESSION_ID_CLIENT = "client_session_id";
  public static final String TEST_START_TIMESTAMP = "test_start_timestamp";
  public static final String TEST_END_TIMESTAMP = "test_end_timestamp";

  // Test Status
  public static final String COMPLETED = "completed";
  public static final String RUNNING = "running";
  public static final String STOPPED = "stopped";
  public static final String IDLE_TIMEOUT_STATUS = "idle_timeout";
  public static final String IGNORED = "ignored";
  public static final String FAILED = "failed";
  public static final String PASSED = "passed";
  public static final String SKIPPED = "skipped";

  // Test caps
  public static final String BROWSER_NAME = "browserName";
  public static final String BROWSER_VERSION = "version";
  public static final String PLATFORM_NAME = "platform";
  public static final String LOAD_PUBLIC_EXTENSION = "loadExtension";
  public static final String LOAD_PRIVATE_EXTENSION = "lambda:loadExtension";
  public static final String TUNNEL = "tunnel";
  public static final String TUNNEL_NAME = "tunnelName";
  public static final String TEST_NAME = "name";
  public static final String SELENIUM_VERSION = "selenium_version";
  public static final String GEO_LOCATION = "geoLocation";
  public static final String TIMEZONE = "timezone";
  public static final String NETWORK = "network";
  public static final String RESOLUTION = "resolution";
  public static final String NETWORK_HTTP_2 = "network.http2";
  public static final String NETWORK_HAR = "network.har";
  public static final String NETWORK_FULL_HAR = "network.full.har";
  public static final String TERMINAL = "terminal";
  public static final String SELENIUM_CDP = "scCdp";
  public static final String VIDEO = "video";
  public static final String VISUAL = "visual";
  public static final String CONSOLE = "console";
  public static final String PERFORMANCE = "performance";
  public static final String WEBDRIVER_MODE = "webdriverMode";
  public static final String SELENIUM_TELEMETRY_LOGS = "seTelemetryLogs";
  public static final String VERBOSE_WEBDRIVER_LOGGING = "verboseWebDriverLogging";
  public static final String IDLE_TIMEOUT = "idleTimeout";

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
  public static final String TIMEZONE_DATA_PATH = "src/test/resources/TestData/timeZones.json";
  public static final String BROWSER_VERSIONS_DATA_PATH = "src/test/resources/TestData/browser_versions/<BROWSER_NAME>_<TEMPLATE>.json";
  public static final String SAMPLE_TXT_FILE_PATH = "src/test/resources/TestFiles/LambdaTest.txt";
  public static final String SAMPLE_TERMINAL_LOGS_FILE_PATH = "src/test/resources/TestFiles/sample_terminal_logs.txt";
  public static final String TEST_LOGS_DOWNLOAD_DIRECTORY = "logs/testLogsFromSwaggerV2/";
  public static final String COMMAND_LOGS_API_V1_SCHEMA = "src/test/resources/TestData/jsonSchemas/commandLogsAPIV1.json";
  public static final String COMMAND_LOGS_API_V2_SCHEMA = "src/test/resources/TestData/jsonSchemas/commandLogsAPIV2.json";

  // ffprobe commands to extract video data
  public static final String[] DURATION_COMMAND = { "ffprobe", "-v", "error", "-show_entries", "format=duration", "-of",
    "default=noprint_wrappers=1:nokey=1" };

  public static final String[] RESOLUTION_COMMAND = { "ffprobe", "-v", "error", "-show_entries", "stream=width,height",
    "-of", "csv=p=0:s=x" };

  public static final String[] CODEC_COMMAND = { "ffprobe", "-v", "error", "-show_entries", "stream=codec_name", "-of",
    "default=noprint_wrappers=1:nokey=1" };

  public static final String[] FRAME_RATE_COMMAND = { "ffprobe", "-v", "error", "-show_entries",
    "stream=avg_frame_rate", "-of", "default=noprint_wrappers=1:nokey=1" };

  public static final String[] BITRATE_COMMAND = { "ffprobe", "-v", "error", "-show_entries", "format=bit_rate", "-of",
    "default=noprint_wrappers=1:nokey=1" };

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

  public static final Map<String, Set<String>> testActionsToCapsMap = Map.of("local", Set.of(TUNNEL, NETWORK),
    "geolocation", Set.of(GEO_LOCATION), "timezone", Set.of(TIMEZONE));

  public static final Map<String, Map<String, Object>> testArtefactsToCapsMap = new HashMap<>() {{
    put("network", Map.of(NETWORK, "true"));
    put("full.har", Map.of(NETWORK_FULL_HAR, "true", NETWORK, "true"));
    put("terminal", Map.of(TERMINAL, "true"));
    put("screenshot", Map.of(VISUAL, "true"));
    put("console", Map.of(CONSOLE, "true", BROWSER_NAME, List.of("chrome", "edge")));
    put("performance report", Map.of(PERFORMANCE, "true", BROWSER_NAME, List.of("chrome")));
  }};

  public static final String IST_TimeZone = "Asia/Kolkata";
  public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss z";

  public enum testVerificationDataKeys {
    URL, LOCATORS, JAVA_SCRIPTS, BROWSER_VERSION, ACTUAL_BROWSER_VERSION, BROWSER_VERSION_ID, GEO_LOCATION, RESOLUTION, CONSOLE_LOG, TERMINAL_LOG, EXCEPTION_LOG
  }

  @Getter public enum videoMetadataTypes {
    WIDTH("width"), HEIGHT("height"), FRAMERATE("frameRate"), DURATION_IN_SECONDS("durationInSeconds"), RESOLUTION(
      "resolution"), CODEC("codec"), BITRATE("bitrate"),
    ;

    private final String value;

    videoMetadataTypes(String value) {
      this.value = value;
    }
  }

  // JavaScripts
  public static final String jsForFetchBrowserDetails = "const browserData = navigator.userAgentData || {}; " + "const userAgent = navigator.userAgent.toLowerCase(); " + "let browserName = ''; " + "let browserVersion = ''; " + "if (userAgent.includes('firefox')) { " + "  browserName = 'firefox'; " + "} else if (userAgent.includes('edg')) { " + "  browserName = 'edge'; " + "} else if (userAgent.includes('chrome') && !userAgent.includes('chromium')) { " + "  browserName = 'chrome'; " + "} else if (userAgent.includes('safari')) { " + "  browserName = 'safari'; " + "} else if (userAgent.includes('opera') || userAgent.includes('opr')) { " + "  browserName = 'opera'; " + "} else if (userAgent.includes('chromium')) { " + "  browserName = 'chromium'; " + "} else { " + "  browserName = browserData.brands?.find(b => b.brand)?.brand || navigator.appName; " + "} " + "if (browserData.brands) { " + "  browserVersion = browserData.brands.find(b => b.brand)?.version || ''; " + "} else { " + "  const versionMatch = userAgent.match(/(firefox|edg|chrome|safari|opera|opr|chromium)[\\/ ]([\\d.]+)/i); " + "  browserVersion = versionMatch ? versionMatch[2] : navigator.appVersion; " + "} " + "return { name: browserName.toLowerCase(), version: browserVersion.trim() };";

}
