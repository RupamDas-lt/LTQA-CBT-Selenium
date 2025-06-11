package factory;

import lombok.Getter;

@Getter public enum SoftAssertionMessages {
  BASIC_AUTH_FAILED_MESSAGE("Basic Authentication Failed"), BASIC_AUTH_USING_KEYBOARD_EVENT_FAILED_MESSAGE(
    "Basic Authentication Failed using keyboard events."), SELF_SIGNED_CERT_ERROR_MESSAGE(
    "Self-signed site not open. There might be a certificate issue or website didn't open."), TIMEZONE_FAILURE_MESSAGE(
    "Timezone is not set correctly. Current timezone is: %s expected: %s"), LOGIN_USING_KEYBOARD_EVENT_FAILURE_MESSAGE(
    "Heroku app web page keyboard log in failed, Expected: You logged into a secure area but Received: %s"), UNABLE_TO_OPEN_HEROKU_APP_LOGIN_PAGE_MESSAGE(
    "Unable to open Heroku Login Page"), BROWSER_NAME_ERROR_MESSAGE(
    "Browser name doesn't match. Expected: %s, Actual: %s"), BROWSER_VERSION_ERROR_MESSAGE(
    "Browser version doesn't match. Expected: %s, Actual: %s"), EXTENSION_NOT_WORKING_ERROR_MESSAGE(
    "Extension not working, or might be tab not switched"), BASE_TEST_ERROR_MESSAGE(
    "Entered text doesn't match. Expected: %s Actual: %s"), UPLOAD_FILE_ERROR_MESSAGE(
    "Unable to upload file"), GEOLOCATION_ERROR_MESSAGE(
    "GeoLocation didn't match. Expected: %s Actual: %s"), LOGIN_CACHE_CHECK_FAILED_ERROR_MESSAGE(
    "LT login page is not displayed, cache is not cleared in the machine."), LOGIN_CACHE_NOT_SET_FAILED_ERROR_MESSAGE(
    "Failed to login to LT website, cache verification will not be valid for next sessions."), LOCAL_URL_CHECK_FAILED_WITH_TUNNEL_ERROR_MESSAGE(
    "Local Url Not Displayed"), AB_IHA_TEST_FAILED_ERROR_MESSAGE(
    "A/B Test Variation failed."), ADD_ELEMENT_IHA_TEST_FAILED_ERROR_MESSAGE(
    "Add element test verification failed"), BROKEN_IMAGES_IHA_TEST_FAILED_ERROR_MESSAGE(
    "Broken Images are not displayed"), CHECKBOXES_IHA_TEST_FAILED_ERROR_MESSAGE(
    "Checkboxes are not selected"), DYNAMIC_CONTENT_IHA_TEST_FAILED_ERROR_MESSAGE(
    "Dynamic Content test failed as static messages are not same after page refresh"), DYNAMIC_CONTROLS_IHA_TEST_FAILED_ERROR_MESSAGE_1(
    "First swap failed."), DYNAMIC_CONTROLS_IHA_TEST_FAILED_ERROR_MESSAGE_2(
    "Second swap failed."), DYNAMIC_CONTROLS_IHA_TEST_FAILED_ERROR_MESSAGE_3(
    "Text box enable failed."), DYNAMIC_CONTROLS_IHA_TEST_FAILED_ERROR_MESSAGE_4(
    "Text box disable failed."), FORM_LOGIN_IHA_TEST_FAILED_ERROR_MESSAGE(
    "Form login is not successful"), FORM_LOGOUT_IHA_TEST_FAILED_ERROR_MESSAGE(
    "Form logout failed."), UPLOAD_SAMPLE_TERMINAL_LOGS_FAILED_ERROR_MESSAGE(
    "Failed to upload terminal logs"), STOP_TEST_SESSION_VIA_API_ERROR_MESSAGE(
    "Unable to stop session with test stop api. Status: %s , Message: %s"), BUILD_STATUS_MISMATCH_ERROR_MESSAGE(
    "Build status doesn't match. Expected: %s , Actual: %s"), TUNNEL_NOT_IN_RUNNING_STATE_ERROR_MESSAGE(
    "There is no running tunnel with name: %s"), TUNNEL_NOT_STOPPED_ERROR_MESSAGE(
    "The tunnel with name: %s is in running state. But expected state is: %s"), STOP_RUNNING_TUNNEL_FAILED_ERROR_MESSAGE(
    "Stop tunnel failed with tunnel id: %s, Status: %s"), PUBLIC_WEBSITES_NOT_RESOLVED_IN_EXPECTED_PLACE_ERROR_MESSAGE(
    "Public websites are not resolved in expected location for tunnel flag %s. Expected: %s, Actual: %s, Fetched IP: %s, Fetched location: %s"), ALLOW_HOSTS_TUNNEL_FLAG_NOT_WORKING_ERROR_MESSAGE_1(
    "allowHosts flag is not working. %s should be resolved in tunnel client as allowHosts='*lambda*' flag is used, but unable to open it."), ALLOW_HOSTS_TUNNEL_FLAG_NOT_WORKING_ERROR_MESSAGE_2(
    "allowHosts flag is not working. %s should be resolved in the DC or Tunnel Server not in Tunnel Client"), BYPASS_HOSTS_TUNNEL_FLAG_NOT_WORKING_ERROR_MESSAGE_1(
    "bypassHosts flag is not working. %s should be resolved in tunnel server or DC not in Tunnel Client as bypassHosts='*lambda*' flag is used, and it shouldn't be opened."), BYPASS_HOSTS_TUNNEL_FLAG_NOT_WORKING_ERROR_MESSAGE_2(
    "bypassHosts flag is not working. %s should be resolved in tunnel client as bypassHosts='*lambda*' flag is used, but unable to open it."), LOCAL_URL_NOT_WORKING_WITH_TUNNEL_ERROR_MESSAGE(
    "Unable to open %s with tunnel. Tunnel is not working as expected. Private websites should be resolved in Tunnel Client"),

