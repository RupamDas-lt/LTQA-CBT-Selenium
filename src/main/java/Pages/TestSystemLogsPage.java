package Pages;

import TestManagers.DriverManager;
import factory.Locator;
import factory.LocatorTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.CustomSoftAssert;

public class TestSystemLogsPage extends LTDashboardCommonActions {

  private final Logger ltLogger = LogManager.getLogger(TestSystemLogsPage.class);

  DriverManager driver;
  CustomSoftAssert softAssert;

  private static final Locator allLogsTab = new Locator(LocatorTypes.ID, "test-detail-logs-tab");
  private static final Locator systemLogsTab = new Locator(LocatorTypes.CSS, "li[id='lt-test-detail-logs-selenium']");
  private static final Locator systemLogsContainer = new Locator(LocatorTypes.CSS, "div[aria-label='selenium logs']");

  public TestSystemLogsPage(String testId, DriverManager driverManager, CustomSoftAssert softAssert) {
    super(testId, driverManager);
    driver = driverManager;
    this.softAssert = softAssert;
  }

  public boolean openCommandLogsTab() {
    navigateToHomePageOfSpecificTest();
    waitForTime(10);
    driver.click(allLogsTab);
    driver.click(systemLogsTab, 2);
    return driver.isDisplayed(systemLogsContainer, 5);
  }
}
