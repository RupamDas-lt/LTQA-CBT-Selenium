package Pages.Selenium;

import TestManagers.DriverManager;
import factory.Locator;
import factory.LocatorTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.CustomSoftAssert;

import static factory.SoftAssertionMessages.PERFORMANCE_REPORT_NOT_GENERATED_CLIENT_ERROR_MESSAGE;
import static factory.SoftAssertionMessages.PERFORMANCE_REPORT_NOT_PRESENT_IN_UI_CLIENT_ERROR_MESSAGE;

public class TestPerformanceReportPage extends LTDashboardCommonActions {

    private final Logger ltLogger = LogManager.getLogger(TestPerformanceReportPage.class);

    DriverManager driver;
    CustomSoftAssert softAssert;

    private static final Locator performanceReportTab = new Locator(LocatorTypes.ID, "test-detail-performance-tab");
    private static final Locator performanceJsonReportParentContainer = new Locator(LocatorTypes.ID,
            "test-detail-performance");
    private static final Locator performanceJsonReportOptionTab = new Locator(LocatorTypes.ID,
            "lt-test-detail-performance-lighthouse.report.json");
    private static final Locator performanceReport = new Locator(LocatorTypes.ID, "lighthouse_wrapper");
    private static final Locator performanceReportNotGeneratedMessage = new Locator(LocatorTypes.CSS,
            "#test-detail-performance>div[class*='ErrorScreen']");

    public TestPerformanceReportPage(String testId, DriverManager driverManager, CustomSoftAssert softAssert) {
        super(testId, driverManager);
        driver = driverManager;
        this.softAssert = softAssert;
    }

    public boolean openPerformanceReportTab() {
        navigateToHomePageOfSpecificTest();
        waitForTime(10);
        if (driver.isDisplayed(performanceReportTab)) {
            driver.click(performanceReportTab);
            return driver.isDisplayed(performanceJsonReportParentContainer, 5);
        }
        return false;
    }

    public void isPerformanceReportDisplayed() {
        if (driver.isDisplayed(performanceReportNotGeneratedMessage)) {
            softAssert.fail(softAssertMessageFormat(PERFORMANCE_REPORT_NOT_GENERATED_CLIENT_ERROR_MESSAGE));
            return;
        }
        driver.click(performanceJsonReportOptionTab);
        softAssert.assertTrue(driver.isDisplayed(performanceReport),
                softAssertMessageFormat(PERFORMANCE_REPORT_NOT_PRESENT_IN_UI_CLIENT_ERROR_MESSAGE));
    }

}
