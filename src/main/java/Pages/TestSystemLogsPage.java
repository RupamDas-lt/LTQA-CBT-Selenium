package Pages;

import TestManagers.DriverManager;
import automationHelper.LTHooks;
import factory.Locator;
import factory.LocatorTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.CustomSoftAssert;

public class TestSystemLogsPage extends LTDashboardCommonActions {

  private final Logger ltLogger = LogManager.getLogger(TestSystemLogsPage.class);

  DriverManager driver;
  CustomSoftAssert softAssert;
  private static final int expectedMinimumSizeOfSystemLogs = 300;

  private static final Locator allLogsTab = new Locator(LocatorTypes.ID, "test-detail-logs-tab");
  private static final Locator systemLogsTab = new Locator(LocatorTypes.CSS, "li[id='lt-test-detail-logs-selenium']");
  private static final Locator systemLogsContainerParent = new Locator(LocatorTypes.ID, "test-detail-logs-selenium");
  private static final Locator systemLogsContainer = new Locator(LocatorTypes.CSS, "div[aria-label='selenium logs']");
  private static final Locator systemLogsNotFoundContainer = new Locator(LocatorTypes.CSS,
    "#test-detail-logs-selenium div[class*='ComponentNoDataFound_message']");
  private static final Locator systemLogRowLocator = new Locator(LocatorTypes.CSS,
    "div[class*='innerScrollContainer']>div");
  private static final Locator systemLogsDownloadButton = new Locator(LocatorTypes.CSS,
    "button[aria-label='Download Logs']");
  private static final Locator systemLogsFullScreenViewButton = new Locator(LocatorTypes.CSS,
    "button[aria-label='Open in a new tab']");

  public TestSystemLogsPage(String testId, DriverManager driverManager, CustomSoftAssert softAssert) {
    super(testId, driverManager);
    driver = driverManager;
    this.softAssert = softAssert;
  }

  public boolean openSystemLogsTab() {
    navigateToHomePageOfSpecificTest();
    waitForTime(10);
    driver.click(allLogsTab);
    driver.click(systemLogsTab, 2);
    return driver.isDisplayed(systemLogsContainerParent, 5);
  }

  private boolean seleniumLogsNotFoundMessageDisplayed() {
    return driver.isDisplayed(systemLogsNotFoundContainer, 5);
  }

  private String extractSystemLogs() {
    String systemLogs = driver.getText(systemLogsContainer);
    ltLogger.info("SystemLogs container text: {}", systemLogs);
    return systemLogs;
  }

  public void downloadSystemLogsFromUI(String expectedFileName) {
    ltLogger.info("Downloading System logs from UI");
    driver.click(systemLogsDownloadButton);
    waitForTime(5);
    boolean isSystemLogsDownloaded = LTHooks.isFileExist(driver, expectedFileName, 5);
    softAssert.assertTrue(isSystemLogsDownloaded, "Unable to download system logs from UI");
  }

  public void openAndVerifySystemLogsInNewTab() {
    ltLogger.info("Verifying system logs in new tab");
    driver.click(systemLogsFullScreenViewButton, 2);
    waitForTime(2);
    try {
      driver.switchToTab(1);
    } catch (Exception e) {
      ltLogger.error("Unable to open system logs in new tab. Error: {}", e.getMessage());
      softAssert.fail("Unable to open And verify system logs in New Tab");
      return;
    }
    softAssert.assertTrue(driver.isDisplayed(systemLogRowLocator), "System logs are not displayed in new tab.");
    driver.closeCurrentTabAndSwitchContextToLastTab();
  }

  public void verifySystemLogs() {
    if (seleniumLogsNotFoundMessageDisplayed()) {
      ltLogger.info("System Logs are found");
      softAssert.fail("System logs are not generated.");
      return;
    }
    String systemLogs = extractSystemLogs();
    softAssert.assertTrue(systemLogs.length() > expectedMinimumSizeOfSystemLogs,
      String.format("System logs size is lesser than expected %s.", expectedMinimumSizeOfSystemLogs));
  }
}
