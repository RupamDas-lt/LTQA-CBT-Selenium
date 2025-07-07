package Pages;

import TestManagers.DriverManager;
import factory.Locator;
import factory.LocatorTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.CustomSoftAssert;
import utility.EnvSetup;


public class ManualAccessibilityPage extends EnvSetup {
    DriverManager driver;
    CustomSoftAssert softAssert;
    private static final Logger ltLogger = LogManager.getLogger(ManualAccessibilityPage.class);


    String prod_url = "https://accessibility.lambdatest.com/scanner/app";
    String stage_url = "https://stage-accessibility.lambdatestinternal.com/scanner/app";

    private static final Locator A11Y_MANUAL_HOMEPAGE_LOCATOR = new Locator(LocatorTypes.XPATH, "(//h4[normalize-space()=\"Test Your App's Accessibility on Real Device\"])");
    private static final Locator ANDROID_APP = new Locator(LocatorTypes.XPATH, "//div[@class='newAppCard__data']//div[normalize-space()='YellQAApp']");
    private static final Locator IOS_APP = new Locator(LocatorTypes.XPATH, "//div[@class='newAppCard__data']//div[normalize-space()='babbelFlag']");
    private static final Locator DEVICE = new Locator(LocatorTypes.XPATH, "//div[@class='deviceSelectionBadge active']");
    private static final Locator SEARCH_BAR = new Locator(LocatorTypes.XPATH, "//input[@id='devices-search-bar']");
    private static final Locator NO_DEVICE_FOUND = new Locator(LocatorTypes.XPATH, "//div[normalize-space()='No Results Found']");
    private static final Locator START_BUTTON = new Locator(LocatorTypes.XPATH, "//span[contains(text(),'Start')]");
    private static final Locator ANDROID_ICON = new Locator(LocatorTypes.XPATH, "//span[contains(@role,'tab')][1]");
    private static final Locator IOS_ICON = new Locator(LocatorTypes.XPATH, "//span[contains(@role,'tab')][2]");


    public ManualAccessibilityPage(DriverManager driverManager, CustomSoftAssert softAssert) {
        driver = driverManager;
        this.softAssert = softAssert;
    }

    public void navigateToManualAccessibilityPage() {
        if (EnvSetup.TEST_ENV.equalsIgnoreCase("prod"))
            driver.getURL(prod_url);
        else
            driver.getURL(stage_url);

        if (driver.waitForElementToBeVisible(A11Y_MANUAL_HOMEPAGE_LOCATOR, 10).isDisplayed())
            ltLogger.info("Accessibility Manual Page is Opened");
    }

    public void iSelectAppAndDevice(String OS) {

        if (OS.equalsIgnoreCase("Android")) {
            driver.waitForElementToBeVisible(ANDROID_ICON, 2).click();
            driver.click(ANDROID_APP, 2);

            String[] androidVersions = {"15", "14", "13"};
            for (String version : androidVersions) {
                driver.sendKeys(SEARCH_BAR, version);

                if (!driver.isDisplayed(NO_DEVICE_FOUND)) {
                    driver.findElement(DEVICE).click();
                    break;
                } else
                    ltLogger.info("Android Device is not present. Please try again after some time when devices are present.");
            }
        } else {
            driver.waitForElementToBeVisible(IOS_ICON, 2).click();
            driver.click(IOS_APP, 2);

            String[] iosVersions = {"18", "17", "16"};
            for (String version : iosVersions) {
                driver.sendKeys(SEARCH_BAR, version);
                if (!driver.isDisplayed(NO_DEVICE_FOUND)) {
                    driver.findElement(DEVICE).click();
                    break;
                } else
                    ltLogger.info("iOS Device is not present. Please try again after some time when devices are present.");
            }
        }

        driver.click(START_BUTTON);
        ltLogger.info("Accessibility Test Initiated");
        driver.waitForTime(10);
    }
}
