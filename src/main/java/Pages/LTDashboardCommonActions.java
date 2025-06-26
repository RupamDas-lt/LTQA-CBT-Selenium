package Pages;

import TestManagers.DriverManager;
import automationHelper.LTHooks;
import factory.Locator;
import factory.LocatorTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.BaseClass;
import utility.CustomSoftAssert;
import utility.EnvSetup;

import static factory.SoftAssertionMessages.LOGS_NOT_PRESENT_IN_NEW_TAB_CLIENT_ERROR_MESSAGE;
import static factory.SoftAssertionMessages.UNABLE_TO_VERIFY_LOGS_IN_NEW_TAB_CLIENT_ERROR_MESSAGE;
import static utility.FrameworkConstants.HTTPS;

public class LTDashboardCommonActions extends BaseClass {
    DriverManager driver;
    private final Logger ltLogger = LogManager.getLogger(LTDashboardCommonActions.class);

    private String testDashboardHomePage = HTTPS + EnvSetup.TEST_DASHBOARD_URL_BASE + "/test?selectedTab=home&testID=<TEST_ID>";

    Locator testPageLogsParentContainer = new Locator(LocatorTypes.CSS, "div[class*='AutomationLogsPage_mainDiv']");
    Locator testIDCopyButton = new Locator(LocatorTypes.ID, "testIdCopyButton");
    Locator testLogsDownloadButton = new Locator(LocatorTypes.CSS, "button[aria-label*='Download']");
    Locator logsFullScreenViewButton = new Locator(LocatorTypes.CSS, "button[aria-label='Open in a new tab']");

    public LTDashboardCommonActions(String testId, DriverManager driverManager) {
        testDashboardHomePage = testDashboardHomePage.replace("<TEST_ID>", testId);
        driver = driverManager;
    }

    public boolean navigateToHomePageOfSpecificTest() {
        driver.getURL(testDashboardHomePage);
        return driver.isDisplayed(testPageLogsParentContainer, 5);
    }

    public String getTestIDFromTestDashboard() {
        driver.click(testIDCopyButton);
        waitForTime(2);
        return LTHooks.getClipboard(driver);
    }

    public boolean checkIfCurrentUrlContainsLogNameAsQueryParam(String logName) {
        if (logName.equalsIgnoreCase("system") || logName.equalsIgnoreCase("webdriver"))
            logName = "selenium";
        ltLogger.info("Checking if url contains log name as query parameter: {}", logName);
        String currentUrl = driver.getCurrentURL();
        if (!currentUrl.contains(logName.toLowerCase()))
            ltLogger.warn(
                    "Current URL does not contain '{}'. URL, navigating to logs page should contain that log type in url query params. Current URL: '{}'",
                    logName, currentUrl);
        return currentUrl.contains(logName);
    }

    public boolean downloadLogFile(String logFileName, String logType, int... customTimeout) {
        checkIfCurrentUrlContainsLogNameAsQueryParam(logType);

        int timeout = customTimeout == null || customTimeout.length == 0 ? 5 : customTimeout[0];
        ltLogger.info("Downloading {} logs from UI, with file name: {} and timeout: {}", logType, logFileName, timeout);
        driver.click(testLogsDownloadButton);
        waitForTime(timeout);
        return LTHooks.isFileExist(driver, logFileName, 5);
    }

    public String openLogsInNewTabAndVerify(CustomSoftAssert softAssert, String logType,
                                            Locator expectedLocatorToBePresent, int... customTimeout) {
        String errorMessage = "";
        int timeout = customTimeout == null || customTimeout.length == 0 ? 2 : customTimeout[0];
        ltLogger.info("Verifying {} logs in new tab", logType);
        driver.click(logsFullScreenViewButton, 2);
        waitForTime(timeout);
        try {
            driver.switchToTab(1);
        } catch (Exception e) {
            ltLogger.error("Unable to open {} logs in new tab. Error: {}", logType, e.getMessage());
            errorMessage = softAssertMessageFormat(UNABLE_TO_VERIFY_LOGS_IN_NEW_TAB_CLIENT_ERROR_MESSAGE, logType);
            return errorMessage;
        }
        checkIfCurrentUrlContainsLogNameAsQueryParam(logType);
        if (!driver.isDisplayed(expectedLocatorToBePresent)) {
            errorMessage = softAssertMessageFormat(LOGS_NOT_PRESENT_IN_NEW_TAB_CLIENT_ERROR_MESSAGE, logType);
        }
        driver.closeCurrentTabAndSwitchContextToLastTab();
        return errorMessage;
    }
}
