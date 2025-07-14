package Pages.Shared;

import TestManagers.DriverManager;
import automationHelper.AutomationAPIHelper;
import factory.Locator;
import factory.LocatorTypes;
import utility.BaseClass;
import utility.EnvSetup;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static utility.EnvSetup.IS_GDPR_TEST_CONFIG;

public class LoginPage extends BaseClass {

    private static final String baseUrl = EnvSetup.TEST_ENV.contains("stage") ?
            "https://stage-accounts.lambdatestinternal.com/login" :
            "https://accounts.lambdatest.com/login";

    private final String ltLoginPageUrl = Boolean.parseBoolean(IS_GDPR_TEST_CONFIG) ?
            getCorrespondingDashboardUrlForGDPRUser(baseUrl) :
            baseUrl;

    private static Map<String, String> loginCookies;
    private static final Lock lock = new ReentrantLock();  // Lock to control access

    Locator heading = new Locator(LocatorTypes.CSS, "a[aria-label='LambdaTest']");
    Locator emailInput = new Locator(LocatorTypes.ID, "email");
    Locator passwordInput = new Locator(LocatorTypes.ID, "password");
    Locator submitButton = new Locator(LocatorTypes.ID, "login-button");
    Locator afterLoginPageContent = new Locator(LocatorTypes.CLASS_NAME, "product_main_content");

    DriverManager driver;

    public LoginPage(DriverManager driver) {
        this.driver = driver;
    }

    public boolean navigateToLoginPage() {
        driver.getURL(ltLoginPageUrl);
        return driver.isDisplayed(heading, 10);
    }

    public void fillUpLoginForm() {
        driver.sendKeys(emailInput, EnvSetup.USER_EMAIL);
        driver.sendKeys(passwordInput, EnvSetup.USER_PASS);
    }

    public void clickSubmitButton() {
        driver.click(submitButton);
    }

    public boolean verifyUserIsLoggedIn() {
        return driver.isDisplayed(afterLoginPageContent, 10);
    }

    private Map<String, String> getLoginCookies() {
        lock.lock();
        try {
            if (loginCookies != null && !loginCookies.isEmpty()) {
                return loginCookies;
            }
            AutomationAPIHelper automationAPIHelper = new AutomationAPIHelper();
            loginCookies = automationAPIHelper.getCookiesFromLoginAPI();
        } finally {
            lock.unlock();
        }
        return loginCookies;
    }

    public boolean loginToLTDashboardUsingCookies() {
        Map<String, String> loginCookies = getLoginCookies();
        if (loginCookies == null || loginCookies.isEmpty()) {
            return false;
        }
        driver.getURL(ltLoginPageUrl);
        driver.setCookies(loginCookies);
        driver.refreshPage();
        return driver.isDisplayed(afterLoginPageContent, 10);
    }

}
