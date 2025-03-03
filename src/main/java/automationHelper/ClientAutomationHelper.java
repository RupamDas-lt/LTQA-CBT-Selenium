package automationHelper;

import Pages.LoginPage;
import TestManagers.CapabilityManager;
import TestManagers.DriverManager;
import com.mysql.cj.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.BaseClass;
import utility.CustomSoftAssert;
import utility.EnvSetup;

import static utility.EnvSetup.TEST_SCENARIO_NAME;
import static utility.FrameworkConstants.LAMBDA_TEST_CASE_END;
import static utility.FrameworkConstants.LAMBDA_TEST_CASE_START;

public class ClientAutomationHelper extends BaseClass {

  private static final String defaultClientCapabilities = "browserName=chrome,version=latest,platform=win10";

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

  private void startTestContext(String actionName) {
    driverManager.executeScript(LAMBDA_TEST_CASE_START + "=" + actionName);
  }

  private void endTestContext(String actionName) {
    driverManager.executeScript(LAMBDA_TEST_CASE_END + "=" + actionName);
  }

  public void loginToLTDashboard() {
    CustomSoftAssert clientSoftAssert = EnvSetup.CLIENT_SOFT_ASSERT.get();
    startTestContext("login");
    LoginPage loginPage = new LoginPage(driverManager);
    boolean status = loginPage.navigateToLoginPage();
    if (!status)
      throw new RuntimeException("Unable to navigate to login page");
    else {
      loginPage.fillUpLoginForm();
      loginPage.clickSubmitButton();
    }
    clientSoftAssert.assertTrue(loginPage.verifyUserIsLoggedIn(), "User is not logged in");
    EnvSetup.CLIENT_SOFT_ASSERT.set(clientSoftAssert);
    endTestContext("login");
  }
}
