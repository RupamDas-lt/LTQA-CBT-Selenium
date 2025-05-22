package Pages;

import TestManagers.DriverManager;
import factory.Locator;
import factory.LocatorTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebElement;
import utility.CustomSoftAssert;
import utility.FrameworkConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;

import static factory.SoftAssertionMessages.*;
import static utility.EnvSetup.TEST_VERIFICATION_DATA;

public class TestNetworkLogsPage extends LTDashboardCommonActions {

  DriverManager driver;
  CustomSoftAssert softAssert;

  private final Logger ltLogger = LogManager.getLogger(TestNetworkLogsPage.class);

  public TestNetworkLogsPage(String testId, DriverManager driverManager, CustomSoftAssert softAssert) {
    super(testId, driverManager);
    driver = driverManager;
    this.softAssert = softAssert;
  }

  private static final Locator networkLogsTab = new Locator(LocatorTypes.ID, "test-detail-network-tab");
  private static final Locator networkLogsRows = new Locator(LocatorTypes.CSS, "tr[id*='network-log-row']");
  private static final Locator networkLogsNotFoundMessage = new Locator(LocatorTypes.CSS,
    "div[class*='emptyContainer']");
  private static final Locator networkLogsSearchInput = new Locator(LocatorTypes.CSS,
    "input[aria-label='Search Network Logs']");
  private static final Locator networkLogsRowName = new Locator(LocatorTypes.CSS,
    "tr[id*='network-log-row']>td[class*='NetworkTableHeader-styles__filename'] span");

  public boolean openNetworkLogsTab() {
    navigateToHomePageOfSpecificTest();
    waitForTime(10);
    driver.click(networkLogsTab);
    return driver.isDisplayed(networkLogsRows, 5);
  }

  public void verifyAllExpectedNetworkCallsArePresentInTheUI() {
    Queue<String> urls = (Queue<String>) TEST_VERIFICATION_DATA.get()
      .get(FrameworkConstants.testVerificationDataKeys.URL);

    ltLogger.info("urls: {}", urls);

    if (urls.isEmpty()) {
      ltLogger.info("Verification data is invalid - URLs are empty");
      softAssert.fail(
        softAssert.softAssertMessageFormat(NETWORK_LOGS_VERIFICATION_DATA_NOT_VALID_CLIENT_ERROR_MESSAGE));
      return;
    }

    List<String> notFoundUrls = searchForItemsAndCollectMissing(urls);

    softAssert.assertTrue(notFoundUrls.isEmpty(),
      softAssert.softAssertMessageFormat(URLS_NOT_PRESENT_IN_NETWORK_LOGS_CLIENT_ERROR_MESSAGE, notFoundUrls));
  }

  private List<String> searchForItemsAndCollectMissing(Collection<String> items) {
    List<String> notFoundItems = new ArrayList<>();

    for (String item : items) {
      driver.clearTextUsingKeyboardForWindows(networkLogsSearchInput);
      performNetworkLogsSearch(item);

      if (isItemNotFoundInNetworkLogsResults(item)) {
        ltLogger.info("Item {} not found in search results of network logs", item);
        notFoundItems.add(item);
      }
    }

    return notFoundItems;
  }

  private void performNetworkLogsSearch(String searchTerm) {
    ltLogger.info("Performing command search for network logs: {}", searchTerm);
    driver.sendKeys(networkLogsSearchInput, searchTerm);
  }

  private boolean isItemNotFoundInNetworkLogsResults(String item) {
    if (driver.isDisplayed(networkLogsNotFoundMessage, 5)) {
      ltLogger.info("No Network logs found message displayed for command {}", item);
      return true;
    }

    List<WebElement> networkLogs = driver.findElements(networkLogsRowName);
    for (WebElement networkLog : networkLogs) {
      if (item.contains(networkLog.getText())) {
        ltLogger.info("Found Network log {} in search result.", item);
        return false;
      }
    }
    return true;
  }

  public void downloadNetworkLogsFromUI(String expectedFileName) {
    boolean isNetworkLogsDownloaded = downloadLogFile(expectedFileName, "Network", 10);
    softAssert.assertTrue(isNetworkLogsDownloaded,
      softAssert.softAssertMessageFormat(UNABLE_TO_DOWNLOAD_NETWORK_LOGS_CLIENT_ERROR_MESSAGE));
  }

  public void openNetworkLogsInNewTabAndVerify() {
    String errorMessage = openLogsInNewTabAndVerify(softAssert, "network", networkLogsRows, 5);
    softAssert.assertTrue(errorMessage.isEmpty(), errorMessage);
  }
}