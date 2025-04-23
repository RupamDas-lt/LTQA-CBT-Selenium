package Pages;

import TestManagers.DriverManager;
import factory.Locator;
import factory.LocatorTypes;
import utility.BaseClass;
import utility.EnvSetup;

import static utility.FrameworkConstants.HTTPS;

public class LTDashboardCommonActions extends BaseClass {
  DriverManager driver;

  private String testDashboardHomePage = HTTPS + EnvSetup.TEST_DASHBOARD_URL_BASE + "/test?selectedTab=home&testID=<TEST_ID>";

  Locator testPageLogsParentContainer = new Locator(LocatorTypes.CSS, "div[class*='AutomationLogsPage_mainDiv']");

  public LTDashboardCommonActions(String testId, DriverManager driverManager) {
    testDashboardHomePage = testDashboardHomePage.replace("<TEST_ID>", testId);
    driver = driverManager;
  }

  public boolean navigateToHomePageOfSpecificTest() {
    driver.getURL(testDashboardHomePage);
    return driver.isDisplayed(testPageLogsParentContainer, 5);
  }
}
