package TestManagers;

import factory.BrowserType;
import factory.Locator;
import factory.LocatorTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.http.ClientConfig;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import utility.BaseClass;
import utility.EnvSetup;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.*;
import java.util.function.Function;

import static utility.EnvSetup.*;
import static utility.FrameworkConstants.*;

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
    private boolean putDriverActionsToTestVerificationData = false;

    public DriverManager() {
    }

    public DriverManager(boolean putDriverActionsToTestVerificationData) {
        this.putDriverActionsToTestVerificationData = putDriverActionsToTestVerificationData;
    }

    private By toBy(Locator locator) {
        putValueToVerificationData(testVerificationDataKeys.LOCATORS, locator.value());
        return LOCATOR_MAP.get(locator.type()).apply(locator.value());
    }

    @SuppressWarnings("unchecked")
    private void putValueToVerificationData(testVerificationDataKeys key, String value) {
        if (!putDriverActionsToTestVerificationData) {
            return;
        }
        Map<testVerificationDataKeys, Object> verificationData = TEST_VERIFICATION_DATA.get();
        Queue<String> queue;
        if (verificationData.get(key) == null) {
            queue = new LinkedList<>();
            verificationData.put(key, queue);
        }
        ((Queue<String>) verificationData.get(key)).add(value);
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

    public boolean waitForElementToDisappear(Locator locator, int timeout) {
        ltLogger.info("Waiting for element to disappear via, using ['{}', '{}']", locator.type(), locator.value());
        Duration setImplicitWait;
        try {
            setImplicitWait = driver.manage().timeouts().getImplicitWaitTimeout();
        } catch (Exception ignore) {
            setImplicitWait = Duration.ofSeconds(10);
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(0));

        boolean isElementGone;
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
            By byLocator = toBy(locator);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(byLocator));
            isElementGone = true;
        } catch (Exception e) {
            ltLogger.error("Element with locator ['{}', '{}'] did not disappear within {} seconds. Error: {}",
                    locator.type(), locator.value(), timeout, e.getMessage());
            isElementGone = false;
        } finally {
            driver.manage().timeouts().implicitlyWait(setImplicitWait);
        }
        return isElementGone;
    }

    public boolean waitForExactText(Locator locator, String expectedText, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        By byLocator = toBy(locator);
        return wait.until(driver -> {
            try {
                String actualText = driver.findElement(byLocator).getText();
                return expectedText.equals(actualText);
            } catch (NoSuchElementException e) {
                return false;
            }
        });
    }

    public void createTestDriver() {
        capabilities = EnvSetup.TEST_CAPS.get();
        ltLogger.info("Test caps used passed by user: {}", capabilities.asMap().toString());
        if (TEST_ENV.equals("local"))
            createLocalTestDriver();
        else
            createRemoteTestDriver("test");
        testDriver.set(driver);
    }

    public void createClientDriver() {
        capabilities = EnvSetup.CLIENT_TEST_CAPS.get();
        ltLogger.info("Client caps used passed by user: {}", capabilities.asMap().toString());
        if (TEST_ENV.equals("local"))
            createLocalTestDriver();
        else
            createRemoteTestDriver("client");
        clientDriver.set(driver);
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

    private String getGridUrl(String purpose) {
        if (purpose.equals("client")) {
            return HTTPS + clientTestUserName.get() + ":" + clientTestAccessKey.get() + "@" + clientTestGridUrl.get() + "/wd/hub";
        }
        return HTTPS + testUserName.get() + ":" + testAccessKey.get() + "@" + testGridUrl.get() + "/wd/hub";
    }

    private void createRemoteTestDriver(String purpose) {
        ThreadLocal<String> sessionId = purpose.equals("client") ? CLIENT_SESSION_ID : TEST_SESSION_ID;
        String sessionIdKey = purpose.equals("client") ? SESSION_ID_CLIENT : SESSION_ID;
        Queue<String> sessionIdQueue = purpose.equals("client") ?
                CLIENT_TEST_SESSION_ID_QUEUE.get() :
                TEST_SESSION_ID_QUEUE.get();
        gridUrl = getGridUrl(purpose);
        ltLogger.info("Creating remote driver with remote grid url: {}", gridUrl);
        try {
            ClientConfig clientConfig = ClientConfig.defaultConfig().connectionTimeout(Duration.ofMinutes(20))
                    .readTimeout(Duration.ofMinutes(20));
            driver = (RemoteWebDriver) RemoteWebDriver.builder().oneOf(capabilities).address(gridUrl).config(clientConfig)
                    .build();
            sessionId.set(driver.getSessionId().toString());
            EnvSetup.TEST_REPORT.get().put(sessionIdKey, sessionId.get());
            ltLogger.info("Remote driver created. Test session ID: {}", sessionId.get());
            sessionIdQueue.add(sessionId.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void getURL(String url) {
        ltLogger.info("Opening URL: {}", url);
        driver.get(url);
        putValueToVerificationData(testVerificationDataKeys.URL, url);
    }

    public void getURLInNewTab(String url) {
        ltLogger.info("Opening URL in new tab: {}", url);
        String script = "window.open('" + url + "', '_blank');";
        executeScript(script);
        ltLogger.info("New tab opened with URL: {}", url);
    }

    ///  Use this method to open URL using JavaScript, this wouldn't block the driver while navigating to the URL.
    public void getURLUsingJs(String url) {
        ltLogger.info("Opening URL using JS: {}", url);
        executeScript(String.format(jsToNavigateToUrl, url));
    }

    public void quit() {
        driver.quit();
    }

    public WebElement findElement(Locator locator) {
        ltLogger.info("Finding element with locator: {}", locator.toString());
        return driver.findElement(toBy(locator));
    }

    public List<WebElement> findElements(Locator locator) {
        ltLogger.info("Finding elements with locator: {}", locator);
        return driver.findElements(toBy(locator));
    }

    public String getText(WebElement element) {
        ltLogger.info("Getting text of webElement: {}", element);
        String text = element.getText();
        ltLogger.info("Found text from element: {}", text);
        return text;
    }

    public String getText(Locator locator, int... timeout) {
        ltLogger.info("Finding text with locator: {}", locator.toString());
        int waitTime = Optional.ofNullable(timeout).filter(t -> t.length > 0).map(t -> t[0]).orElse(0);
        try {
            WebElement element = (waitTime > 0) ?
                    waitForElementToBeVisible(locator, waitTime) :
                    driver.findElement(toBy(locator));
            return getText(element);
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
        putValueToVerificationData(testVerificationDataKeys.JAVA_SCRIPTS, script);
        Object response = driver.executeScript(script);
        ltLogger.info("JS Script executed successfully. Script: {}", script);
        ltLogger.info("JS Script response: {}", response == null ? "null" : response.toString());
        return response;
    }

    public void executeJavaScriptOnSpecificElement(String script, WebElement element, Object... args) {
        ltLogger.info("Executing JavaScript {} on specific element: {} with args: {}", script, element, args);
        Object object = args == null ? driver.executeScript(script, element) : driver.executeScript(script, element, args);
        ltLogger.info("JS Script execution response: {}", object == null ? "null" : object.toString());
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

    public void sendKeys(Locator locator, CharSequence... keys) {
        ltLogger.info("Sending {} keys to element using ['{}', '{}']", keys, locator.type(), locator.value());
        WebElement element = waitForElementToBeVisible(locator, 5);
        if (element != null) {
            try {
                element.sendKeys(keys);
                ltLogger.info("Successfully sent keys '{}' to the element.", (Object[]) keys);
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

    public Set<String> getWindowHandles() {
        ltLogger.info("Getting all window handles.");
        Set<String> windowHandles = driver.getWindowHandles();
        ltLogger.info("Found {} window handles: {}", windowHandles.size(), windowHandles);
        return windowHandles;
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

    public void closeCurrentTab() {
        ltLogger.info("Closing the current tab");
        driver.close();
    }

    public void closeCurrentTabUsingJS() {
        ltLogger.info("Closing the current tab using JavaScript");
        try {
            executeScript(jsToCloseTab);
            ltLogger.info("Current tab closed successfully using JavaScript.");
        } catch (Exception e) {
            ltLogger.error("Failed to close the current tab using JavaScript. Error: {}", e.getMessage());
            throw new RuntimeException("Failed to close the current tab using JS", e);
        }
    }

    public void closeCurrentTabAndSwitchContextToLastTab() {
        try {
            Set<String> windowHandles = driver.getWindowHandles();

            if (windowHandles.size() < 2) {
                ltLogger.error("There are not enough tabs open to close and switch.");
                throw new RuntimeException("There are not enough tabs open.");
            }

            closeCurrentTab();

            // Wait for the number of window handles to change (indicating the tab was closed)
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(d -> d.getWindowHandles().size() == windowHandles.size() - 1);

            // Switch to the last tab (new context after closing the current one)
            Set<String> newWindowHandles = driver.getWindowHandles();
            ArrayList<String> tabs = new ArrayList<>(newWindowHandles);
            String lastTab = tabs.getLast();
            driver.switchTo().window(lastTab);
            ltLogger.info("Successfully switched to the last tab.");

        } catch (Exception e) {
            ltLogger.error("Failed to close the current tab and switch to the last tab. Error: {}", e.getMessage());
            throw new RuntimeException("Failed to close and switch tabs", e);
        }
    }

    public String getCurrentURL() {
        ltLogger.info("Getting current url.");
        String url = driver.getCurrentUrl();
        ltLogger.info("Current url is: {}", url);
        return url;
    }

    public void click(Locator locator, int... timeout) {
        ltLogger.info("Clicking element with locator: {}", locator.toString());
        if (timeout != null && timeout.length > 0)
            waitForElementToBeVisible(locator, timeout[0]).click();
        else
            driver.findElement(toBy(locator)).click();
    }

    public boolean isSelected(Locator locator, int... customTimeout) {
        int timeout = customTimeout == null || customTimeout.length == 0 ? 5 : customTimeout[0];
        ltLogger.info("Checking element with locator: {} is selected.", locator.toString());
        return waitForElementToBeVisible(locator, timeout).isSelected();
    }

    public void setLocalFileDetector() {
        driver.setFileDetector(new LocalFileDetector());
    }

    public void getURLWithCustomPageLoadTimeout(String url, Duration timeoutInSeconds) {
        Duration defaultPageLoadTimeout = driver.manage().timeouts().getPageLoadTimeout();
        ltLogger.info("Default page load timeout: {}", defaultPageLoadTimeout.toString());
        driver.manage().timeouts().pageLoadTimeout(timeoutInSeconds);
        getURL(url);
        driver.manage().timeouts().pageLoadTimeout(defaultPageLoadTimeout);
        ltLogger.info("After reset page load timeout: {}", driver.manage().timeouts().getPageLoadTimeout().toString());
    }

    public void getUrlWithoutTimeoutException(String url, Duration... timeoutInSeconds) {
        Duration timeout = timeoutInSeconds != null && timeoutInSeconds.length > 0 ?
                timeoutInSeconds[0] :
                Duration.ofSeconds(10);
        try {
            getURLWithCustomPageLoadTimeout(url, timeout);
        } catch (org.openqa.selenium.TimeoutException e) {
            ltLogger.error("Unable to find url with timeout: {}. Error: {}", timeout, e.getMessage());
        }
    }

    public void refreshPage() {
        ltLogger.info("Refreshing page ...");
        driver.navigate().refresh();
    }

    public void setCookies(Set<Cookie> cookies) {
        ltLogger.info("Setting cookies to {}", cookies);
        for (Cookie cookie : cookies) {
            driver.manage().addCookie(cookie);
        }
    }

    public void setCookies(Map<String, String> cookies) {
        ltLogger.info("Setting cookies from cookies map: {}", cookies);
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            driver.manage().addCookie(new Cookie(entry.getKey(), entry.getValue()));
        }
    }

    public String getCssValue(Locator locator, String attributeName, int... customTimeout) {
        int timeout = customTimeout == null || customTimeout.length == 0 ? 5 : customTimeout[0];
        ltLogger.info("Getting {} attribute value with locator: {}", attributeName, locator);
        String attributeValue = waitForElementToBeVisible(locator, timeout).getCssValue(attributeName);
        ltLogger.info("Found {} attribute value with locator: {} is: {}", attributeName, locator, attributeValue);
        return attributeValue;
    }

    public void clearText(Locator locator, int... customTimeout) {
        int timeout = customTimeout == null || customTimeout.length == 0 ? 2 : customTimeout[0];
        ltLogger.info("Clearing text with locator: {}", locator);
        waitForElementToBeVisible(locator, timeout).clear();
    }

    public void clearTextUsingKeyboardForWindows(Locator locator) {
        WebElement element = waitForElementToBeVisible(locator, 5);
        ltLogger.info("[Win] Selecting all the text in element with locator: {}", locator);
        sendKeys(locator, Keys.CONTROL, "a");
        ltLogger.info("[Win] Clearing text with locator via keyboard action: {}", locator);
        sendKeys(locator, Keys.BACK_SPACE);
    }

    public void clearTextUsingKeyboardForMac(Locator locator) {
        WebElement element = waitForElementToBeVisible(locator, 5);
        ltLogger.info("[Mac] Selecting all the text in element with locator: {}", locator);
        sendKeys(locator, Keys.COMMAND, "a");
        ltLogger.info("[Mac] Clearing text with locator via keyboard action: {}", locator);
        sendKeys(locator, Keys.DELETE);
    }

    public WebElement getNestedElement(WebElement parentElement, Locator locator) {
        ltLogger.info("Getting nested element with locator: {}", locator);
        return parentElement.findElement(toBy(locator));
    }

    public WebElement getNestedElement(Locator parentLocator, Locator childLocator, int... customTimeout) {
        int timeout = customTimeout == null || customTimeout.length == 0 ? 5 : customTimeout[0];
        ltLogger.info("Getting nested element with parent locator: {} child locator: {}", parentLocator, childLocator);
        WebElement parentElement = waitForElementToBeVisible(parentLocator, timeout);
        return getNestedElement(parentElement, childLocator);
    }

    public void setCustomAttributeValue(Locator locator, String attributeName, String value) {
        ltLogger.info("Setting custom attribute '{}' with value '{}' for element with locator: {}", attributeName, value,
                locator);
        String script = String.format("arguments[0].setAttribute('%s', '%s');", attributeName, value);
        executeJavaScriptOnSpecificElement(script, waitForElementToBeVisible(locator, 5));
        ltLogger.info("Custom attribute '{}' set to '{}' for element with locator: {}", attributeName, value, locator);
    }

    public void acceptAlert() {
        ltLogger.info("Accepting alert if present.");
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
            alert.accept();
            ltLogger.info("Alert accepted successfully.");
        } catch (NoAlertPresentException e) {
            ltLogger.warn("No alert present to accept.");
        } catch (Exception e) {
            ltLogger.error("Failed to accept alert: {}", e.getMessage());
        }
    }

    public boolean isClickable(Locator locator) {
        WebElement element = waitForElementToBeVisible(locator, 5);
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
            wait.until(ExpectedConditions.elementToBeClickable(element));
            ltLogger.info("Element with locator '{}' is clickable.", locator);
            return true;
        } catch (TimeoutException e) {
            ltLogger.warn("Element with locator '{}' is not clickable.", locator);
            return false;
        } catch (Exception e) {
            ltLogger.error("Error checking if element with locator '{}' is clickable: {}", locator, e.getMessage());
            return false;
        }
    }

    public List<String> getAllSelectedOptionFromDropdown(Locator locator) {
        ltLogger.info("Getting selected option from dropdown with locator: {}", locator);
        WebElement dropdownElement = waitForElementToBeVisible(locator, 5);
        Select dropdown = new Select(dropdownElement);
        List<WebElement> options = dropdown.getAllSelectedOptions();
        List<String> selectedOptions = new ArrayList<>();
        for (WebElement option : options) {
            selectedOptions.add(option.getText());
        }
        ltLogger.info("Selected options from dropdown with locator {} : {}", locator, selectedOptions);
        return selectedOptions;
    }

    public List<String> getAllOptionsFromDropdown(Locator locator) {
        ltLogger.info("Getting all options from dropdown with locator: {}", locator);
        WebElement dropdownElement = waitForElementToBeVisible(locator, 5);
        Select dropdown = new Select(dropdownElement);
        List<WebElement> options = dropdown.getOptions();
        List<String> optionTexts = new ArrayList<>();
        for (WebElement option : options) {
            optionTexts.add(option.getText());
        }
        ltLogger.info("Found {} options in dropdown with locator: {}", optionTexts.size(), locator);
        return optionTexts;
    }

    public void scrollElementIntoView(Locator locator) {
        ltLogger.info("Scrolling element into view with locator: {}", locator);
        WebElement element = waitForElementToBeVisible(locator, 5);
        executeJavaScriptOnSpecificElement(jsToScrollElementIntoView, element);
        ltLogger.info("Scrolled element with locator {} into view.", locator);
    }

    public String getPageSource() {
        ltLogger.info("Getting page source.");
        String pageSource = driver.getPageSource();
        ltLogger.info("Page source retrieved successfully.");
        return pageSource;
    }
}