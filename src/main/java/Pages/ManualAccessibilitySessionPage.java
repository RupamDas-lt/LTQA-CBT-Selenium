package Pages;

import TestManagers.DriverManager;
import factory.Locator;
import factory.LocatorTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.BaseClass;
import utility.CustomSoftAssert;

import java.util.Random;

import static factory.SoftAssertionMessagesAccessibility.*;

public class ManualAccessibilitySessionPage extends BaseClass {

    DriverManager driver;
    CustomSoftAssert softAssert;
    private static final Logger ltLogger = LogManager.getLogger(ManualAccessibilitySessionPage.class);

    public ManualAccessibilitySessionPage(DriverManager driverManager, CustomSoftAssert softAssert) {
        driver = driverManager;
        this.softAssert = softAssert;
    }

    private static final Locator appScreen = new Locator(LocatorTypes.XPATH, "//video[@id='remote-view']");
    private static final Locator accessibilityScannerHeading = new Locator(LocatorTypes.XPATH, "//h1[normalize-space()='App Accessibility Scanner']");
    private static final Locator appInstalling = new Locator(LocatorTypes.XPATH, "//h3[@id='app-controls-heading']");
    private static final Locator startScanButton = new Locator(LocatorTypes.XPATH, "//button[contains(@class,'types__StyledButton-sc-ws60qy-0 btydTp')]");
    private static final Locator saveTestButton = new Locator(LocatorTypes.XPATH, "//span[contains(text(),'Save test')]");
    private static final Locator homeButton = new Locator(LocatorTypes.XPATH, "//span[normalize-space()='Home']");
    private static final Locator scanViewportButton = new Locator(LocatorTypes.XPATH, "//span[contains(text(),'Scan viewport')]");
    private static final Locator numberOfPages = new Locator(LocatorTypes.XPATH, "//span[normalize-space()='View 2 of 2']");
    private static final Locator saveReportButton = new Locator(LocatorTypes.XPATH, "//span[contains(text(),'Save report')]");
    private static final Locator issueTabButton = new Locator(LocatorTypes.XPATH, "//span//span[normalize-space()='Issue']");
    private static final Locator liveTabButton = new Locator(LocatorTypes.XPATH, "//span//span[normalize-space()='Live']");
    private static final Locator imageAndSkin = new Locator(LocatorTypes.XPATH, "//img[@alt='device-skin']");
    private static final Locator switchModeMsg = new Locator(LocatorTypes.XPATH, "//span[normalize-space()='Switch to live mode to continue scanning']");
    private static final Locator scanningInProgress = new Locator(LocatorTypes.XPATH, "//h1[normalize-space()='Scanning for accessibility issues']");
    private static final Locator appControlsButton = new Locator(LocatorTypes.XPATH, "//span[normalize-space()='App Controls']");
    private static final Locator installNewApp = new Locator(LocatorTypes.XPATH, "//span[@id='install-new-app']");
    private static final Locator sampleApp = new Locator(LocatorTypes.XPATH, "//div[@id='appcard-bundle-sample-app-emulator']");
    private static final Locator captureScreenshot = new Locator(LocatorTypes.XPATH, "//span[normalize-space()='Screenshot']");
    private static final Locator startRecording = new Locator(LocatorTypes.XPATH, "//span[normalize-space()='Record Session']");
    private static final Locator stopRecording = new Locator(LocatorTypes.XPATH, "//img[@alt='Stop Recording']");
    private static final Locator videoRecordingCard = new Locator(LocatorTypes.XPATH, "//div[@class='flex gap-8']");
    private static final Locator gallery = new Locator(LocatorTypes.XPATH, "//div[@id='gallery']");
    private static final Locator gallerySsSection = new Locator(LocatorTypes.XPATH, "//div[@class='Box-sc-g0xbh4-0 sc-fiCwlc ducviM pPKIC']//span[1]");
    private static final Locator gallerySsCount = new Locator(LocatorTypes.XPATH, "//div[@class='Box-sc-g0xbh4-0 sc-fiCwlc ducviM pPKIC']//span[2]");
    private static final Locator galleryVideoSection = new Locator(LocatorTypes.XPATH, "//div[@class='Box-sc-g0xbh4-0 sc-fiCwlc ducviM bThyy']//span[1]");
    private static final Locator galleryVideoCount = new Locator(LocatorTypes.XPATH, "//div[@class='Box-sc-g0xbh4-0 sc-fiCwlc ducviM bThyy']//span[2]");
    private static final Locator deviceControls = new Locator(LocatorTypes.XPATH, "//div[@id='devicecontrols']");
    private static final Locator rotate = new Locator(LocatorTypes.XPATH, "//span[contains(text(),'Rotate')]");
    private static final Locator rotatedDevice = new Locator(LocatorTypes.XPATH, "//div[contains(@class,'streamMainComponent__relativeWrapper wireless-charger rotated pointer-none')]");
    private static final Locator endSessionButton = new Locator(LocatorTypes.XPATH, "//div[@id='endsession']");
    private static final Locator endSessionConfirm = new Locator(LocatorTypes.XPATH, "//span[contains(text(),'Yes, End Session')]");
    private static final Locator a11yMAnualHomepageLocator = new Locator(LocatorTypes.XPATH, "(//h4[normalize-space()=\"Test Your App's Accessibility on Real Device\"])");
    private static final Locator TEST_NAME = new Locator(LocatorTypes.XPATH, "//input[contains(@placeholder,'Enter test name')]");
    public static final ThreadLocal<String> testName = new ThreadLocal<>();

