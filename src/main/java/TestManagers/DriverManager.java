package TestManagers;

import factory.BrowserType;
import factory.Locator;
import factory.LocatorTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utility.BaseClass;
import utility.EnvSetup;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;

import static utility.EnvSetup.*;
import static utility.FrameworkConstants.HTTPS;
import static utility.FrameworkConstants.SESSION_ID;

public class DriverManager extends BaseClass {
  private static final EnumMap<LocatorTypes, Function<String, By>> LOCATOR_MAP = new EnumMap<>(LocatorTypes.class);

  static {
    LOCATOR_MAP.put(LocatorTypes.CSS, By::cssSelector);
    LOCATOR_MAP.put(LocatorTypes.XPATH, By::xpath);
    LOCATOR_MAP.put(LocatorTypes.ID, By::id);
    LOCATOR_MAP.put(LocatorTypes.NAME, By::name);
    LOCATOR_MAP.put(LocatorTypes.CLASS_NAME, By::className);
    LOCATOR_MAP.put(LocatorTypes.TAG_NAME, By::tagName);
    LOCATOR_MAP.put(LocatorTypes.LINK_TEXT, By::linkText);
    LOCATOR_MAP.put(LocatorTypes.PARTIAL_LINK_TEXT, By::partialLinkText);
  }

  private final Logger ltLogger = LogManager.getLogger(DriverManager.class);
  RemoteWebDriver driver;
  MutableCapabilities capabilities;
  String gridUrl;

  private static By toBy(Locator locator) {
    putValueToVerificationData("locators", locator.value());
    return LOCATOR_MAP.get(locator.type()).apply(locator.value());
  }

  private static void putValueToVerificationData(String key, String value) {
    Map<String, Object> verificationData = TEST_VERIFICATION_DATA.get();
    Set<String> set = (Set<String>) verificationData.computeIfAbsent(key, k -> new HashSet<String>());
    set.add(value);
  }

