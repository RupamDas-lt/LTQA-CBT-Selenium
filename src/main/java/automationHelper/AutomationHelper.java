package automationHelper;

import utility.CustomSoftAssert;
import utility.EnvSetup;

import static utility.UrlsAndLocators.BASIC_AUTH;
import static utility.UrlsAndLocators.basicAuthHeading;

public class AutomationHelper {

  CapabilityManager capabilityManager = new CapabilityManager();
  DriverManager driverManager = new DriverManager();

  private void createTestSession(String testCapability) {
    capabilityManager.buildTestCapability(testCapability);
    System.out.println("Caps: " + EnvSetup.TEST_CAPS.get());
    driverManager.createTestDriver();
  }

  private void runTestActions(String actionName) {
    switch (actionName) {
    case "basicAuthentication":
      basicAuthentication();
      break;
    default:
      baseTest();
      break;
    }
  }

  private void basicAuthentication() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    driverManager.getURL(BASIC_AUTH);
    String pageHeading = driverManager.getText(basicAuthHeading);
    softAssert.assertTrue(pageHeading.equals("Basic Auth"), "Basic Authentication Failed");
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  private void baseTest() {
  }

  public void startSessionWithSpecificCapabilities(String testCapability, String testActions) {
    createTestSession(testCapability);
    String[] testActionsArray = testActions.split(",");
    for (String testAction : testActionsArray) {
      runTestActions(testAction);
    }
    try {
      driverManager.quit();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}