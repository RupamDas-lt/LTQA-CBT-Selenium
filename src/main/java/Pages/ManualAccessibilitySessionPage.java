package Pages;

import TestManagers.DriverManager;
import factory.Locator;
import factory.LocatorTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.CustomSoftAssert;

import java.util.Random;

public class ManualAccessibilitySessionPage {

    DriverManager driver;
    CustomSoftAssert softAssert;
    private static final Logger ltLogger = LogManager.getLogger(ManualAccessibilitySessionPage.class);

    private static final Locator APP_SCREEN = new Locator(LocatorTypes.XPATH, "//video[@id='remote-view']");
    private static final Locator APP_ACCESSIBILITY_SCANNER_HEADING = new Locator(LocatorTypes.XPATH, "//h1[normalize-space()='App Accessibility Scanner']");
    private static final Locator APP_INSTALLING = new Locator(LocatorTypes.XPATH, "//h3[@id='app-controls-heading']");
    private static final Locator START_SCAN_BUTTON = new Locator(LocatorTypes.XPATH, "//button[contains(@class,'types__StyledButton-sc-ws60qy-0 btydTp')]");
    private static final Locator SAVE_TEST_BUTTON = new Locator(LocatorTypes.XPATH, "//span[contains(text(),'Save test')]");
    private static final Locator HOME_BUTTON = new Locator(LocatorTypes.XPATH, "//span[normalize-space()='Home']");
    private static final Locator SCAN_VIEWPORT_BUTTON = new Locator(LocatorTypes.XPATH, "//span[contains(text(),'Scan viewport')]");
    private static final Locator NUMBER_OF_PAGES = new Locator(LocatorTypes.XPATH, "//span[normalize-space()='View 2 of 2']");
    private static final Locator SAVE_REPORT_BUTTON = new Locator(LocatorTypes.XPATH, "//span[contains(text(),'Save report')]");
    private static final Locator ISSUE_TAB_BUTTON = new Locator(LocatorTypes.XPATH, "//span//span[normalize-space()='Issue']");
    private static final Locator LIVE_TAB_BUTTON = new Locator(LocatorTypes.XPATH, "//span//span[normalize-space()='Live']");
    private static final Locator IMAGE_AND_SKIN = new Locator(LocatorTypes.XPATH, "//img[@alt='device-skin']");
    private static final Locator SWITCH_MODE_MSG = new Locator(LocatorTypes.XPATH, "//img[@alt='device-skin']");
    private static final Locator SCANNING_IN_PROGRESS = new Locator(LocatorTypes.XPATH, "//h1[normalize-space()='Scanning for accessibility issues']");
    private static final Locator APP_CONTROL_BUTTON = new Locator(LocatorTypes.XPATH, "//span[normalize-space()='App Controls']");
    private static final Locator INSTALL_NEW_APP = new Locator(LocatorTypes.XPATH, "//span[@id='install-new-app']");
    private static final Locator SAMPLE_APP = new Locator(LocatorTypes.XPATH, "//div[@id='appcard-bundle-sample-app-emulator']");
    private static final Locator CAPTURE_SCREENSHOT = new Locator(LocatorTypes.XPATH, "//span[normalize-space()='Screenshot']");
    private static final Locator START_RECORDING = new Locator(LocatorTypes.XPATH, "//span[normalize-space()='Record Session']");
    private static final Locator STOP_RECORDING = new Locator(LocatorTypes.XPATH, "//img[@alt='Stop Recording']");
    private static final Locator VIDEO_RECORDING_CARD = new Locator(LocatorTypes.XPATH, "//div[@class='flex gap-8']");
    private static final Locator GALLERY = new Locator(LocatorTypes.XPATH, "//div[@id='gallery']");
    private static final Locator GALLERY_SS_SECTION = new Locator(LocatorTypes.XPATH, "//div[@class='Box-sc-g0xbh4-0 sc-fiCwlc ducviM pPKIC']//span[1]");
    private static final Locator GALLERY_SS_COUNT = new Locator(LocatorTypes.XPATH, "//div[@class='Box-sc-g0xbh4-0 sc-fiCwlc ducviM pPKIC']//span[2]");
    private static final Locator GALLERY_VIDEO_SECTION = new Locator(LocatorTypes.XPATH, "//div[@class='Box-sc-g0xbh4-0 sc-fiCwlc ducviM bThyy']//span[1]");
    private static final Locator GALLERY_VIDEO_COUNT = new Locator(LocatorTypes.XPATH, "//div[@class='Box-sc-g0xbh4-0 sc-fiCwlc ducviM bThyy']//span[2]");
    private static final Locator DEVICE_CONTROLS = new Locator(LocatorTypes.XPATH, "//div[@id='devicecontrols']");
    private static final Locator ROTATE = new Locator(LocatorTypes.XPATH, "//span[contains(text(),'Rotate')]");
    private static final Locator ROTATED_DEVICE = new Locator(LocatorTypes.XPATH, "//div[contains(@class,'streamMainComponent__relativeWrapper wireless-charger rotated pointer-none')]");
    private static final Locator END_SESSION_BUTTON = new Locator(LocatorTypes.XPATH, "//div[@id='endsession']");
    private static final Locator END_SESSION_CONFIRM = new Locator(LocatorTypes.XPATH, "//span[contains(text(),'Yes, End Session')]");
    private static final Locator A11Y_MANUAL_HOMEPAGE_LOCATOR = new Locator(LocatorTypes.XPATH, "(//h4[normalize-space()=\"Test Your App's Accessibility on Real Device\"])");
    private static final Locator TEST_NAME = new Locator(LocatorTypes.XPATH, "//input[contains(@placeholder,'Enter test name')]");
    public static String TestName;

