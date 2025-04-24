package Pages;

import TestManagers.DriverManager;
import automationHelper.LTHooks;
import factory.Locator;
import factory.LocatorTypes;
import utility.BaseClass;
import utility.EnvSetup;

import static utility.FrameworkConstants.HTTPS;

public class LTDashboardCommonActions extends BaseClass {
  DriverManager driver;

  private String testDashboardHomePage = HTTPS + EnvSetup.TEST_DASHBOARD_URL_BASE + "/test?selectedTab=home&testID=<TEST_ID>";

  Locator testPageLogsParentContainer = new Locator(LocatorTypes.CSS, "div[class*='AutomationLogsPage_mainDiv']");
  Locator testIDCopyButton = new Locator(LocatorTypes.ID, "testIdCopyButton");

  public LTDashboardCommonActions(String testId, DriverManager driverManager) {
    testDashboardHomePage = testDashboardHomePage.replace("<TEST_ID>", testId);
    driver = driverManager;
  }

  public boolean navigateToHomePageOfSpecificTest() {
    driver.getURL(testDashboardHomePage);
    return driver.isDisplayed(testPageLogsParentContainer, 5);
  }

  public String getTestIDFromTestDashboard() {
    driver.click(testIDCopyButton);
    waitForTime(2);
    return LTHooks.getClipboard(driver);
  }
}
