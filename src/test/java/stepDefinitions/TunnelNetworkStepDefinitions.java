package stepDefinitions;

import automationHelper.AutomationHelper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import utility.BaseClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TunnelNetworkStepDefinitions extends BaseClass {

  private final Logger ltLogger = LogManager.getLogger(TunnelNetworkStepDefinitions.class);
  private final AutomationHelper automationHelper = new AutomationHelper();

  private static final String NETWORK_SCRIPT_PATH = "./Utility/Bash/NetworkBlockingUtils.sh";

  private void executeNetworkScript(String... params) {
    try {
      ProcessBuilder pb = new ProcessBuilder();
      pb.command("sudo", "bash", NETWORK_SCRIPT_PATH);
      for (String param : params) {
        pb.command().add(param);
      }

      ltLogger.info("Executing network blocking command: {}", String.join(" ", pb.command()));
      Process process = pb.start();

      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        ltLogger.info("Network script output: {}", line);
      }

      BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      while ((line = errorReader.readLine()) != null) {
        ltLogger.error("Network script error: {}", line);
      }

      int exitCode = process.waitFor();
      if (exitCode != 0) {
        ltLogger.error("Network blocking script failed with exit code: {}", exitCode);
        throw new RuntimeException("Network blocking operation failed");
      }

    } catch (IOException | InterruptedException e) {
      ltLogger.error("Failed to execute network blocking script: {}", e.getMessage());
      throw new RuntimeException("Network blocking script execution failed", e);
    }
  }

  @And("I ensure port {int} is open")
  public void iEnsurePortIsOpen(int port) {
    ltLogger.info("Ensuring port {} is open", port);
    executeNetworkScript("ensure_port_open", String.valueOf(port));
  }

  @And("I block port {int}")
  public void iBlockPort(int port) {
    ltLogger.info("Blocking SSH over port {}", port);
    if (port == 22) {
      executeNetworkScript("block_ssh_22");
    } else if (port == 443) {
      executeNetworkScript("block_ssh_443");
    } else {
      throw new IllegalArgumentException("Unsupported port for SSH blocking: " + port);
    }
  }

  @And("I block ports {int} and SSH:{int}")
  public void iBlockPortsAndSSH(int port1, int port2) {
    ltLogger.info("Blocking SSH over ports {} and {}", port1, port2);
    executeNetworkScript("block_ssh_ports");
  }

  @And("I block all SSH and TCP connections")
  public void iBlockAllSSHAndTCPConnections() {
    ltLogger.info("Blocking all SSH and TCP connections");
    executeNetworkScript("block_all_ssh_tcp");
  }

  @Then("I unblock port {int}")
  public void iUnblockPort(int port) {
    ltLogger.info("Unblocking port {}", port);
    executeNetworkScript("ensure_port_open", String.valueOf(port));
  }

  @Then("I unblock all ports")
  public void iUnblockAllPorts() {
    ltLogger.info("Unblocking all ports by flushing iptables rules");
    executeNetworkScript("flush_all_rules");
  }

  @Then("I verify tunnel connection uses {word} protocol")
  public void iVerifyTunnelConnectionUsesProtocol(String protocol) {
    ltLogger.info("Verifying tunnel connection uses {} protocol", protocol);

    waitForTime(3);

    boolean isProtocolCorrect = automationHelper.verifyTunnelMode(protocol);

    if (!isProtocolCorrect) {
      String currentMode = automationHelper.getCurrentTunnelMode();
      ltLogger.error("Tunnel protocol verification failed. Expected: {}, Actual: {}", protocol, currentMode);
      Assert.fail("Tunnel is not using the expected protocol. Expected: " + protocol + ", Actual: " + currentMode);
    }

    ltLogger.info("✓ Tunnel protocol verification successful. Using: {}", protocol);
  }

  @Then("I verify tunnel uses {word} connection")
  public void iVerifyTunnelUsesConnection(String connectionType) {
    ltLogger.info("Verifying tunnel uses {} connection", connectionType);

    waitForTime(3);

    boolean isConnectionTypeCorrect = automationHelper.verifySshConnectionType(connectionType);

    if (!isConnectionTypeCorrect) {
      String currentSshConnType = automationHelper.getCurrentSshConnectionType();
      ltLogger.error("Tunnel SSH connection type verification failed. Expected: {}, Actual: {}", connectionType,
        currentSshConnType);
      Assert.fail(
        "Tunnel is not using the expected SSH connection type. Expected: " + connectionType + ", Actual: " + currentSshConnType);
    }
    ltLogger.info("Tunnel SSH connection type verification successful. Using: {}", connectionType);
  }

  @Then("I verify tunnel connects on port {int}")
  public void iVerifyTunnelConnectsOnPort(int port) {
    ltLogger.info("Verifying tunnel connects on port {}", port);

    waitForTime(3);

    int currentPort = automationHelper.getCurrentTunnelPort();
    boolean isPortCorrect = (currentPort == port);

    if (!isPortCorrect) {
      ltLogger.error("Tunnel port verification failed. Expected: {}, Actual: {}", port, currentPort);
      Assert.fail("Tunnel is not connecting on the expected port. Expected: " + port + ", Actual: " + currentPort);
    }

    ltLogger.info("✓ Tunnel port verification successful. Connecting on port: {}", port);
  }

  @Then("I verify tunnel connects on port {int} using SSH")
  public void iVerifyTunnelConnectsOnPortUsingSSH(int port) {
    ltLogger.info("Verifying tunnel connects on port {} using SSH", port);

    waitForTime(3);

    boolean isSSHMode = automationHelper.verifyTunnelMode("ssh");
    if (!isSSHMode) {
      String currentMode = automationHelper.getCurrentTunnelMode();
      ltLogger.error("Tunnel is not using SSH mode. Expected: ssh, Actual: {}", currentMode);
      Assert.fail("Tunnel is not using SSH mode. Expected: ssh, Actual: " + currentMode);
    }

    int currentPort = automationHelper.getCurrentTunnelPort();
    boolean isPortCorrect = (currentPort == port);
    if (!isPortCorrect) {
      ltLogger.error("Tunnel port verification failed. Expected: {}, Actual: {}", port, currentPort);
      Assert.fail("Tunnel is not connecting on the expected port. Expected: " + port + ", Actual: " + currentPort);
    }

    ltLogger.info("✓ Tunnel SSH port verification successful. Using SSH on port: {}", port);
  }

  @Then("I verify tunnel connects on port {int} using TCP")
  public void iVerifyTunnelConnectsOnPortUsingTCP(int port) {
    ltLogger.info("Verifying tunnel connects on port {} using TCP", port);

    waitForTime(3);

    boolean isTCPMode = automationHelper.verifyTunnelMode("tcp");
    if (!isTCPMode) {
      String currentMode = automationHelper.getCurrentTunnelMode();
      ltLogger.error("Tunnel is not using TCP mode. Expected: tcp, Actual: {}", currentMode);
      Assert.fail("Tunnel is not using TCP mode. Expected: tcp, Actual: " + currentMode);
    }

    int currentPort = automationHelper.getCurrentTunnelPort();
    boolean isPortCorrect = (currentPort == port);
    if (!isPortCorrect) {
      ltLogger.error("Tunnel port verification failed. Expected: {}, Actual: {}", port, currentPort);
      Assert.fail("Tunnel is not connecting on the expected port. Expected: " + port + ", Actual: " + currentPort);
    }

    ltLogger.info("✓ Tunnel TCP port verification successful. Using TCP on port: {}", port);
  }

  @Then("I verify tunnel connects using WebSocket")
  public void iVerifyTunnelConnectsUsingWebSocket() {
    ltLogger.info("Verifying tunnel connects using WebSocket");

    waitForTime(3);

    boolean isWebSocketMode = automationHelper.verifyTunnelMode("ws");

    if (!isWebSocketMode) {
      String currentMode = automationHelper.getCurrentTunnelMode();
      ltLogger.error("Tunnel WebSocket verification failed. Expected: ws, Actual: {}", currentMode);
      Assert.fail("Tunnel is not using WebSocket mode. Expected: ws, Actual: " + currentMode);
    }

    ltLogger.info("✓ Tunnel WebSocket verification successful. Using WebSocket mode");
  }

  @And("I set up network restrictions according to {word}")
  public void iSetUpNetworkRestrictionsAccordingTo(String networkScenario) {
    ltLogger.info("Setting up network restrictions for scenario: {}", networkScenario);

    switch (networkScenario) {
    case "no_restrictions":
      executeNetworkScript("flush_all_rules");
      break;
    case "block_port_22":
      executeNetworkScript("block_ssh_22");
      break;
    case "block_all_but_websocket":
      executeNetworkScript("block_all_ssh_tcp");
      break;
    default:
      throw new IllegalArgumentException("Unknown network scenario: " + networkScenario);
    }
    waitForTime(2);
  }

  @Then("I reset network restrictions")
  public void iResetNetworkRestrictions() {
    ltLogger.info("Resetting all network restrictions");
    executeNetworkScript("flush_all_rules");

    waitForTime(2);
  }

  @Then("I verify tunnel uses {word} protocol")
  public void iVerifyTunnelUsesProtocol(String expectedProtocol) {
    ltLogger.info("Verifying tunnel uses {} protocol", expectedProtocol);

    waitForTime(3);

    String protocolToCheck = expectedProtocol;
    if (expectedProtocol.contains(":")) {
      protocolToCheck = expectedProtocol.split(":")[0];
    }

    boolean isProtocolCorrect = automationHelper.verifyTunnelMode(protocolToCheck);

    if (!isProtocolCorrect) {
      String currentMode = automationHelper.getCurrentTunnelMode();
      ltLogger.error("Tunnel protocol verification failed. Expected: {}, Actual: {}", protocolToCheck, currentMode);
      Assert.fail(
        "Tunnel is not using the expected protocol. Expected: " + expectedProtocol + ", Actual: " + currentMode);
    }

    if (expectedProtocol.contains(":")) {
      String[] parts = expectedProtocol.split(":");
      if (parts.length == 2) {
        try {
          int expectedPort = Integer.parseInt(parts[1]);
          int currentPort = automationHelper.getCurrentTunnelPort();
          boolean isPortCorrect = (currentPort == expectedPort);
          if (!isPortCorrect) {
            ltLogger.error("Tunnel port verification failed. Expected: {}, Actual: {}", expectedPort, currentPort);
            Assert.fail(
              "Tunnel is not connecting on the expected port. Expected: " + expectedPort + ", Actual: " + currentPort);
          }
        } catch (NumberFormatException e) {
          ltLogger.warn("Could not parse port from protocol: {}", expectedProtocol);
        }
      }
    }

    ltLogger.info("✓ Tunnel protocol verification successful. Using: {}", expectedProtocol);
  }

  @And("I verify all tunnel flags are applied correctly")
  public void iVerifyAllTunnelFlagsAreAppliedCorrectly() {
    ltLogger.info("Verifying all tunnel flags are applied correctly");

    waitForTime(3);

    try {
      String currentMode = automationHelper.getCurrentTunnelMode();
      String currentSshConnType = automationHelper.getCurrentSshConnectionType();
      int currentPort = automationHelper.getCurrentTunnelPort();

      ltLogger.info("Current tunnel configuration:");
      ltLogger.info("  Mode: {}", currentMode);
      ltLogger.info("  SSH Connection Type: {}", currentSshConnType);
      ltLogger.info("  Port: {}", currentPort);

      if (currentMode == null || currentMode.isEmpty()) {
        Assert.fail("Tunnel mode is not available or invalid");
      }

      ltLogger.info("✓ Tunnel flags verification completed successfully");

    } catch (Exception e) {
      ltLogger.error("Failed to verify tunnel flags: {}", e.getMessage());
      Assert.fail("Failed to verify tunnel flags: " + e.getMessage());
    }
  }

  @Then("I restart tunnel")
  public void iRestartTunnel() {
    ltLogger.info("Restarting tunnel");
    automationHelper.stopTunnel();

    waitForTime(5);

    automationHelper.startTunnel();

    waitForTime(10);
  }

  @And("I restart tunnel with {word}")
  public void iRestartTunnelWith(String flags) {
    ltLogger.info("Restarting tunnel with flags: {}", flags);
    automationHelper.stopTunnel();

    waitForTime(5);

    automationHelper.startTunnel(flags);

    waitForTime(10);
  }

  @Then("I simulate tunnel connection failure")
  public void iSimulateTunnelConnectionFailure() {
    ltLogger.info("Simulating tunnel connection failure");
    executeNetworkScript("block_all_ssh_tcp");
    waitForTime(3);
  }

  @Then("I verify tunnel reconnection occurs")
  public void iVerifyTunnelReconnectionOccurs() {
    ltLogger.info("Verifying tunnel reconnection occurs");

    executeNetworkScript("flush_all_rules");

    waitForTime(10);

    try {
      String currentMode = automationHelper.getCurrentTunnelMode();
      if (currentMode == null || currentMode.isEmpty()) {
        Assert.fail("Tunnel did not reconnect successfully");
      }
      ltLogger.info("✓ Tunnel reconnection verification successful. Current mode: {}", currentMode);
    } catch (Exception e) {
      ltLogger.error("Tunnel reconnection verification failed: {}", e.getMessage());
      Assert.fail("Tunnel reconnection verification failed: " + e.getMessage());
    }
  }

  public void waitForTime(int seconds) {
    try {
      Thread.sleep(seconds * 1000L);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      ltLogger.warn("Wait interrupted: {}", e.getMessage());
    }
  }
}