    public ManualAccessibilitySessionPage(DriverManager driverManager, CustomSoftAssert softAssert) {
        driver = driverManager;
        this.softAssert = softAssert;
    }

    public void verifyTestStartedOrNot() {
        try {
            driver.waitForElementToBeVisible(APP_SCREEN, 2);
            driver.waitForElementToBeVisible(APP_ACCESSIBILITY_SCANNER_HEADING, 20);
            ltLogger.info("Test is Started for Accessibility Testing. Wait for app to install ...");

            if (driver.waitForElementToDisappear(APP_INSTALLING, 20) || driver.findElement(START_SCAN_BUTTON).getAttribute("aria-disabled").equalsIgnoreCase("false")) {
                driver.waitForTime(20);
                driver.click(START_SCAN_BUTTON);
            }
        } catch (Exception e) {
            ltLogger.info("App is not getting installed.");
        }
    }

    public void verifyScanHappeningOrNot() {
        try {
            if (driver.waitForElementToBeVisible(SAVE_TEST_BUTTON, 5).isDisplayed()) {
                ltLogger.info("Scan is happening, scanning 1 more time to confirm.");
                driver.click(HOME_BUTTON);
                driver.waitForTime(3);
                driver.click(SCAN_VIEWPORT_BUTTON);
                if (driver.waitForExactText(NUMBER_OF_PAGES, "View 2 of 2", 10) && driver.waitForElementToDisappear(SCANNING_IN_PROGRESS, 10))
                    ltLogger.info("Verification scan also happened. It means scanning is working properly");
            }
        } catch (Exception e) {
            ltLogger.info("Accessibility scan is not happening.");
        }
    }

    public void verifyTestSavingOrNot() {
        try {
            driver.click(SAVE_TEST_BUTTON);
            TestName = driver.findElement(TEST_NAME).getAttribute("value").replaceFirst("App Accessibility Test \\|\\s*", "").trim();
            driver.click(SAVE_REPORT_BUTTON, 2);

            if (driver.waitForElementToDisappear(SAVE_TEST_BUTTON, 10))
                ltLogger.info("Test has been saved.");
        } catch (Exception e) {
            ltLogger.info("Test Save feature is not working");
        }
    }

    public void verifyIssueTabAndImages() {
        try {
            driver.click(ISSUE_TAB_BUTTON);
            if (driver.waitForElementToBeVisible(IMAGE_AND_SKIN, 5).isDisplayed() &&
                    driver.waitForElementToBeVisible(SWITCH_MODE_MSG, 5).isDisplayed()) {
                ltLogger.info("Images and Switch to live mode to continue scanning message are visible");
                driver.waitForTime(2);
                driver.click(LIVE_TAB_BUTTON);
            }
        } catch (Exception e) {
            ltLogger.info("Images are not coming");
        }
    }

    public void verifyAppControls() {
        try {
            driver.click(APP_CONTROL_BUTTON);
            driver.waitForElementToBeVisible(INSTALL_NEW_APP, 2).click();
            driver.waitForElementToBeVisible(SAMPLE_APP, 5).click();
            driver.waitForElementToBeVisible(APP_INSTALLING, 10);
            ltLogger.info("Second App started installing");
            if (driver.waitForElementToDisappear(APP_INSTALLING, 15))
                ltLogger.info("Second App is installed");
        } catch (Exception e) {
            ltLogger.info("Second App is not installing. Check the app if it is compatible with the Android version.");
        }
    }

    public void screenshot() {
        try {
            Random random = new Random();
            int randomNumber = random.nextInt(5) + 1;

            for (int i = 0; i < randomNumber * 2; i++) {
                driver.click(CAPTURE_SCREENSHOT);
                driver.waitForTime(2);
            }
            driver.click(GALLERY);
            if (driver.findElement(GALLERY_SS_SECTION).getText().equalsIgnoreCase("Screenshots") && driver.findElement(GALLERY_SS_COUNT).getText().equals("0" + randomNumber))
                ltLogger.info("Correct number of Screenshots are getting generated i.e: {}", driver.findElement(GALLERY_SS_COUNT).getText());
        } catch (Exception e) {
            ltLogger.info("Incorrect number of Screenshots are getting generated");
        }
    }

    public void recordSession() {
        try {
            driver.click(START_RECORDING);
            driver.waitForTime(5);
            driver.click(STOP_RECORDING);
            driver.waitForElementToDisappear(VIDEO_RECORDING_CARD, 15);
            driver.waitForTime(5);
            driver.click(GALLERY);

            if (driver.findElement(GALLERY_VIDEO_SECTION).getText().equalsIgnoreCase("Videos") && driver.findElement(GALLERY_VIDEO_COUNT).getText().equals("01"))
                ltLogger.info("Correct number of Videos are getting generated i.e: {}", driver.findElement(GALLERY_VIDEO_COUNT).getText());
        } catch (Exception e) {
            ltLogger.info("Incorrect number of Videos are getting generated");
        }
    }

    public void rotate() {
        try {
            driver.click(DEVICE_CONTROLS);
            driver.waitForElementToBeVisible(ROTATE, 2).click();

            if (driver.waitForElementToBeVisible(ROTATED_DEVICE, 5).isDisplayed())
                ltLogger.info("Device is rotated");
        } catch (Exception e) {
            ltLogger.info("Device rotation not working");
        }
    }

    public void iStopAccessibilityTest() {
        driver.click(END_SESSION_BUTTON);
        driver.click(END_SESSION_CONFIRM, 2);

        if (driver.waitForElementToBeVisible(A11Y_MANUAL_HOMEPAGE_LOCATOR, 2).isDisplayed()) {
            ltLogger.info("Test Ended Successfully");
        }
    }
}