  public WebElement waitForElementToBeVisible(Locator locator, int timeout) {
    ltLogger.info("Waiting for element via, using ['{}', '{}']", locator.type(), locator.value());
    Duration setImplicitWait;
    try {
      setImplicitWait = driver.manage().timeouts().getImplicitWaitTimeout();
    } catch (Exception ignore) {
      setImplicitWait = Duration.ofSeconds(10);
    }

    driver.manage().timeouts().implicitlyWait(Duration.ofMillis(0));

    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
      By byLocator = toBy(locator);
      return wait.until(ExpectedConditions.visibilityOfElementLocated(byLocator));
    } finally {
      driver.manage().timeouts().implicitlyWait(setImplicitWait);
    }
  }

  public void createTestDriver() {
    capabilities = EnvSetup.TEST_CAPS.get();
    ltLogger.info("Test caps used passed by user: {}", capabilities.asMap().toString());
    if (TEST_ENV.equals("local"))
      createLocalTestDriver();
    else
      createRemoteTestDriver();
    testDriver.set(driver);
  }

  private void createLocalTestDriver() {
    String browserName = (String) EnvSetup.TEST_CAPS_MAP.get().get("browserName");
    BrowserType browserType = BrowserType.valueOf(browserName.toUpperCase());
    ltLogger.info("Creating local driver for browser {}, with caps: {}", browserType, capabilities.asMap());
    switch (browserType) {
    case FIREFOX:
      driver = new FirefoxDriver((FirefoxOptions) capabilities);
      return;
    case CHROME:
      driver = new ChromeDriver((ChromeOptions) capabilities);
      return;
    case EDGE:
      driver = new EdgeDriver((EdgeOptions) capabilities);
      return;
    case SAFARI:
      driver = new SafariDriver((SafariOptions) capabilities);
      return;
    default:
      throw new IllegalArgumentException("Unsupported browser: " + browserName);
    }
  }

  private String getGridUrl() {
    return HTTPS + testUserName.get() + ":" + testAccessKey.get() + "@" + testGridUrl.get() + "/wd/hub";
  }

  private void createRemoteTestDriver() {
    gridUrl = getGridUrl();
    ltLogger.info("Creating remote driver with remote grid url: {}", gridUrl);
    try {
      driver = new RemoteWebDriver(URI.create(gridUrl).toURL(), capabilities);
      TEST_SESSION_ID.set(driver.getSessionId().toString());
      EnvSetup.TEST_REPORT.get().put(SESSION_ID, TEST_SESSION_ID.get());
      ltLogger.info("Remote driver created. Test session ID: {}", TEST_SESSION_ID.get());
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public void getURL(String url) {
    ltLogger.info("Opening URL: {}", url);
    driver.get(url);
    putValueToVerificationData("url", url);
  }

  public void quit() {
    driver.quit();
  }

  public WebElement findElement(Locator locator) {
    ltLogger.info("Finding element with locator: {}", locator.toString());
    return driver.findElement(toBy(locator));
  }

  public String getText(Locator locator, int... timeout) {
    ltLogger.info("Finding text with locator: {}", locator.toString());
    int waitTime = Optional.ofNullable(timeout).filter(t -> t.length > 0).map(t -> t[0]).orElse(0);
    try {
      WebElement element = (waitTime > 0) ?
        waitForElementToBeVisible(locator, waitTime) :
        driver.findElement(toBy(locator));
      String text = element.getText();
      ltLogger.info("Found text from element: {}", text);
      return text;
    } catch (Exception e) {
      ltLogger.error("Failed to get text from element with locator: {}. Error: {}", locator.toString(), e.getMessage());
      throw new RuntimeException("Unable to get text from locator: " + locator, e);
    }
  }

  public boolean isDisplayed(Locator locator, int... timeout) {
    ltLogger.info("Finding if element is displayed with locator: {}", locator.toString());
    int waitTime = Optional.ofNullable(timeout).filter(t -> t.length > 0).map(t -> t[0]).orElse(0);
    try {
      WebElement element = (waitTime > 0) ?
        waitForElementToBeVisible(locator, waitTime) :
        driver.findElement(toBy(locator));
      return element.isDisplayed();
    } catch (Exception exception) {
      ltLogger.error("Unable to find element with locator: {}. Exception occurred: {}", locator.toString(),
        exception.getMessage());
      return false;
    }
  }

  public Set<Cookie> getCookies() {
    Set<Cookie> cookies = driver.manage().getCookies();
    ltLogger.info("Retrieved {} cookies: {}", cookies.size(), cookies);
    return cookies;
  }

  public Set<String> getCookieNames() {
    Set<Cookie> cookies = driver.manage().getCookies();
    Set<String> cookieNames = new HashSet<>();
    for (Cookie cookie : cookies) {
      cookieNames.add(cookie.getName());
    }
    ltLogger.info("Found cookies names: {}", cookieNames);
    return cookieNames;
  }

  public void executeScript(String script) {
    executeScriptAndFetchValue(script);
  }

  public Object executeScriptAndFetchValue(String script) {
    putValueToVerificationData("javaScripts", script);
    try {
      Object response = driver.executeScript(script);
      ltLogger.info("JS Script executed successfully. Script: {}", script);
      ltLogger.info("JS Script response: {}", response == null ? "null" : response.toString());
      return response;
    } catch (Exception e) {
      ltLogger.error("JS Script execution failed. Script: {}", script);
      return null;
    }
  }

  public String openUrlAndGetLocatorText(String url, Locator locator, int timeout) {
    getURL(url);
    String text = null;
    if (isDisplayed(locator, timeout)) {
      text = getText(locator);
      ltLogger.info("Retrieved text from website: {}, with locator: {}, is {}", url, locator.toString(), text);
    }
    ltLogger.error("Unable to find text from website: {}, with locator: {}, is {}", url, locator.toString(), text);
    return text;
  }

  public void sendKeys(Locator locator, CharSequence keys) {
    ltLogger.info("Sending {} keys to element using ['{}', '{}']", keys, locator.type(), locator.value());
    WebElement element = waitForElementToBeVisible(locator, 5);
    if (element != null) {
      try {
        element.sendKeys(keys);
        ltLogger.info("Successfully sent keys '{}' to the element.", keys);
      } catch (Exception e) {
        ltLogger.error("Error sending keys to element '{}': {}", locator.value(), e.getMessage());
      }
    } else {
      ltLogger.error("Element with locator ['{}', '{}'] is not visible after the timeout.", locator.type(),
        locator.value());
      throw new RuntimeException(
        "Send keys failed. Element with locator " + locator + " is not visible after the timeout.");
    }
  }

  public void switchToTab(int tabIndex) {
    try {
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      wait.until(d -> d.getWindowHandles().size() > tabIndex);
      ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
      driver.switchTo().window(tabs.get(tabIndex));
      ltLogger.info("Successfully switched to tab {}", tabIndex);
    } catch (Exception e) {
      ltLogger.error("Failed to switch to tab {}. Error: {}", tabIndex, e.getMessage());
      throw new RuntimeException("Failed to switch tabs", e);
    }
  }

  public String getCurrentURL() {
    return driver.getCurrentUrl();
  }

  public void click(Locator locator, int... timeout) {
    ltLogger.info("Clicking element with locator: {}", locator.toString());
    if (timeout != null && timeout.length > 0)
      waitForElementToBeVisible(locator, timeout[0]).click();
    else
      driver.findElement(toBy(locator)).click();
  }
}