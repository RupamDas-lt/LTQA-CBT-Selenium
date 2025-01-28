package utility;

import factory.Locator;
import factory.LocatorTypes;

public class UrlsAndLocators {
  public static final String BASIC_AUTH = "https://admin:admin@the-internet.herokuapp.com/basic_auth";
  public static final Locator basicAuthHeading = new Locator(LocatorTypes.CSS, "#content h3");

}
