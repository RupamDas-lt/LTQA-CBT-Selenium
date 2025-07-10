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

    public ManualAccessibilityPage(DriverManager driverManager, CustomSoftAssert softAssert) {
        driver = driverManager;
        this.softAssert = softAssert;
    }

    private static final Locator a11yManualHomepageLocator = new Locator(LocatorTypes.XPATH, "(//h4[normalize-space()=\"Test Your App's Accessibility on Real Device\"])");
    private static final Locator androidApp = new Locator(LocatorTypes.XPATH, "//div[@class='newAppCard__data']//div[normalize-space()='YellQAApp']");
    private static final Locator iosApp = new Locator(LocatorTypes.XPATH, "//div[@class='newAppCard__data']//div[normalize-space()='babbelFlag']");
    private static final Locator device = new Locator(LocatorTypes.XPATH, "//div[@class='deviceSelectionBadge active']");
    private static final Locator searchBar = new Locator(LocatorTypes.XPATH, "//input[@id='devices-search-bar']");
    private static final Locator noDeviceFound = new Locator(LocatorTypes.XPATH, "//div[normalize-space()='No Results Found']");
    private static final Locator startButton = new Locator(LocatorTypes.XPATH, "//span[contains(text(),'Start')]");
    private static final Locator androidIcon = new Locator(LocatorTypes.XPATH, "//span[contains(@role,'tab')][1]");
    private static final Locator iosIcon = new Locator(LocatorTypes.XPATH, "//span[contains(@role,'tab')][2]");

    private static final String manualAccessibilityUrl = EnvSetup.TEST_ENV.toLowerCase().contains("stage") ?
            "https://stage-accessibility.lambdatestinternal.com/scanner/app" : "https://accessibility.lambdatest.com/scanner/app";


    public void navigateToManualAccessibilityPage() {
        try {
            driver.getURL(manualAccessibilityUrl);

            if (driver.isDisplayed(a11yManualHomepageLocator, 10))
                ltLogger.info("Accessibility Manual Page is Opened");
        } catch (Exception e) {
            throw new RuntimeException("Accessibility Manual Page is not Opening");
        }
    }

    public void iSelectAppAndDevice(String OS) {
        try {
            if (OS.equalsIgnoreCase("Android")) {
                driver.click(androidIcon, 2);
                driver.click(androidApp, 2);

                String[] androidVersions = {"15", "14", "13"};
                for (String version : androidVersions) {
                    driver.sendKeys(searchBar, version);

                    if (!driver.isDisplayed(noDeviceFound)) {
                        driver.click(device);
                        break;
                    } else
                        ltLogger.info("Android Device is not present. Please try again after some time when devices are present.");
                }
            } else {
                driver.click(iosIcon, 2);
                driver.click(iosApp, 2);
                String[] iosVersions = {"18", "17", "16"};
                for (String version : iosVersions) {
                    driver.sendKeys(searchBar, version);

                    if (!driver.isDisplayed(noDeviceFound)) {
                        driver.click(device);
                        break;
                    } else
                        ltLogger.info("iOS Device is not present. Please try again after some time when devices are present.");
                }
            }
            driver.click(startButton);
            ltLogger.info("Accessibility Test Initiated");
            driver.waitForTime(10);
        } catch (Exception e) {
            throw new RuntimeException("Test Not started due to device unavailability");
        }

    }
}
