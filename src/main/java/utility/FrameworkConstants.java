package utility;

public class FrameworkConstants extends BaseClass {
  public static final String LT_OPTIONS = getRandomLowerUpperCaseOfSpecificString("LT:OPTIONS");

  public static final String HTTPS = "https://";
  public static final String HTTP = "http://";
  public static final String OS_NAME = "os.name";
  public static final String LOCAL_HOST_URL = "http://127.0.0.1:";

  // API Constants
  public static final String TUNNEL_INFO_API_PATH = "/api/v1.0/info";
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

  // Test Meta data
  public static final String TEST_SETUP_TIME = "test_setup_time";
  public static final String TEST_EXECUTION_TIME = "test_execution_time";
  public static final String TEST_STOP_TIME = "test_stop_time";
  public static final String SESSION_ID = "test_session_id";

  // Test Status
  public static final String COMPLETED = "completed";
  public static final String IGNORED = "ignored";
  public static final String FAILED = "failed";
  public static final String PASSED = "passed";
  public static final String SKIPPED = "skipped";

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
}
