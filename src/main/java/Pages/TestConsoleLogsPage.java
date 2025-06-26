package Pages;

import TestManagers.DriverManager;
import factory.Locator;
import factory.LocatorTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.CustomSoftAssert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static factory.SoftAssertionMessages.*;
import static utility.FrameworkConstants.consoleLogs;

public class TestConsoleLogsPage extends LTDashboardCommonActions {

    private final Logger ltLogger = LogManager.getLogger(TestConsoleLogsPage.class);

    DriverManager driver;
    CustomSoftAssert softAssert;

    private static final HashMap<String, String> actualLoglevelToFELogLevelsMap = new HashMap<>() {{
        put("warn", "WARNING");
        put("log", "INFO");
        put("error", "SEVERE");
        put("info", "INFO");
    }};

    private static final Locator allLogsTab = new Locator(LocatorTypes.ID, "test-detail-logs-tab");
    private static final Locator consoleLogsTab = new Locator(LocatorTypes.CSS, "li[id='lt-test-detail-logs-console']");
    private static final Locator consoleLogsNotSupportedMessageContainer = new Locator(LocatorTypes.CSS,
            "div[class*='ComponentNoDataFound'] h2");
    private static final Locator consoleLogsParentContainer = new Locator(LocatorTypes.CSS,
            "div[id='test-detail-logs-console']");
    private static final Locator consoleLogsNotFoundMessage = new Locator(LocatorTypes.CSS,
            "#test-detail-logs-console div[class*='ComponentNoDataFound_message']");
    private static final Locator consoleLogsRowsContainer = new Locator(LocatorTypes.CSS,
            "div[aria-label='console logs']");
    private static final Locator consoleLogRowLocator = new Locator(LocatorTypes.CSS,
            "div[class*='innerScrollContainer']>div");

    public TestConsoleLogsPage(String testId, DriverManager driverManager, CustomSoftAssert softAssert) {
        super(testId, driverManager);
        driver = driverManager;
        this.softAssert = softAssert;
    }

    public boolean openConsoleLogsTab() {
        navigateToHomePageOfSpecificTest();
        waitForTime(10);
        driver.click(allLogsTab);
        driver.click(consoleLogsTab, 2);
        return driver.isDisplayed(consoleLogsParentContainer, 5);
    }

    private boolean consoleLogsNotFoundMessageDisplayed() {
        return driver.isDisplayed(consoleLogsNotFoundMessage, 5);
    }

    private List<String> constructExpectedConsoleLogMessage() {
        final String messagePrefix = "console-api 2:32";
        List<String> logMessages = new ArrayList<>();
        consoleLogs.forEach((key, value) -> {
            String expectedLogLevel = actualLoglevelToFELogLevelsMap.get(key);
            String extractedLogMessage = String.format("%s \"%s\"", messagePrefix, value);
            String expectedLogMessageWithLevel = expectedLogLevel + extractedLogMessage;
            logMessages.add(expectedLogMessageWithLevel);
        });
        ltLogger.info("Expected Console log messages with log levels: {}", logMessages);
        return logMessages;
    }

    public void verifyConsoleLogsFromUI(int... customRetryCount) {
        int retryCount = customRetryCount != null && customRetryCount.length > 0 ? customRetryCount[0] : 2;
        if (consoleLogsNotFoundMessageDisplayed()) {
            softAssert.fail(softAssertMessageFormat(CONSOLE_LOGS_NOT_FOUND_CLIENT_ERROR_MESSAGE));
            return;
        }
        List<String> expectedConsoleLogs = constructExpectedConsoleLogMessage();
        String consoleLogsPresentInDashboard = "";
        for (int i = 1; i <= retryCount; i++) {
            ltLogger.info("Trying to extract console logs from UI. Attempt: {}", i);
            try {
                consoleLogsPresentInDashboard = driver.getText(consoleLogsRowsContainer, 5);
                break;
            } catch (Exception e) {
                ltLogger.error("Failed to extract console logs from dashboard. Error: {}", e.getMessage());
                driver.refreshPage();
                waitForTime(5);
            }
        }
        List<String> notFoundConsoleLogs = new ArrayList<>();
        for (String consoleLog : expectedConsoleLogs) {
            ltLogger.info("Searching for console log: {}", consoleLog);
            if (!consoleLogsPresentInDashboard.contains(consoleLog))
                notFoundConsoleLogs.add(consoleLog);
        }
        ltLogger.info("Missing console logs: {}", notFoundConsoleLogs);
        softAssert.assertTrue(notFoundConsoleLogs.isEmpty(),
                softAssertMessageFormat(SOME_CONSOLE_LOGS_ARE_MISSING_CLIENT_ERROR_MESSAGE, notFoundConsoleLogs));
    }

    public void downloadConsoleLogsFromUI(String expectedFileName) {
        boolean consoleLogsFileDownloadStatus = downloadLogFile(expectedFileName, "Console");
        softAssert.assertTrue(consoleLogsFileDownloadStatus,
                softAssertMessageFormat(UNABLE_TO_DOWNLOAD_CONSOLE_LOGS_CLIENT_ERROR_MESSAGE));
    }

    public void openConsoleLogsInNewTabAndVerify() {
        String errorMessage = openLogsInNewTabAndVerify(softAssert, "console", consoleLogRowLocator, 5);
        softAssert.assertTrue(errorMessage.isEmpty(), errorMessage);
    }

    public void verifyConsoleLogsNotSupportedMessageDisplayed() {
        final String expectedErrorMessage = "Console Logs are not supported on the selected Browser";
        String message = driver.getText(consoleLogsNotSupportedMessageContainer, 2);
        softAssert.assertTrue(message.contains(expectedErrorMessage),
                softAssertMessageFormat(CONSOLE_LOGS_NOT_SUPPORTED_ERROR_MESSAGE_NOT_PRESENT_CLIENT_ERROR_MESSAGE, message,
                        expectedErrorMessage));
    }

}