  // TEST Artefacts api verification error messages
  UNABLE_TO_GET_LOGS_DOWNLOAD_URL_FROM_API_V2_ERROR_MESSAGE(
    "Unable to retrieve artefacts API download URL from API V2. Message: %s"), UNABLE_TO_VERIFY_PORT_NUMBER_FROM_SYSTEM_LOGS_ERROR_MESSAGE(
    "Unable to verify port number. Received API response: %s"), WEBDRIVER_MODE_PORT_MISMATCH_ERROR_IN_SYSTEM_LOGS_ERROR_MESSAGE(
    "With webdriver mode, expected port number is %s. But used port is %s"), SELENIUM_MODE_PORT_MISMATCH_ERROR_IN_SYSTEM_LOGS_ERROR_MESSAGE(
    "Expected port number is %s for Selenium Driver. But used port is %s"), EXPECTED_PORT_NUMBER_NOT_FOUND_IN_SYSTEM_LOGS_ERROR_MESSAGE(
    "Expected port number is not present in the Selenium logs. Expected port number is: %s"), RECEIVED_LOGS_FROM_API_IS_NULL_OR_EMPTY_ERROR_MESSAGE(
    "Unable to fetch %s logs from API. Received API response: %s"), EXPECTED_DATA_IS_NOT_PRESENT_IN_LOGS_API_RESPONSE_ERROR_MESSAGE(
    "%s is not present in the %s logs."), SELENIUM_VERSION_MISMATCH_ERROR_MESSAGE(
    "Expected Selenium version is %s but found %s"), SELENIUM_VERSION_MISMATCH_FALLBACK_ERROR_MESSAGE(
    "Selenium logs does not contain %s"), LEGACY_SELENIUM_4_LOGS_ERROR_MESSAGE_1(
    "Info level logs are missing for Selenium version %s"), LEGACY_SELENIUM_4_LOGS_ERROR_MESSAGE_2(
    "Debug level logs are missing for Selenium version %s"), LEGACY_SELENIUM_4_LOGS_ERROR_MESSAGE_3(
    "Selenium %s logs are incomplete."), NEW_SELENIUM_4_LOGS_ERROR_MESSAGE_1(
    "Expected log 'Started Selenium Standalone' missing for Selenium %s"), NEW_SELENIUM_4_LOGS_ERROR_MESSAGE_2(
    "Session creation log with correct session ID missing. Expected logs: %s"), NEW_SELENIUM_4_LOGS_ERROR_MESSAGE_3(
    "Distributor session creation log with correct session ID missing. Expected logs: %s"), NEW_SELENIUM_4_LOGS_ERROR_MESSAGE_4(
    "Expected session log missing or incorrect session ID: %s"), CHECK_EXPECTED_URLS_PRESENT_ERROR_MESSAGE_1(
    "Number of urls present in the %s logs are not same. Expected: %s, Actual: %s"), CHECK_EXPECTED_URLS_PRESENT_ERROR_MESSAGE_2(
    "Mismatch found in %s. Expected URL: %s but got URL: %s"), RECEIVED_NULL_COMMAND_LOGS_IN_API_RESPONSE_ERROR_MESSAGE(
    "Unable to fetch %s logs from API %s. Received API response: %s"), SCHEMA_VALIDATION_FAILURE_FOR_LOGS_API_RESPONSE_ERROR_MESSAGE(
    "Schema validation failed for %s logs from %s. Errors: %s"), LOGS_COUNT_MISMATCH_ERROR_MESSAGE(
    "%s logs count fetched from %s doesn't match. Expected: %s, Actual: %s"), EXPECTED_CONSOLE_LOGS_DATA_NOT_AVAILABLE_ERROR_MESSAGE(
    "Expected logs to verify console logs are missing."), EXPECTED_CONSOLE_LOGS_ARE_NOT_AVAILABLE_ERROR_MESSAGE(
    "Expected log: %s is missing from the console logs fetched from API version: %s"), TERMINAL_LOGS_NOT_UPLOADED_ERROR_MESSAGE(
    "Expected terminal logs data is empty, please upload terminal logs before verifying terminal logs"), TERMINAL_LOGS_DATA_MISMATCH_ERROR_MESSAGE(
    "Terminal logs data doesn't match for the logs data fetched from API version: %s"), UNABLE_TO_EXTRACT_VIDEO_URLS_ERROR_MESSAGE(
    "Unable to extract video urls from API response. Status: %s Message: %s"), VIDEO_NOT_GENERATED_ERROR_MESSAGE(
    "Extracted video metadata is null. Possible cause: Downloaded video file is corrupted."), VIDEO_RESOLUTION_MISMATCH_ERROR_MESSAGE_1(
    "Actual video width is not greater than expected width. Expected: %s, Actual: %s"), VIDEO_RESOLUTION_MISMATCH_ERROR_MESSAGE_2(
    "Actual video height is not greater than expected height. Expected: %s, Actual: %s"), VIDEO_DURATION_MISMATCH_ERROR_MESSAGE(
    "Test video duration is greater than the expected video duration [1min + test execution time]. Expected duration: %s, Actual: %s"), UNABLE_TO_DOWNLOAD_VIDEO_ERROR_MESSAGE(
    "Unable to download video file for the session with name: %s"), VIDEO_SHARE_URL_NOT_VALID_ERROR_MESSAGE(
    "Video share url is not valid. Url: %s"), UNABLE_TO_FETCH_LIGHTHOUSE_REPORT_ERROR_MESSAGE(
    "Unable to fetch lighthouse reports. Status: %s, Message: %s"), UNABLE_TO_DOWNLOAD_JSON_LIGHTHOUSE_REPORT_ERROR_MESSAGE(
    "Unable to download Lighthouse report (JSON). Status: %s"), UNABLE_TO_DOWNLOAD_HTML_LIGHTHOUSE_REPORT_ERROR_MESSAGE(
    "Unable to download Lighthouse report (HTML). Status: %s"), AUTO_HEAL_NOT_WORKING_FOR_LOCATORS_ERROR_MESSAGE(
    "Auto-heal is not working for %s locator"), AUTO_HEALED_LOCATOR_IS_LOCATING_SOME_OTHER_ELEMENT_THAN_EXPECTED_ERROR_MESSAGE(
    "Auto-healed locator for %s is locating some other element than expected. Expected: %s, Actual: %s"), AUTO_HEAL_BASELINE_CAPTURE_FAILED_ERROR_MESSAGE(
    "Base line capture failed for auto-heal test, so this action will be skipped"), NULL_OR_EMPTY_API_RESPONSE_ERROR_MESSAGE(
    "Received null or empty response for %s API. Response: %s"), COMMAND_LOGS_NO_OF_PAGES_MISMATCH_ERROR_MESSAGE(
    "Number of pages in command logs does not match. Expected: %s, Actual: %s"), COMMAND_LOGS_PAGE_SIZE_MISMATCH_ERROR_MESSAGE(
    "Number of pages in command logs does not match with total number of commands: %s and commands per page: %s. Expected: %s, Actual: %s"), COMMAND_LOGS_PAGE_START_TIME_NULL_OR_EMPTY_ERROR_MESSAGE(
    "Command logs page start time is null or empty for page: %s"), SHARE_LINK_VERIFICATION_FAILURE_ERROR_MESSAGE(
    "%s share link verification failed. Expected status code: 200, Actual: %s\nShare Link: %s"),