    public void verifyTestStartedOrNot() {
        try {
            driver.waitForElementToBeVisible(appScreen, 2);
            driver.waitForElementToBeVisible(accessibilityScannerHeading, 20);
            softAssert.assertTrue(driver.isDisplayed(accessibilityScannerHeading, 20), softAssertMessageFormat(TEST_STARTED, "Accessibility Testing"));
            if (driver.waitForElementToDisappear(appInstalling, 20) || driver.findElement(startScanButton).getAttribute("aria-disabled").equalsIgnoreCase("false")) {
                driver.waitForTime(20);
                driver.click(startScanButton);
            }
        } catch (Exception e) {
            throw new RuntimeException("App is not getting installed.");
        }
    }

    public void verifyScanHappeningOrNot() {
        try {
            if (driver.isDisplayed(saveTestButton, 5)) {
                ltLogger.info("Scan is happening, scanning 1 more time to confirm.");
                driver.click(homeButton);
                driver.waitForTime(3);
                driver.click(scanViewportButton);

                softAssert.assertTrue(driver.waitForExactText(numberOfPages, "View 2 of 2", 10) && driver.waitForElementToDisappear(scanningInProgress, 10), softAssertMessageFormat(SCAN_WORKING, "Accessibility Manual"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Accessibility scan is not happening.");
        }
    }

    public void verifyTestSavingOrNot() {
        try {
            driver.click(saveTestButton);
            testName.set(driver.findElement(TEST_NAME).getAttribute("value").replaceFirst("App Accessibility Test \\|\\s*", "").trim());
            driver.click(saveReportButton, 2);

            softAssert.assertTrue(driver.waitForElementToDisappear(saveTestButton, 10), softAssertMessageFormat(TEST_SAVED));
        } catch (Exception e) {
            throw new RuntimeException("Test Save feature is not working");
        }
    }

    public void verifyIssueTabAndImages() {
        try {
            driver.click(issueTabButton);

            softAssert.assertTrue(driver.isDisplayed(imageAndSkin, 5) && driver.isDisplayed(switchModeMsg, 5), softAssertMessageFormat(SWITCHED_TO, "Issue tab"));
            driver.waitForTime(2);
            driver.click(liveTabButton);
            softAssert.assertTrue(driver.isDisplayed(appScreen, 5), softAssertMessageFormat(SWITCHED_TO, "Live tab"));
        } catch (Exception e) {
            throw new RuntimeException("Images are not coming");
        }
    }

    public void verifyAppControls() {
        try {
            driver.click(appControlsButton);
            driver.click(installNewApp, 2);
            driver.click(sampleApp, 5);
            driver.waitForElementToBeVisible(appInstalling, 10);
            ltLogger.info("Second App started installing");

            softAssert.assertTrue(driver.waitForElementToDisappear(appInstalling, 15), softAssertMessageFormat(APP_IS_INSTALLED));
        } catch (Exception e) {
            throw new RuntimeException("Second App is not installing. Check the app if it is compatible with the Android version.");
        }
    }

    public void screenshot() {
        try {
            Random random = new Random();
            int randomNumber = random.nextInt(5) + 1;

            for (int i = 0; i < randomNumber * 2; i++) {
                driver.click(captureScreenshot);
                driver.waitForTime(2);
            }
            driver.click(gallery);

            softAssert.assertTrue(driver.findElement(gallerySsSection).getText().equalsIgnoreCase("Screenshots") && driver.findElement(gallerySsCount).getText().equals("0" + randomNumber), softAssertMessageFormat(GALLERY_VERIFICATION, "Screenshots") + driver.findElement(gallerySsCount).getText());
        } catch (Exception e) {
            throw new RuntimeException("Incorrect number of Screenshots are getting generated");
        }
    }

    public void recordSession() {
        try {
            driver.click(startRecording);
            driver.waitForTime(5);
            driver.click(stopRecording);
            driver.waitForElementToDisappear(videoRecordingCard, 15);
            driver.waitForTime(5);
            driver.click(gallery);

            softAssert.assertTrue(driver.findElement(galleryVideoSection).getText().equalsIgnoreCase("Videos") && driver.findElement(galleryVideoCount).getText().equals("01"), softAssertMessageFormat(GALLERY_VERIFICATION, "Videos") + driver.findElement(galleryVideoCount).getText());
        } catch (Exception e) {
            throw new RuntimeException("Incorrect number of Videos are getting generated");
        }
    }

    public void rotate() {
        try {
            driver.click(deviceControls);
            driver.waitForTime(1);
            driver.click(rotate, 2);

            softAssert.assertTrue(driver.isDisplayed(rotatedDevice, 5), softAssertMessageFormat(DEVICE_ROTATED));
        } catch (Exception e) {
            throw new RuntimeException("Device rotation not working");
        }
    }

    public void iStopAccessibilityTest() {
        driver.click(endSessionButton);
        driver.click(endSessionConfirm, 2);

        softAssert.assertTrue(driver.isDisplayed(a11yMAnualHomepageLocator, 2), softAssertMessageFormat(TEST_ENDED, "Manual Accessibility"));
    }
}
