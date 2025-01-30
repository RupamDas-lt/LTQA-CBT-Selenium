package utility;

import factory.Locator;
import factory.LocatorTypes;

public class UrlsAndLocators {
  public static final String BASIC_AUTH = "https://admin:admin@the-internet.herokuapp.com/basic_auth";
  public static final Locator basicAuthHeading = new Locator(LocatorTypes.CSS, "#content h3");

  public static final String LOCAL_URL = "http://localhost:8000/";
  public static final String LOCAL_IOS = "http://localhost.lambdatest.com:8000/";
  public static final Locator localUrlHeading = new Locator(LocatorTypes.XPATH,
    "//h1[contains(text(),'Directory listing for')]");
}
