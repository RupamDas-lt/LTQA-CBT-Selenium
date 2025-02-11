package utility;

import factory.Locator;
import factory.LocatorTypes;

public class UrlsAndLocators {
  public static final String GOOGLE_URL = "https://www.google.com/";

  public static final String BASIC_AUTH = "https://admin:admin@the-internet.herokuapp.com/basic_auth";
  public static final Locator basicAuthHeading = new Locator(LocatorTypes.CSS, "#content h3");

  public static final String LOCAL_URL = "http://localhost:8000/";
  public static final String LOCAL_IOS = "http://localhost.lambdatest.com:8000/";
  public static final Locator localUrlHeading = new Locator(LocatorTypes.XPATH,
    "//h1[contains(text(),'Directory listing for')]");

  public static final String SELF_SIGNED_URL = "https://self-signed.badssl.com/";
  public static final String SELF_SIGNED_URL_2 = "https://untrusted-root.badssl.com/";
  public static final Locator selfSignedPageHeading = new Locator(LocatorTypes.CSS, "#content > h1:nth-child(1)");
  public static final String SELF_SIGNED_URL_FALLBACK = "https://expired-rsa-dv.ssl.com/?_gl=1*jiqbno*_gcl_au*MTAzODU4MTU3MC4xNzM2MjI3MDkx";
  public static final Locator selfSignedFallbackPageHeading = new Locator(LocatorTypes.TAG_NAME, "h1");

  public static final String HEROKU_APP_LOGIN_PAGE = "https://the-internet.herokuapp.com/login";
  public static final Locator herokuLoginPageHeading = new Locator(LocatorTypes.CSS, "[class='subheader']");
  public static final Locator herokuLoginPageUsernameInput = new Locator(LocatorTypes.ID, "username");
  public static final Locator herokuLoginPagePasswordInput = new Locator(LocatorTypes.ID, "password");
  public static final Locator herokuLoginPageLoginButton = new Locator(LocatorTypes.CSS, "button[type='submit']");
  public static final Locator herokuAfterLoginPageHeading = new Locator(LocatorTypes.ID, "flash");

}
