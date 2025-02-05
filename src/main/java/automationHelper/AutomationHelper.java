package automationHelper;

import TestManagers.CapabilityManager;
import TestManagers.DriverManager;
import TestManagers.TunnelManager;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import utility.CustomSoftAssert;
import utility.EnvSetup;

import static utility.FrameworkConstants.*;
import static utility.UrlsAndLocators.*;

public class AutomationHelper {

  private final Logger ltLogger = LogManager.getLogger(AutomationHelper.class);

  CapabilityManager capabilityManager = new CapabilityManager();
  DriverManager driverManager = new DriverManager();
  TunnelManager tunnelManager;
  AutomationAPIHelper apiHelper = new AutomationAPIHelper();

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
    case "local":
      testLocalUrlWithTunnel();
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

  private void testLocalUrlWithTunnel() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    int httpServerStatus = apiHelper.getStatusCode(LOCAL_URL, null, null, null, null);
    Assert.assertEquals(httpServerStatus, 200,
      "Please start http server on port 8000 to start verifying tunnel. Expected status code: 200, original status code: " + httpServerStatus);
    driverManager.getURL(LOCAL_URL);
    boolean localUrlStatus = driverManager.isDisplayed(localUrlHeading);
    softAssert.assertTrue(localUrlStatus, "Local Url Not Displayed");
    EnvSetup.SOFT_ASSERT.set(softAssert);
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
    int maxTunnelStartRetry = 2;
    while (maxTunnelStartRetry > 0) {
      tunnelManager = new TunnelManager();
      tunnelManager.startTunnel("");
      boolean tunnelInfoAPIServerStatus = tunnelManager.checkTunnelInfoAPIServerIsInitiated();
      boolean tunnelCLIStatus;
      try {
        tunnelCLIStatus = tunnelManager.getTunnelStatusFromAPIServer();
      } catch (Exception e) {
        ltLogger.error("Encountered error while checking Tunnel Status: {}", e.getMessage());
        tunnelCLIStatus = false;
      }
      ltLogger.info("Tunnel info API server status {}, Tunnel CLI status {}", tunnelInfoAPIServerStatus,
        tunnelCLIStatus);
      if (tunnelInfoAPIServerStatus && tunnelCLIStatus)
        break;
      maxTunnelStartRetry--;
    }
  }

  public void stopTunnel() {
    tunnelManager.stopTunnel();
  }
}