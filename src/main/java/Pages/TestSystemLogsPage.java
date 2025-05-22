package Pages;

import TestManagers.DriverManager;
import factory.Locator;
import factory.LocatorTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.CustomSoftAssert;

import static factory.SoftAssertionMessages.*;

public class TestSystemLogsPage extends LTDashboardCommonActions {

  private final Logger ltLogger = LogManager.getLogger(TestSystemLogsPage.class);

  DriverManager driver;
  CustomSoftAssert softAssert;
  private static final int expectedMinimumSizeOfSystemLogs = 300;

  private static final Locator allLogsTab = new Locator(LocatorTypes.ID, "test-detail-logs-tab");
  private static final Locator systemLogsTab = new Locator(LocatorTypes.CSS, "li[id='lt-test-detail-logs-selenium']");
  private static final Locator systemLogsContainerParent = new Locator(LocatorTypes.CSS,
    "div[id='test-detail-logs-selenium']");
  private static final Locator systemLogsContainer = new Locator(LocatorTypes.CSS, "div[aria-label='selenium logs']");
  private static final Locator systemLogsNotFoundContainer = new Locator(LocatorTypes.CSS,
    "#test-detail-logs-selenium div[class*='ComponentNoDataFound_message']");
  private static final Locator systemLogRowLocator = new Locator(LocatorTypes.CSS,
    "div[class*='innerScrollContainer']>div");
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
    boolean isSystemLogsDownloaded = downloadLogFile(expectedFileName, "Selenium");
    softAssert.assertTrue(isSystemLogsDownloaded,
      softAssert.softAssertMessageFormat(UNABLE_TO_DOWNLOAD_SYSTEM_LOGS_CLIENT_ERROR_MESSAGE));
  }

  public void openAndVerifySystemLogsInNewTab() {
    String errorMessage = openLogsInNewTabAndVerify(softAssert, "selenium", systemLogRowLocator, 5);
    softAssert.assertTrue(errorMessage.isEmpty(), errorMessage);
  }

  public void verifySystemLogs() {
    if (seleniumLogsNotFoundMessageDisplayed()) {
      ltLogger.info("System Logs are found");
      softAssert.fail(softAssert.softAssertMessageFormat(SYSTEM_LOGS_NOT_GENERATED_CLIENT_ERROR_MESSAGE));
      return;
    }
    String systemLogs = extractSystemLogs();
    softAssert.assertTrue(systemLogs.length() > expectedMinimumSizeOfSystemLogs,
      softAssert.softAssertMessageFormat(SYSTEM_LOGS_SIZE_IS_LESSER_CLIENT_ERROR_MESSAGE,
        expectedMinimumSizeOfSystemLogs));
  }
}
