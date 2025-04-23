package automationHelper;

import Pages.LTDashboardCommonActions;
import Pages.LoginPage;
import Pages.TestCommandLogsPage;
import TestManagers.CapabilityManager;
import TestManagers.DriverManager;
import com.mysql.cj.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import utility.BaseClass;
import utility.CustomSoftAssert;
import utility.EnvSetup;

import static utility.EnvSetup.TEST_SCENARIO_NAME;
import static utility.FrameworkConstants.TEST_END_TIMESTAMP;

public class ClientAutomationHelper extends BaseClass {

  private static final String defaultClientCapabilities = "browserName=chrome,version=latest,platform=win10,network=true,idleTimeout=900";

  private final Logger ltLogger = LogManager.getLogger(ClientAutomationHelper.class);
  DriverManager driverManager = new DriverManager();
  CapabilityManager capabilityManager = new CapabilityManager();

  public void startClientSession() {
    String capsString = StringUtils.isNullOrEmpty(TEST_SCENARIO_NAME.get()) ?
      defaultClientCapabilities :
      defaultClientCapabilities + ",name=" + TEST_SCENARIO_NAME.get().replace(",", "_");
    capabilityManager.buildClientTestCapability(capsString);
    driverManager.createClientDriver();
    EnvSetup.IS_UI_VERIFICATION_ENABLED.set(true);
  }

  public void stopClientSession() {
    driverManager.quit();
  }

  public void loginToLTDashboard() {
    CustomSoftAssert clientSoftAssert = EnvSetup.CLIENT_SOFT_ASSERT.get();
    LTHooks.startStepContext(driverManager, "login");
    LoginPage loginPage = new LoginPage(driverManager);
    boolean loginUsingCookies = false;
    //    loginUsingCookies = loginPage.loginToLTDashboardUsingCookies();
    ltLogger.info("Login done using cookies: {}", loginUsingCookies);
    if (!loginUsingCookies) {
      boolean status = loginPage.navigateToLoginPage();
      if (!status)
        throw new RuntimeException("Unable to navigate to login page");
      else {
        loginPage.fillUpLoginForm();
        loginPage.clickSubmitButton();
      }
      clientSoftAssert.assertTrue(loginPage.verifyUserIsLoggedIn(), "User is not logged in");
    }
    EnvSetup.CLIENT_SOFT_ASSERT.set(clientSoftAssert);
    LTHooks.endStepContext(driverManager, "login");
  }

  public void navigateToDashboardOfSpecificTest(String testId) {
    LTHooks.startStepContext(driverManager, "Navigate to Test home page");
    LTDashboardCommonActions testHomePage = new LTDashboardCommonActions(testId, driverManager);
    Assert.assertTrue(testHomePage.navigateToHomePageOfSpecificTest(), "Unable to open test home page");
  }

  public void verifyTestLogsFromUI(String testId, String logName) {

    // Wait for logs to be uploaded
    waitForSomeTimeAfterTestCompletionForLogsToBeUploaded(EnvSetup.TEST_REPORT.get().get(TEST_END_TIMESTAMP).toString(),
      120);

    CustomSoftAssert clientSoftAssert = EnvSetup.CLIENT_SOFT_ASSERT.get();
    try {
      LTHooks.startStepContext(driverManager, String.format("Verify %s logs from UI", logName));
      switch (logName) {
      case "command":
        TestCommandLogsPage testCommandLogsPage = new TestCommandLogsPage(testId, driverManager, clientSoftAssert);
        boolean openCommandLogsTabOpenStatus = testCommandLogsPage.openCommandLogsTab();
        if (openCommandLogsTabOpenStatus) {
          testCommandLogsPage.verifyCommandLogs();
        } else {
          clientSoftAssert.fail("Unable to open command logs tab...");
        }
        break;
      case "network":
        break;
      }
    } finally {
      LTHooks.endStepContext(driverManager, String.format("Verify %s logs from UI", logName));
    }
    EnvSetup.CLIENT_SOFT_ASSERT.set(clientSoftAssert);
  }
}
