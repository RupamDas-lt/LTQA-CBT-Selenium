package Pages;

import TestManagers.DriverManager;
import factory.Locator;
import factory.LocatorTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;
import utility.CustomSoftAssert;
import utility.EnvSetup;

import java.util.List;

public class ManualAccessibilityDashboardPage {

    DriverManager driver;
    CustomSoftAssert softAssert;
    private static final Logger ltLogger = LogManager.getLogger(ManualAccessibilityDashboardPage.class);

    public ManualAccessibilityDashboardPage(DriverManager driverManager, CustomSoftAssert softAssert) {
        driver = driverManager;
        this.softAssert = softAssert;
    }

    private static final Locator scannedReportHeading = new Locator(LocatorTypes.XPATH, "//h1[normalize-space()='Scanner Reports']");
    private static final Locator testList = new Locator(LocatorTypes.XPATH, "//a//h2");
    private static final Locator appTab = new Locator(LocatorTypes.XPATH, "//span[@id='app-label-undefined']");
    private static final Locator allIssuesTab = new Locator(LocatorTypes.XPATH, "//span[@data-content='All Issues']");
    private static final Locator mobileViewTab = new Locator(LocatorTypes.XPATH, "//span[@data-content='Mobile View']");
    private static final Locator testListDiv = new Locator(LocatorTypes.XPATH, "//div[@class='sc-eBHhsj jjwcCB']");
    private static final Locator mostSevereIssues = new Locator(LocatorTypes.XPATH, "//span[@class='Text-sc-17v1xeu-0 sc-iGgWBj bcaJdd dTYYls']");
    private static final Locator wcagGuidelines = new Locator(LocatorTypes.XPATH, "//span[@class='Text-sc-17v1xeu-0 sc-iGgWBj bkikMT hbaebV']");
    private static final Locator issueTable = new Locator(LocatorTypes.XPATH, "//div[@class='sc-eBHhsj bojVGS']");
    private static final Locator imageCanvas = new Locator(LocatorTypes.XPATH, "//canvas");

    private static final String manualDashboardUrl = EnvSetup.TEST_ENV.toLowerCase().contains("stage") ?
            "https://stage-accessibility.lambdatestinternal.com/" : "https://accessibility.lambdatest.com/";


    public void iOpenManualDashboard() {
        try {
            driver.getURL(manualDashboardUrl);

            if (driver.isDisplayed(scannedReportHeading, 10))
                ltLogger.info("Accessibility Manual Dashboard Opened");

            driver.click(appTab);
            driver.waitForElementToBeVisible(testListDiv, 10);
        } catch (Exception e) {
            throw new RuntimeException("Unable to navigate to Accessibility Manual Dashboard Page");
        }
    }

    public void iSearchForTheTest() {
        List<WebElement> testElements = driver.findElements(testList);
        String expectedTestName = ManualAccessibilitySessionPage.testName;
        boolean found = false;

        for (WebElement testElement : testElements) {
            String actualText = testElement.getText().replaceAll("[^0-9]", "");
            if (actualText.equalsIgnoreCase(expectedTestName)) {
                ltLogger.info("Test id {} is found", expectedTestName);
                testElement.click();
                found = true;
                break;
            }
        }
        if (!found) {
            throw new RuntimeException("Test id not found in the Manual Accessibility Dashboard list");
        }
    }

    public void iValidateAccessibilityReport() {
        softAssert.assertTrue(driver.isDisplayed(mostSevereIssues, 5) && driver.isDisplayed(wcagGuidelines, 5));
    }

    public void iValidateAllIssuesTab() {
        driver.click(allIssuesTab, 2);
        softAssert.assertTrue(driver.isDisplayed(issueTable, 2));
    }

    public void iValidateMobileView() {
        driver.click(mobileViewTab);
        softAssert.assertTrue(driver.isDisplayed(imageCanvas, 5));
    }
}
