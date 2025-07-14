package Pages.Accessibility;

import TestManagers.DriverManager;
import factory.Locator;
import factory.LocatorTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.EnvSetup;


public class ManualAccessibilityPage extends EnvSetup {
    DriverManager driver;
    private static final Logger ltLogger = LogManager.getLogger(ManualAccessibilityPage.class);

    public ManualAccessibilityPage(DriverManager driverManager) {
        driver = driverManager;
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

        driver.getURL(manualAccessibilityUrl);
        if (driver.isDisplayed(a11yManualHomepageLocator, 10))
            ltLogger.info("Accessibility Manual Page is Opened");
        else
            throw new RuntimeException("Accessibility Manual Page is not Opening");
    }

    public void iSelectAppAndDevice(String OS) {
        try {
            Locator icon, app;
            String[] versions;
            String platformName;

            if (OS.equalsIgnoreCase("Android")) {
                icon = androidIcon;
                app = androidApp;
                versions = new String[]{"15", "14", "13"};
                platformName = "Android";
            } else {
                icon = iosIcon;
                app = iosApp;
                versions = new String[]{"18", "17", "16"};
                platformName = "iOS";
            }

            driver.click(icon, 2);
            driver.click(app, 2);

            boolean deviceSelected = false;
            for (String version : versions) {
                driver.sendKeys(searchBar, version);

                if (!driver.isDisplayed(noDeviceFound)) {
                    driver.click(device);
                    deviceSelected = true;
                    break;
                }
            }

            if (!deviceSelected) {
                throw new RuntimeException(platformName + " device is not present for any of the following versions: " + String.join(", ", versions));
            }

            driver.click(startButton);
            ltLogger.info("Accessibility Test Initiated on {}", platformName);
            driver.waitForTime(10);

        } catch (Exception e) {
            throw new RuntimeException("Test not started due to device unavailability");
        }
    }

}
