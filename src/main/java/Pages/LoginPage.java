package Pages;

import TestManagers.DriverManager;
import factory.Locator;
import factory.LocatorTypes;
import utility.EnvSetup;

public class LoginPage {
  private static final String url = EnvSetup.TEST_ENV.contains("stage") ?
    "https://stage-accounts.lambdatestinternal.com/login" :
    "https://accounts.lambdatest.com/login";

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
    driver.getURL(url);
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

}
