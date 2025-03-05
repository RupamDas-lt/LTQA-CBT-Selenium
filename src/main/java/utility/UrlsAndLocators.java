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

  public static final String TODO_APP_URL = "https://lambdatest.github.io/sample-todo-app";
  public static final Locator todoListItem1 = new Locator(LocatorTypes.NAME, "li1");
  public static final Locator todoListItem2 = new Locator(LocatorTypes.NAME, "li2");
  public static final Locator todoListItem3 = new Locator(LocatorTypes.NAME, "li3");
  public static final Locator todoListItem4 = new Locator(LocatorTypes.NAME, "li4");
  public static final Locator todoListItem5 = new Locator(LocatorTypes.NAME, "li5");
  public static final Locator todoInput = new Locator(LocatorTypes.XPATH, "//*[@id='sampletodotext']");
  public static final Locator todoAddButton = new Locator(LocatorTypes.ID, "addbutton");
  public static final Locator todoNewEnteredText = new Locator(LocatorTypes.XPATH,
    "/html/body/div/div/div/ul/li[6]/span");

  public static final String BROWSER_DETAILS_URL = "https://www.whatismybrowser.com/";
  public static final Locator browserDetailsText = new Locator(LocatorTypes.CSS,
    "#primary-browser-detection-backend .string-major a");

  public static final String FILE_UPLOAD_URL = "https://the-internet.herokuapp.com/upload";
  public static final Locator chooseFileButton = new Locator(LocatorTypes.ID, "file-upload");
  public static final Locator uploadFileButton = new Locator(LocatorTypes.ID, "file-submit");
  public static final Locator uploadedFileHeading = new Locator(LocatorTypes.XPATH,
    "//h3[contains(text(),'File Uploaded!')]");

  public static final String GEOLOCATION_VERIFICATION_URL = "https://geotargetly.com/my-ip-geolocation";
  public static final Locator countryCode = new Locator(LocatorTypes.ID, "geotargetly_country_code");
  public static final Locator countryName = new Locator(LocatorTypes.ID, "geotargetly_country_name");
}
