package Pages.Accessibility;

import TestManagers.DriverManager;
import factory.Locator;
import factory.LocatorTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;
import utility.BaseClass;
import utility.CustomSoftAssert;
import utility.EnvSetup;

import java.util.List;

import static factory.SoftAssertionMessagesAccessibility.ERROR_IN_OPENING_PAGE;
import static factory.SoftAssertionMessagesAccessibility.REPORT_NOT_VISIBLE;

public class ManualAccessibilityDashboardPage extends BaseClass {

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

    public static final ThreadLocal<String> expectedTestName = new ThreadLocal<>();
    public static final ThreadLocal<String> actualText = new ThreadLocal<>();


    private static final String manualDashboardUrl = EnvSetup.TEST_ENV.toLowerCase().contains("stage") ?
            "https://stage-accessibility.lambdatestinternal.com/" : "https://accessibility.lambdatest.com/";


    public void iOpenManualDashboard() {
        try {
            driver.getURL(manualDashboardUrl);
            softAssert.assertTrue(driver.isDisplayed(scannedReportHeading, 10), softAssertMessageFormat(ERROR_IN_OPENING_PAGE, "Manual Accessibility Dashboard"));
            driver.click(appTab);
            driver.waitForElementToBeVisible(testListDiv, 10);
        } catch (Exception e) {
            throw new RuntimeException("Unable to navigate to Accessibility Manual Dashboard Page");
        }
    }

    public void iSearchForTheTest() {
        List<WebElement> testElements = driver.findElements(testList);
        expectedTestName.set(ManualAccessibilitySessionPage.testName.get());
        boolean found = false;

        for (WebElement testElement : testElements) {
            actualText.set(testElement.getText().replaceAll("[^0-9]", ""));
            if (actualText.get().equalsIgnoreCase(expectedTestName.get())) {
                ltLogger.info("Test id {} is found", expectedTestName);
                testElement.click();
                found = true;
                break;
            }
        }
        if (!found) {
            throw new RuntimeException("Test name not found in the Manual Accessibility Dashboard list");
        }
    }

    public void iValidateAccessibilityReport() {
        softAssert.assertTrue((driver.isDisplayed(mostSevereIssues, 5) && driver.isDisplayed(wcagGuidelines, 5)), softAssertMessageFormat(REPORT_NOT_VISIBLE, "Mobile Manual Accessibility", "report"));
    }

    public void iValidateAllIssuesTab() {
        driver.click(allIssuesTab, 2);
        softAssert.assertTrue(driver.isDisplayed(issueTable, 2), softAssertMessageFormat(REPORT_NOT_VISIBLE, "Mobile Manual Accessibility", "issues"));
    }

    public void iValidateMobileView() {
        driver.click(mobileViewTab);
        softAssert.assertTrue(driver.isDisplayed(imageCanvas, 5), softAssertMessageFormat(REPORT_NOT_VISIBLE, "Mobile Manual Accessibility", "screenshots"));
    }
}