  //  CLIENT side error messages
  USER_IS_NOT_ABLE_TO_LOGGED_IN_CLIENT_ERROR_MESSAGE(
    "User is not logged in"), UNSUPPORTED_LOGS_TYPE_CLIENT_ERROR_MESSAGE(
    "Unsupported log type: %s"), UNABLE_TO_OPEN_COMMAND_LOGS_TAB_CLIENT_ERROR_MESSAGE(
    "Unable to open command logs tab"), UNABLE_TO_OPEN_NETWORK_LOGS_TAB_CLIENT_ERROR_MESSAGE(
    "Unable to open network logs tab"), UNABLE_TO_OPEN_SYSTEM_LOGS_TAB_CLIENT_ERROR_MESSAGE(
    "Unable to open system logs tab"), UNABLE_TO_OPEN_CONSOLE_LOGS_TAB_CLIENT_ERROR_MESSAGE(
    "Unable to open console logs tab"), UNABLE_TO_OPEN_PERFORMANCE_REPORT_TAB_CLIENT_ERROR_MESSAGE(
    "Unable to open performance report tab. Please check if you have passed capability performance true or UI is breaking."), UNABLE_TO_VERIFY_LOGS_IN_NEW_TAB_CLIENT_ERROR_MESSAGE(
    "Unable to open And verify %s logs in New Tab"), LOGS_NOT_PRESENT_IN_NEW_TAB_CLIENT_ERROR_MESSAGE(
    "%s logs are not displayed in new tab."), COMMAND_LOGS_COUNT_INVALID_CLIENT_ERROR_MESSAGE(
    "Command count should be greater than 0. Current count is %s"), COMMAND_NOT_DISPLAYED_CLIENT_ERROR_MESSAGE(
    "%s is not displayed"), VERIFICATION_DATA_FOR_COMMAND_LOGS_NOT_VALID_CLIENT_ERROR_MESSAGE(
    "Verification data for Command logs are invalid"), DATA_NOT_PRESENT_IN_COMMAND_LOGS_CLIENT_ERROR_MESSAGE_1(
    "Some URLs are not found in command logs. Missing URLs: %s"), DATA_NOT_PRESENT_IN_COMMAND_LOGS_CLIENT_ERROR_MESSAGE_2(
    "Some Locators are not found in command logs. Missing locators: %s"), FIRST_AND_LAST_COMMANDS_MISSING_IN_COMMAND_LOGS_CLIENT_ERROR_MESSAGE(
    "First and last commands were not displayed in the UI."), CONSOLE_LOGS_NOT_FOUND_CLIENT_ERROR_MESSAGE(
    "Console logs not found in UI"), SOME_CONSOLE_LOGS_ARE_MISSING_CLIENT_ERROR_MESSAGE(
    "Some console logs are missing. Missing console logs: %s"), UNABLE_TO_DOWNLOAD_CONSOLE_LOGS_CLIENT_ERROR_MESSAGE(
    "Unable to download console log file from UI"), CONSOLE_LOGS_NOT_SUPPORTED_ERROR_MESSAGE_NOT_PRESENT_CLIENT_ERROR_MESSAGE(
    "Error message of not supported browser for console logs is incorrect. Actual Message: %s, Expected Message: %s"), NETWORK_LOGS_VERIFICATION_DATA_NOT_VALID_CLIENT_ERROR_MESSAGE(
    "Verification data for Network logs are invalid"), URLS_NOT_PRESENT_IN_NETWORK_LOGS_CLIENT_ERROR_MESSAGE(
    "Some URLs are not found in Network logs. Missing URLs: %s"), UNABLE_TO_DOWNLOAD_NETWORK_LOGS_CLIENT_ERROR_MESSAGE(
    "Unable to download network logs from UI"), PERFORMANCE_REPORT_NOT_GENERATED_CLIENT_ERROR_MESSAGE(
    "Test performance report is not generated."), PERFORMANCE_REPORT_NOT_PRESENT_IN_UI_CLIENT_ERROR_MESSAGE(
    "Test performance report is not displayed in the UI."), UNABLE_TO_DOWNLOAD_SYSTEM_LOGS_CLIENT_ERROR_MESSAGE(
    "Unable to download system logs from UI"), SYSTEM_LOGS_NOT_GENERATED_CLIENT_ERROR_MESSAGE(
    "System logs are not generated."), SYSTEM_LOGS_SIZE_IS_LESSER_CLIENT_ERROR_MESSAGE(
    "System logs size is lesser than expected %s."), VIDEO_NOT_GENERATED_CLIENT_ERROR_MESSAGE(
    "Video verification failed from UI. Video is generated."), VIDEO_VERIFICATION_FAILED_CLIENT_ERROR_MESSAGE(
    "Video verification is not valid. Please check from UI and review the script."), VIDEO_DURATION_MISMATCH_CLIENT_ERROR_MESSAGE(
    "Video duration is less than 10 seconds. Duration: %s"), VIDEO_NOT_PLAYABLE_CLIENT_ERROR_MESSAGE(
    "Video is not playable. Video current time stamp after clicking on video play button and waiting for 10 secs: %s"),

  // Hard assertion messages
  TEST_STATUS_MISMATCH_ERROR_MESSAGE(
    "Test status verification failed after %d attempts. Expected: %s, Actual: %s"), UNABLE_TO_STOP_TEST_ERROR_MESSAGE(
    "Unable to initiate stop test as the test is not in Running state. Current state: %s"), UNABLE_TO_NAVIGATE_TO_LT_HOME_PAGE(
    "Unable to open test home page"), UNABLE_TO_STOP_BUILD_ERROR_MESSAGE(
    "Unable to initiate build stop as the build is not in Running state. Current state: %s"), UNABLE_TO_NAVIGATE_TO_PUBLIC_URL_MESSAGE(
    "Unable to open %s url"), UNABLE_TO_SET_BASELINE_FOR_AUTO_HEAL_TEST_ERROR_MESSAGE(
    "Unable to set baseline for auto heal test: %s."), TUNNEL_MODE_PORT_VERIFICATION_FAILURE_ERROR_MESSAGE(
    "Tunnel is not using the expected %s. Expected: %s, Actual: %s");

  private final String value;

  SoftAssertionMessages(String value) {
    this.value = value;
  }
}
