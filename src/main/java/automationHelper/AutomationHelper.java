package automationHelper;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.CustomSoftAssert;
import utility.EnvSetup;

import static utility.FrameworkConstants.*;
import static utility.UrlsAndLocators.BASIC_AUTH;
import static utility.UrlsAndLocators.basicAuthHeading;

public class AutomationHelper {

  private final Logger ltLogger = LogManager.getLogger(AutomationHelper.class);

  CapabilityManager capabilityManager = new CapabilityManager();
  DriverManager driverManager = new DriverManager();

  private void createTestSession(String testCapability) {
    StopWatch stopWatch = new StopWatch();
    capabilityManager.buildTestCapability(testCapability);
    ltLogger.info("Test Caps: {}", EnvSetup.TEST_CAPS.get());
    stopWatch.start();
    driverManager.createTestDriver();
    driverManager.getCookies();
    stopWatch.stop();
    EnvSetup.TEST_REPORT.get().put(TEST_SETUP_TIME, String.valueOf(stopWatch.getTime() / 1000.00));
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
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    String[] testActionsArray = testActions.split(",");
    for (String testAction : testActionsArray) {
      runTestActions(testAction);
    }
    stopWatch.stop();
    EnvSetup.TEST_REPORT.get().put(TEST_EXECUTION_TIME, String.valueOf(stopWatch.getTime() / 1000.00));
    stopWatch.reset();
    stopWatch.start();
    try {
      driverManager.quit();
    } catch (Exception e) {
      e.printStackTrace();
    }
    stopWatch.stop();
    EnvSetup.TEST_REPORT.get().put(TEST_STOP_TIME, String.valueOf(stopWatch.getTime() / 1000.00));
  }

  public void startTunnel() {
    TunnelManager tunnelManager = new TunnelManager();
    tunnelManager.startTunnel("", 1);
  }
}