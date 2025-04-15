package stepDefinitions;

import automationHelper.AutomationHelper;
import automationHelper.ClientAutomationHelper;
import com.mysql.cj.util.StringUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import static utility.EnvSetup.*;
import static utility.FrameworkConstants.*;

public class AutomationSetupDefinitions {
  AutomationHelper automationHelper = new AutomationHelper();
  ClientAutomationHelper clientAutomationHelper = new ClientAutomationHelper();

  @Then("^I start session ([a-zA-Z0-9_=,: ]+) driver quit to test ([a-zA-Z0-9_=,: ]+) with ([^\"]*)$")
  public void startSessionAndPerformActivity(String quitDriverStatus, String testActions, String capability) {
    automationHelper.startSessionWithSpecificCapabilities(!quitDriverStatus.equals("without"), capability, testActions);
  }

  @Then("^I start session ([a-zA-Z0-9_=,: ]+) driver quit on ([a-zA-Z0-9_=,: ]+) to test ([a-zA-Z0-9_=,: ]+) with ([^\"]*)$")
  public void startSessionOnSpecificCloudPlatformAndPerformActivity(String quitDriverStatus, String cloudPlatform,
    String testActions, String capability) {
    automationHelper.startSessionWithSpecificCapabilities(!quitDriverStatus.equals("without"), capability, testActions,
      cloudPlatform);
  }

  @Given("Setup user details")
  public void setupUserDetails() {
    testUserName.set(getPropertyOrDefault(CUSTOM_USER_NAME, USER_NAME));
    testAccessKey.set(getPropertyOrDefault(CUSTOM_USER_KEY, ACCESS_KEY));
    testEmail.set(getPropertyOrDefault(CUSTOM_USER_EMAIL, USER_EMAIL));
    testPassword.set(getPropertyOrDefault(CUSTOM_USER_PASS, USER_PASS));
    testGridUrl.set(getPropertyOrDefault(CUSTOM_GRID_URL, GRID_URL));
  }

  private String getPropertyOrDefault(String property, String defaultValue) {
    return StringUtils.isNullOrEmpty(System.getProperty(property, "")) ? defaultValue : System.getProperty(property);
  }

  @Then("^I ([a-zA-Z0-9_=,: ]+) tunnel$")
  public void iStartTunnel(String startOrStop) {
    if (startOrStop.equals("start")) {
      automationHelper.startTunnel();
    } else if (startOrStop.equals("stop")) {
      automationHelper.stopTunnel();
    }
  }

  @Then("^I ([a-zA-Z0-9_=,: ]+) client test session$")
  public void iStartClientTestSession(String startOrStop) {
    if (startOrStop.equals("start")) {
      clientAutomationHelper.startClientSession();
    } else {
      clientAutomationHelper.stopClientSession();
    }
  }

  @And("Login to LT dashboard")
  public void loginToLTDashboard() {
    clientAutomationHelper.loginToLTDashboard();
  }

  @Then("I upload sample terminal logs")
  public void iUploadSampleTerminalLogs() {
    automationHelper.uploadSampleTerminalLogs();
  }

  @Then("^I verify ([a-zA-Z0-9_=,: ]+) Log via API$")
  public void iVerifyTestLogsViaAPI(String logName) {
    automationHelper.verifyLogs(logName);
  }

  @Then("^I verify video via API$")
  public void iVerifyVideoViaAPI() {
    automationHelper.verifyLogs("video");
  }

  @Then("I stop the running test via API")
  public void iStopTheRunningTestViaAPI() {
    automationHelper.stopRunningTest();
  }

  @Then("^I confirm the test status is ([a-zA-Z0-9_=,: ]+)$")
  public void iConfirmTheTestStatus(String status) {
    automationHelper.verifyTestStatusViaAPI(status, 10);
  }

  @Then("I stop the running build via API")
  public void iStopTheRunningBuildViaAPI() {
    automationHelper.stopRunningBuild();
  }

  @Then("^I confirm the build status is ([a-zA-Z0-9_=,: ]+)")
  public void iConfirmTheBuildStatusIsStopped(String status) {
    automationHelper.verifyBuildStatusViaAPI(status);
  }

  @Then("^I verify tunnel is ([a-zA-Z0-9_=,: ]+) via API$")
  public void iVerifyTunnelIsStartedViaAPI(String status) {
    String expectedTunnelName = TEST_TUNNEL_NAME.get();
    automationHelper.verifyTunnelStatusViaAPI(expectedTunnelName, status);
  }

  @Then("I stop tunnel via api")
  public void iStopTunnelViaApi() {
    String tunnelID = TEST_TUNNEL_ID.get();
    automationHelper.stopRunningTunnelViaAPI(tunnelID);
  }

  @Then("^I set test actions repeat count to ([0-9])$")
  public void iSetTestActionsRepeatCountToTestActionsRepeatCount(String count) {
    System.setProperty(REPEAT_TEST_ACTIONS, count);
  }
}
