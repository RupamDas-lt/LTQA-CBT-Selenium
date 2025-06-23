package stepDefinitions;

import automationHelper.AutomationHelper;
import automationHelper.ClientAutomationHelper;
import automationHelper.MultipleAutomationSessionsHelper;
import com.mysql.cj.util.StringUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
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

  @Then("^I start ([0-9]+) sessions ([a-zA-Z0-9_=,: ]+) driver quit to test ([a-zA-Z0-9_=,: ]+) with ([^\"]*)$")
  public void startMultipleSessionsAndPerformActivity(String numberOfSessions, String quitDriverStatus,
    String testActions, String capability) {
    MultipleAutomationSessionsHelper.runMultipleConcurrentSessions(Integer.parseInt(numberOfSessions),
      !quitDriverStatus.equals("without"), capability, testActions);
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

  @Then("^I create (test|build) share link$")
  public void iCreateTestShareLink(String linkType) {
    if (linkType.equals("test")) {
      automationHelper.createTestShareLinkAndStoreItToSessionReport(TEST_SESSION_ID.get());
    } else if (linkType.equals("build")) {
      automationHelper.createBuildShareLinkAndStoreItToSessionReport(BUILD_ID.get());
    }
  }

  @Then("^I verify (test|build) share link via API$")
  public void iVerifyTestShareLink(String linkType) {
    automationHelper.verifyShareLinkViaApi(linkType);
  }

  @Then("^I set test actions repeat count to ([0-9])$")
  public void iSetTestActionsRepeatCountToTestActionsRepeatCount(String count) {
    System.setProperty(REPEAT_TEST_ACTIONS, count);
  }

  @When("^I upload ([a-zA-Z0-9_=,: ]+) from file path (.+) to lambda storage$")
  public void iUploadFileToLambdaStorage(String uploadType, String filePath) {
    automationHelper.uploadFileToLambdaStorage(uploadType, filePath);
  }

  @Then("^I verify the ([a-zA-Z0-9_=,: ]+) is uploaded successfully with (.+) file name$")
  public void iVerifyFileInLambdaStorage(String uploadType, String fileName) {
    automationHelper.verifyFileInLambdaStorage(uploadType, fileName);
  }

  @Then("^I delete ([a-zA-Z0-9_=,: ]+) with (.+) from lambda storage$")
  public void iDeleteFileFromLambdaStorage(String type, String fileName) {
    automationHelper.deleteFileFromLambdaStorage(type, fileName);
  }

  @Then("^I verify ([a-zA-Z0-9_=,: ]+) via swagger (test|build) details API$")
  public void iVerifyDetailsFromTestAPI(String detailsToVerify, String sourceApi) {
    ltLogger.info("Verifying details: {} from source API: {}", detailsToVerify, sourceApi);
    switch (detailsToVerify) {
    case "test tags":
      automationHelper.verifyTestTags();
      break;
    case "build tags":
      automationHelper.verifyBuildTags();
      break;
    default:
      ltLogger.error("Unknown Details to verify: {}", detailsToVerify);
      throw new IllegalArgumentException("Unknown Details to verify: " + detailsToVerify);
    }
  }

  /// --------------------------------- Client test related step definitions ---------------------------------
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

  @Then("^I verify (test|build) share link via UI$")
  public void iVerifyShareLinkViaUI(String linkType) {
    clientAutomationHelper.verifyShareLinkViaUI(linkType);
  }

  /// --------------------------------- Tunnel related step definitions ---------------------------------
  @Then("^I ([a-zA-Z0-9_=,: ]+) tunnel$")
  public void iStartTunnel(String startOrStopOrRestart) {
    if (startOrStopOrRestart.equals("start")) {
      automationHelper.startTunnel();
    } else if (startOrStopOrRestart.equals("stop")) {
      automationHelper.stopTunnel();
    } else if (startOrStopOrRestart.equals("restart")) {
      automationHelper.iRestartTunnel();
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

  @And("I ensure port {word} is open")
  public void iEnsurePortIsOpen(String port) {
    automationHelper.modifyNetworkRestrictions(clientSideNetworkOperations.ENSURE_PORT_OPEN, port);
  }

  @Then("I verify tunnel connection uses {word} protocol")
  public void iVerifyTunnelConnectionUsesProtocol(String protocol) {
    automationHelper.iVerifyTunnelConnectionUsesProtocol(protocol);
  }

  @Then("^I verify tunnel uses (\\S+) (connection|protocol)$")
  public void iVerifyTunnelUsesProtocol(String expectedProtocol, String connectionType) {
    if (connectionType.equals("connection"))
      automationHelper.iVerifyTunnelUsesConnection(expectedProtocol);
    else if (connectionType.equals("protocol"))
      automationHelper.iVerifyTunnelUsesProtocol(expectedProtocol);
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
    switch (networkScenario) {
    case "block_port_22":
      automationHelper.modifyNetworkRestrictions(clientSideNetworkOperations.BLOCK_SSH_22);
      break;
    case "block_ssh_443":
      automationHelper.modifyNetworkRestrictions(clientSideNetworkOperations.BLOCK_SSH_443);
      break;
    case "block_ssh_ports":
      automationHelper.modifyNetworkRestrictions(clientSideNetworkOperations.BLOCK_SSH_PORTS);
      break;
    case "block_tcp_443":
      automationHelper.modifyNetworkRestrictions(clientSideNetworkOperations.BLOCK_TCP_443);
      break;
    case "block_all_ssh_tcp":
      automationHelper.modifyNetworkRestrictions(clientSideNetworkOperations.BLOCK_ALL_SSH_TCP);
      break;
    case "no_restrictions":
      automationHelper.modifyNetworkRestrictions(clientSideNetworkOperations.FLUSH_ALL_RULES);
      break;
    case "unblock_all_for_servers":
      automationHelper.modifyNetworkRestrictions(clientSideNetworkOperations.UNBLOCK_ALL_FOR_SERVERS);
      break;
    default:
      ltLogger.error("Unknown network scenario: {}", networkScenario);
      throw new IllegalArgumentException("Unknown network scenario: " + networkScenario);
    }
  }

  @And("I verify all tunnel flags are applied correctly")
  public void iVerifyAllTunnelFlagsAreAppliedCorrectly() {
    automationHelper.iVerifyAllTunnelFlagsAreAppliedCorrectly();
  }

  @And("I restart tunnel with {word}")
  public void iRestartTunnelWith(String flags) {
    automationHelper.iRestartTunnelWith(flags);
  }

  @Then("I verify tunnel reconnection occurs")
  public void iVerifyTunnelReconnectionOccurs() {
    automationHelper.iVerifyTunnelReconnectionOccurs();
  }

  @Then("I extract build id from session ID")
  public void iExtractBuildIdFromSessionID() {
    String sessionId = TEST_SESSION_ID_QUEUE.get() == null || TEST_SESSION_ID_QUEUE.get().isEmpty() ?
      TEST_TEST_ID.get() :
      TEST_SESSION_ID_QUEUE.get().peek();
    automationHelper.getBuildIdFromSessionIdAndStoreItToEnvVar(sessionId);
  }
}
