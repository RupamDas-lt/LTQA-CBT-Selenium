package Pages;

import TestManagers.DriverManager;
import factory.Locator;
import factory.LocatorTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.CustomSoftAssert;

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
      softAssert.fail("Test performance report is not generated.");
      return;
    }
    driver.click(performanceJsonReportOptionTab);
    softAssert.assertTrue(driver.isDisplayed(performanceReport), "Test performance report is not displayed in the UI.");
  }

}
