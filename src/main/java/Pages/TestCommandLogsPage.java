package Pages;

import TestManagers.DriverManager;
import factory.Locator;
import factory.LocatorTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.CustomSoftAssert;
import utility.FrameworkConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;

import static factory.SoftAssertionMessages.*;
import static utility.EnvSetup.TEST_VERIFICATION_DATA;

public class TestCommandLogsPage extends LTDashboardCommonActions {

  private final Logger ltLogger = LogManager.getLogger(TestCommandLogsPage.class);

  DriverManager driver;
  CustomSoftAssert softAssert;

  private static final Locator commandLogsTab = new Locator(LocatorTypes.ID, "test-detail-home-tab");
  private static final Locator commandTitleContainer = new Locator(LocatorTypes.CSS,
    "div[class*='indexV2_commandLogsContainer'] div[class*='commandLogTitleSection']");
  private static final Locator commandLogsCountHeading = new Locator(LocatorTypes.CSS,
    "div[class*='commandLogsContainer'] h4");
  private static final Locator commandLogsVirtualizedList = new Locator(LocatorTypes.CSS,
    "div[class*='commandLogsContainer'] div[class*='Virtualized__List']");
  private static final Locator commandLogsScrollableContainer = new Locator(LocatorTypes.CSS,
    "div[class*='commandLogsContainer'] div[class*='innerScrollContainer']");
  private static final Locator displayScreenshotForCommandButton = new Locator(LocatorTypes.CSS,
    "button[aria-label*='<COMMAND_NAME>");
  private static final Locator scrollToTopButton = new Locator(LocatorTypes.ID, "cmd-top-scroll-button");
  private static final Locator scrollToBottomButton = new Locator(LocatorTypes.ID, "cmd-bottom-scroll-button");
  private static final Locator commandLogsSearchBar = new Locator(LocatorTypes.NAME, "Search Command");
  private static final Locator commandLogsSearchBarClearButton = new Locator(LocatorTypes.CSS,
    "button[aria-label='Clear input']");
  private static final Locator commandPreviewValue = new Locator(LocatorTypes.XPATH,
    "//div[contains(@id,'previewValue')]//span");
  private static final Locator noCommandLogsFound = new Locator(LocatorTypes.CSS,
    "div[class*='ComponentNoDataFound_message']");

  public TestCommandLogsPage(String testId, DriverManager driverManager, CustomSoftAssert softAssert) {
    super(testId, driverManager);
    driver = driverManager;
    this.softAssert = softAssert;
  }

  public boolean openCommandLogsTab() {
    navigateToHomePageOfSpecificTest();
    waitForTime(10);
    driver.click(commandLogsTab);
    return driver.isDisplayed(commandTitleContainer, 5);
  }

  private void verifyCommandsCount() {
    String commandCountHeading = driver.getText(commandLogsCountHeading);
    int commandCount = (int) extractNumberFromString(commandCountHeading);
    softAssert.assertTrue(commandCount > 0,
      softAssertMessageFormat(COMMAND_LOGS_COUNT_INVALID_CLIENT_ERROR_MESSAGE, commandCount));
    ltLogger.info("command count: {}", commandCount);
  }

  private boolean verifyFirstAndLastCommandsAreDisplayed() {
    String firstCommandName = "New Session";
    String lastCommandName = "Delete Session (Close Browser)";
    boolean firstCommandDisplayed;
    boolean lastCommandDisplayed;
    Locator firstCommandLocator = new Locator(displayScreenshotForCommandButton.type(),
      displayScreenshotForCommandButton.value().replace("<COMMAND_NAME>", firstCommandName));
    Locator lastCommandLocator = new Locator(displayScreenshotForCommandButton.type(),
      displayScreenshotForCommandButton.value().replace("<COMMAND_NAME>", lastCommandName));
    firstCommandDisplayed = driver.isDisplayed(firstCommandLocator);
    if (!firstCommandDisplayed && driver.isDisplayed(scrollToTopButton)) {
      ltLogger.info("{} is not displayed by default. Trying scrolling to top.", firstCommandName);
      driver.click(scrollToTopButton);
    }
    softAssert.assertTrue(firstCommandDisplayed,
      softAssertMessageFormat(COMMAND_NOT_DISPLAYED_CLIENT_ERROR_MESSAGE, firstCommandName));
    if (driver.isDisplayed(scrollToBottomButton)) {
      ltLogger.info("Scrolling to bottom.");
      driver.click(scrollToBottomButton);
    }
    lastCommandDisplayed = driver.isDisplayed(lastCommandLocator, 20);
    softAssert.assertTrue(lastCommandDisplayed,
      softAssertMessageFormat(COMMAND_NOT_DISPLAYED_CLIENT_ERROR_MESSAGE, lastCommandName));
    return lastCommandDisplayed && firstCommandDisplayed;
  }

