package Pages;

import TestManagers.DriverManager;
import factory.Locator;
import factory.LocatorTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;
import utility.BaseClass;
import utility.CustomSoftAssert;

import java.util.List;

import static factory.SoftAssertionMessages.*;

public class TestShareLinkPage extends BaseClass {
    private final Logger ltLogger = LogManager.getLogger(TestShareLinkPage.class);

    DriverManager driver;
    CustomSoftAssert softAssert;

    private final String SHARE_LINK;
    private static final Locator buildName = new Locator(LocatorTypes.CSS,
            "#testHeader>div:nth-child(1)>div:nth-child(1) span");
    private static final Locator testName = new Locator(LocatorTypes.ID, "testName");
    private static final Locator testDetailsHomeTab = new Locator(LocatorTypes.ID, "test-detail-home-tab");
    private static final Locator testDetailsLogsTab = new Locator(LocatorTypes.ID, "test-detail-logs-tab");
    private static final Locator testDetailsNetworkTab = new Locator(LocatorTypes.ID, "test-detail-network-tab");
    private static final Locator testDetailsVideo = new Locator(LocatorTypes.TAG_NAME, "video");

    private static final Locator commandLogsRow = new Locator(LocatorTypes.CSS, "div[class*='indexV2_commandLogTitleSection']");
    private static final Locator seleniumLogsContainer = new Locator(LocatorTypes.CSS, "div[aria-label='selenium logs']");
    private static final Locator networkLogsRows = new Locator(LocatorTypes.CSS, "tr[id*='network-log-row']");
    private static final Locator videoPlayButton = new Locator(LocatorTypes.CSS, "button[aria-label='Play Video']");

    private static final String firstCommandTitle = "New Session";
    private static final String lastCommandTitle = "Delete Session";

    public TestShareLinkPage(DriverManager driverManager, CustomSoftAssert softAssert, String shareLink) {
        driver = driverManager;
        this.softAssert = softAssert;
        SHARE_LINK = shareLink;
    }

    public boolean navigateToShareLink() {
        driver.getURL(SHARE_LINK);
        boolean isSharePageOpened = driver.isDisplayed(testName, 20);
        softAssert.assertTrue(isSharePageOpened,
                softAssertMessageFormat(UNABLE_TO_OPEN_SHARE_PAGE_ERROR_MESSAGE, "test", SHARE_LINK));
        return isSharePageOpened;
    }

    public void verifyBuildName(String expectedBuildName) {
        String actualBuildNameText = driver.getText(buildName);
        softAssert.assertTrue(expectedBuildName.equals(actualBuildNameText),
                softAssertMessageFormat(BUILD_NAME_MISMATCH_CLIENT_ERROR_MESSAGE, "test share page", expectedBuildName,
                        actualBuildNameText));
    }

    public void verifyTestName(String expectedTestName) {
        String actualTestNameText = driver.getText(testName);
        softAssert.assertTrue(expectedTestName.equals(actualTestNameText),
                softAssertMessageFormat(TEST_NAME_MISMATCH_CLIENT_ERROR_MESSAGE, "test share page", expectedTestName,
                        actualTestNameText));
    }

    private void verifyCommandLogs() {
        if (driver.isDisplayed(commandLogsRow, 60)) {
            List<WebElement> commandLogs = driver.findElements(commandLogsRow);
            String firstCommandTitleText = driver.getText(commandLogs.getFirst());
            String lastCommandTitleText = driver.getText(commandLogs.getLast());
            ltLogger.info("First command title: {}, Last command title: {}", firstCommandTitleText, lastCommandTitleText);
            softAssert.assertTrue(
                    firstCommandTitleText.contains(firstCommandTitle) && lastCommandTitleText.contains(lastCommandTitle),
                    softAssertMessageFormat(LOGS_NOT_PRESENT_IN_SHARE_PAGE_CLIENT_ERROR_MESSAGE, "Command"));
        } else {
            softAssert.fail(softAssertMessageFormat(LOGS_NOT_PRESENT_IN_SHARE_PAGE_CLIENT_ERROR_MESSAGE, "Command"));
        }
    }

    private void verifySeleniumLogs() {
        if (driver.isDisplayed(seleniumLogsContainer, 5) && driver.getText(seleniumLogsContainer).length() > 100) {
            ltLogger.info("Selenium logs are present in the share link page.");
        } else {
            ltLogger.warn("Selenium logs are not present in the share link page.");
            softAssert.fail(softAssertMessageFormat(LOGS_NOT_PRESENT_IN_SHARE_PAGE_CLIENT_ERROR_MESSAGE, "Selenium"));
        }
    }

    private void verifyNetworkLogs() {
        if (driver.isDisplayed(networkLogsRows, 10)) {
            List<WebElement> networkLogs = driver.findElements(networkLogsRows);
            ltLogger.info("Network logs count: {}", networkLogs.size());
            softAssert.assertTrue(networkLogs.size() > 10,
                    softAssertMessageFormat(LOGS_NOT_PRESENT_IN_SHARE_PAGE_CLIENT_ERROR_MESSAGE, "Network"));
        } else {
            softAssert.fail(softAssertMessageFormat(LOGS_NOT_PRESENT_IN_SHARE_PAGE_CLIENT_ERROR_MESSAGE, "Network"));
        }
    }

    private void verifyVideo() {
        if (driver.isDisplayed(testDetailsVideo, 10)) {
            ltLogger.info("Video is present in the share link page.");
            softAssert.assertTrue(driver.isClickable(videoPlayButton),
                    softAssertMessageFormat(VIDEO_NOT_PRESENT_IN_SHARE_PAGE_CLIENT_ERROR_MESSAGE));
        } else {
            ltLogger.warn("Video is not present in the share link page.");
            softAssert.fail(softAssertMessageFormat(VIDEO_NOT_PRESENT_IN_SHARE_PAGE_CLIENT_ERROR_MESSAGE));
        }
    }

    public void verifyArtefactsAreDisplayed() {
        driver.click(testDetailsHomeTab);
        verifyCommandLogs();
        driver.click(testDetailsLogsTab);
        verifySeleniumLogs();
        driver.click(testDetailsNetworkTab);
        verifyNetworkLogs();
        verifyVideo();
    }
}
