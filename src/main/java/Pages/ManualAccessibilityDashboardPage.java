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

    private static final Locator SCANNED_REPORT_HEADING = new Locator(LocatorTypes.XPATH, "//h1[normalize-space()='Scanner Reports']");
    private static final Locator TEST_LIST = new Locator(LocatorTypes.XPATH, "//a//h2");
    private static final Locator APP_TAB = new Locator(LocatorTypes.XPATH, "//span[@id='app-label-undefined']");
    private static final Locator ALL_ISSUES_TAB = new Locator(LocatorTypes.XPATH, "//span[@data-content='All Issues']");
    private static final Locator MOBILE_VIEW_TAB = new Locator(LocatorTypes.XPATH, "//span[@data-content='Mobile View']");
    private static final Locator TEST_LIST_DIV = new Locator(LocatorTypes.XPATH, "//div[@class='sc-eBHhsj jjwcCB']");
    private static final Locator MOST_SEVERE_ISSUES = new Locator(LocatorTypes.XPATH, "//span[@class='Text-sc-17v1xeu-0 sc-iGgWBj bcaJdd dTYYls']");
    private static final Locator WCAG_GUIDELINES = new Locator(LocatorTypes.XPATH, "//span[@class='Text-sc-17v1xeu-0 sc-iGgWBj bkikMT hbaebV']");
    private static final Locator ISSUES_TABLE = new Locator(LocatorTypes.XPATH, "//div[@class='sc-eBHhsj bojVGS']");
    private static final Locator IMAGE_CANVAS = new Locator(LocatorTypes.XPATH, "//canvas");

    String prod_manual_accessibility_dashboard_url = "https://accessibility.lambdatest.com/";
    String stage_manual_accessibility_dashboard_url = "https://stage-accessibility.lambdatestinternal.com/";


    public void iOpenManualDashboard() {
        if (EnvSetup.TEST_ENV.equalsIgnoreCase("prod")) {
            driver.getURL(prod_manual_accessibility_dashboard_url);
        } else
            driver.getURL(stage_manual_accessibility_dashboard_url);

        if (driver.waitForElementToBeVisible(SCANNED_REPORT_HEADING, 10).isDisplayed())
            ltLogger.info("Accessibility Manual Dashboard Opened");

        driver.click(APP_TAB);
        driver.waitForElementToBeVisible(TEST_LIST_DIV, 10);
    }

    public void iSearchForTheTest() {
        List<WebElement> testElements = driver.findElements(TEST_LIST);
        String expectedTestName = ManualAccessibilitySessionPage.TestName;
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
        if (!found)
            ltLogger.warn("Test id {} not found in the list", expectedTestName);
    }

    public void iValidateAccessibilityReport() {
        if (driver.waitForElementToBeVisible(MOST_SEVERE_ISSUES, 5).isDisplayed() && driver.waitForElementToBeVisible(WCAG_GUIDELINES, 5).isDisplayed())
            ltLogger.info("Issues, WCAG Guidelines & Views table are visible");
    }

    public void iValidateAllIssuesTab() {
        driver.click(ALL_ISSUES_TAB, 2);
        if (driver.waitForElementToBeVisible(ISSUES_TABLE, 2).isDisplayed())
            ltLogger.info("Issues table are visible with the issues");
    }

    public void iValidateMobileView() {
        driver.click(MOBILE_VIEW_TAB);
        if (driver.waitForElementToBeVisible(IMAGE_CANVAS, 5).isDisplayed())
            ltLogger.info("Image is visible in the Mobile View");
    }
}
