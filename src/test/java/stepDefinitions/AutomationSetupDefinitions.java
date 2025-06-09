package stepDefinitions;

import automationHelper.AutomationHelper;
import automationHelper.ClientAutomationHelper;
import com.mysql.cj.util.StringUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static utility.EnvSetup.*;
import static utility.FrameworkConstants.*;

public class AutomationSetupDefinitions {
  AutomationHelper automationHelper = new AutomationHelper();
  ClientAutomationHelper clientAutomationHelper = new ClientAutomationHelper();
  private final Logger ltLogger = LogManager.getLogger(AutomationSetupDefinitions.class);

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
    clientTestUserName.set(getPropertyOrDefault(CUSTOM_CLIENT_USER_NAME, CLIENT_ENV_USER_NAME));
    clientTestAccessKey.set(getPropertyOrDefault(CUSTOM_CLIENT_USER_KEY, CLIENT_ENV_ACCESS_KEY));
    clientTestGridUrl.set(getPropertyOrDefault(CUSTOM_CLIENT_GRID_URL, CLIENT_GRID_URL));
    ltLogger.info(
      "User details are set to :-\n Test User Name: {}\n Test Access Key: {}\n Test Email: {}\n Test Password: {}\n Test Grid URL: {}\n Client Test User Name: {}\n Client Test Access Key: {}\n Client Test Grid URL: {}",
      testUserName.get(), testAccessKey.get(), testEmail.get(), testPassword.get(), testGridUrl.get(),
      clientTestUserName.get(), clientTestAccessKey.get(), clientTestGridUrl.get());
  }

  private String getPropertyOrDefault(String property, String defaultValue) {
    return StringUtils.isNullOrEmpty(System.getProperty(property, "")) ? defaultValue : System.getProperty(property);
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
    automationHelper.verifyTestStatusViaAPI(status, 20);
  }

  @Then("I stop the running build via API")
  public void iStopTheRunningBuildViaAPI() {
    automationHelper.stopRunningBuild();
  }

  @Then("^I confirm the build status is ([a-zA-Z0-9_=,: ]+)")
  public void iConfirmTheBuildStatusIsStopped(String status) {
    automationHelper.verifyBuildStatusViaAPI(status);
  }

  @Then("^I set test actions repeat count to ([0-9])$")
  public void iSetTestActionsRepeatCountToTestActionsRepeatCount(String count) {
    System.setProperty(REPEAT_TEST_ACTIONS, count);
  }

  @Then("I navigate to ML dashboard of current test")
  public void iNavigateToMLDashboardOfCurrentTest() {
    clientAutomationHelper.navigateToDashboardOfSpecificTest(TEST_SESSION_ID.get());
  }

  @Then("^I verify ([a-zA-Z ]+) logs from UI$")
  public void iVerifyCommandLogsFromUI(String logName) {
    clientAutomationHelper.verifyTestLogsFromUI(TEST_SESSION_ID.get(), logName);
  }

  @Then("^I verify test (video|screenshot|performanceReport) from UI$")
  public void iVerifyTestVideoFromUI(String testMediaType) {
    clientAutomationHelper.verifyTestMediaFromUI(TEST_SESSION_ID.get(), testMediaType);
  }

  /**
   * Tunnel related step definitions
   */

  @Then("^I ([a-zA-Z0-9_=,: ]+) tunnel$")
  public void iStartTunnel(String startOrStop) {
    if (startOrStop.equals("start")) {
      automationHelper.startTunnel();
    } else if (startOrStop.equals("stop")) {
      automationHelper.stopTunnel();
    }
  }

  @Then("^I start tunnel with (.+)$")
  public void iStartTunnelWithTunnelFlags(String args) {
    automationHelper.startTunnel(args);
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

  @And("I ensure port {int} is open")
  public void iEnsurePortIsOpen(int port) {
    automationHelper.iEnsurePortIsOpen(port);
  }

  @And("I block port {int}")
  public void iBlockPort(int port) {
    automationHelper.iBlockPort(port);
  }

  @And("I block ports {int} and SSH:{int}")
  public void iBlockPortsAndSSH(int port1, int port2) {
    automationHelper.iBlockPortsAndSSH(port1, port2);
  }

  @And("I block all SSH and TCP connections")
  public void iBlockAllSSHAndTCPConnections() {
    automationHelper.iBlockAllSSHAndTCPConnections();
  }

  @Then("I unblock port {int}")
  public void iUnblockPort(int port) {
    automationHelper.iUnblockPort(port);
  }

  @Then("I unblock all ports")
  public void iUnblockAllPorts() {
    automationHelper.iUnblockAllPorts();
  }

  @Then("I verify tunnel connection uses {word} protocol")
  public void iVerifyTunnelConnectionUsesProtocol(String protocol) {
    automationHelper.iVerifyTunnelConnectionUsesProtocol(protocol);
  }

  @Then("I verify tunnel uses {word} connection")
  public void iVerifyTunnelUsesConnection(String connectionType) {
    automationHelper.iVerifyTunnelUsesConnection(connectionType);
  }

  @Then("I verify tunnel connects on port {int}")
  public void iVerifyTunnelConnectsOnPort(int port) {
    automationHelper.iVerifyTunnelConnectsOnPort(port);
  }

  @Then("I verify tunnel connects on port {int} using SSH")
  public void iVerifyTunnelConnectsOnPortUsingSSH(int port) {
    automationHelper.iVerifyTunnelConnectsOnPortUsingSSH(port);
  }

  @Then("I verify tunnel connects on port {int} using TCP")
  public void iVerifyTunnelConnectsOnPortUsingTCP(int port) {
    automationHelper.iVerifyTunnelConnectsOnPortUsingTCP(port);
  }

  @Then("I verify tunnel connects using WebSocket")
  public void iVerifyTunnelConnectsUsingWebSocket() {
    automationHelper.iVerifyTunnelConnectsUsingWebSocket();
  }

  @And("I set up network restrictions according to {word}")
  public void iSetUpNetworkRestrictionsAccordingTo(String networkScenario) {
    automationHelper.iSetUpNetworkRestrictionsAccordingTo(networkScenario);
  }

  @Then("I reset network restrictions")
  public void iResetNetworkRestrictions() {
    automationHelper.iResetNetworkRestrictions();
  }

  @Then("I verify tunnel uses {word} protocol")
  public void iVerifyTunnelUsesProtocol(String expectedProtocol) {
    automationHelper.iVerifyTunnelUsesProtocol(expectedProtocol);
  }

  @And("I verify all tunnel flags are applied correctly")
  public void iVerifyAllTunnelFlagsAreAppliedCorrectly() {
    automationHelper.iVerifyAllTunnelFlagsAreAppliedCorrectly();
  }

  @Then("I restart tunnel")
  public void iRestartTunnel() {
    automationHelper.iRestartTunnel();
  }

  @And("I restart tunnel with {word}")
  public void iRestartTunnelWith(String flags) {
    automationHelper.iRestartTunnelWith(flags);
  }

  @Then("I simulate tunnel connection failure")
  public void iSimulateTunnelConnectionFailure() {
    automationHelper.iSimulateTunnelConnectionFailure();
  }

  @Then("I verify tunnel reconnection occurs")
  public void iVerifyTunnelReconnectionOccurs() {
    automationHelper.iVerifyTunnelReconnectionOccurs();
  }
}