  private void verifyAllExpectedCommandsArePresentInTheUI() {
    Queue<String> urls = (Queue<String>) TEST_VERIFICATION_DATA.get()
      .get(FrameworkConstants.testVerificationDataKeys.URL);
    Queue<String> locators = (Queue<String>) TEST_VERIFICATION_DATA.get()
      .get(FrameworkConstants.testVerificationDataKeys.LOCATORS);

    ltLogger.info("urls: {}", urls);
    ltLogger.info("locators: {}", locators);

    if (urls.isEmpty() && locators.isEmpty()) {
      ltLogger.info("Verification data is invalid - both URLs and locators are empty");
      softAssert.fail(softAssertMessageFormat(VERIFICATION_DATA_FOR_COMMAND_LOGS_NOT_VALID_CLIENT_ERROR_MESSAGE));
      return;
    }

    List<String> notFoundUrls = searchForItemsAndCollectMissing(urls);
    List<String> notFoundLocators = searchForItemsAndCollectMissing(locators);

    assertAllItemsFound(notFoundUrls, notFoundLocators);
  }

  private List<String> searchForItemsAndCollectMissing(Collection<String> items) {
    List<String> notFoundItems = new ArrayList<>();

    for (String item : items) {
      clearSearchBarIfNeeded();
      performCommandLogsSearch(item);

      if (isItemNotFoundInCommandLogsResults(item)) {
        ltLogger.info("Item {} not found in search results", item);
        notFoundItems.add(item);
      }
    }

    return notFoundItems;
  }

  private void clearSearchBarIfNeeded() {
    if (driver.isDisplayed(commandLogsSearchBarClearButton)) {
      ltLogger.info("Clearing search bar");
      driver.click(commandLogsSearchBarClearButton);
    }
  }

  private void performCommandLogsSearch(String searchTerm) {
    ltLogger.info("Performing command search command logs: {}", searchTerm);
    driver.sendKeys(commandLogsSearchBar, searchTerm);
  }

  private boolean isItemNotFoundInCommandLogsResults(String item) {
    if (driver.isDisplayed(noCommandLogsFound, 5)) {
      ltLogger.info("No command logs found message displayed for command {}", item);
      return true;
    }

    String searchResultCommandValue = driver.findElements(commandPreviewValue).getFirst().getText();
    ltLogger.info("Command value for first command {} with search term: {}", searchResultCommandValue, item);

    return !searchResultCommandValue.contains(item);
  }

  private void assertAllItemsFound(List<String> notFoundUrls, List<String> notFoundLocators) {
    softAssert.assertTrue(notFoundUrls.isEmpty(),
      softAssertMessageFormat(DATA_NOT_PRESENT_IN_COMMAND_LOGS_CLIENT_ERROR_MESSAGE_1, notFoundUrls));
    softAssert.assertTrue(notFoundLocators.isEmpty(),
      softAssertMessageFormat(DATA_NOT_PRESENT_IN_COMMAND_LOGS_CLIENT_ERROR_MESSAGE_2, notFoundLocators));
  }

  public void verifyCommandLogs() {
    verifyCommandsCount();
    boolean firstAndLastCommandsArePresent = verifyFirstAndLastCommandsAreDisplayed();
    if (firstAndLastCommandsArePresent) {
      verifyAllExpectedCommandsArePresentInTheUI();
    } else {
      softAssert.fail(softAssertMessageFormat(FIRST_AND_LAST_COMMANDS_MISSING_IN_COMMAND_LOGS_CLIENT_ERROR_MESSAGE));
    }
  }

}
