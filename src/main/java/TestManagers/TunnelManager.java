package TestManagers;

import DTOs.Others.TunnelInfoResponseDTO;
import automationHelper.AutomationAPIHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.BaseClass;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static utility.EnvSetup.*;
import static utility.FrameworkConstants.*;

public class TunnelManager extends BaseClass implements Runnable {

  // Tunnel Constants
  private static final Map<String, String> TUNNEL_BINARY_PATHS = Map.of("win", "./LT_Win/LT.exe", "mac", "./LT_Mac/LT",
    "linux", "./LT_Linux/LT");
  private static final String[] TUNNEL_MODES = { "tcp", "ssh" };
  private final Logger ltLogger = LogManager.getLogger(TunnelManager.class);
  private static final String commandToPushConsoleLogsToFile = " > logs/tunnelLogs/%s-debug-logs.log 2>&1";

  // Tunnel Flag Details
  private final String tunnelBinaryPath = getTunnelBinaryPath();
  private final String customTunnelFlagsString = System.getProperty(CUSTOM_TUNNEL_FLAGS, "");
  private final Map<String, Object> defaultTunnelFlags;
  private final String availableOpenPort;
  private String tunnelName = getRandomAlphaNumericString(30);

  // Variables
  private String tunnelRunCommand;
  private Thread thread;

  public TunnelManager() {
    availableOpenPort = getOpenPort();
    String logFilePath = "logs/tunnelLogs/" + tunnelName + ".log";
    defaultTunnelFlags = new HashMap<>(
      Map.of("key", testAccessKey.get(), "user", testUserName.get(), "tunnelName", tunnelName, "maxDataConnections",
        "1", "verbose", "", "infoAPIPort", availableOpenPort, "logFile", logFilePath, "mitm", ""));
    if (TEST_ENV.contains("stage")) {
      defaultTunnelFlags.put("env", TEST_ENV);
    }
  }

  private static String getTunnelBinaryPath() {
    String osKey = System.getProperty(OS_NAME).toLowerCase();
    return TUNNEL_BINARY_PATHS.entrySet().stream().filter(entry -> osKey.contains(entry.getKey()))
      .map(Map.Entry::getValue).findFirst().orElseThrow(() -> new IllegalStateException("Unsupported OS: " + osKey));
  }

  private String constructTunnelRunCommand(String params) {
    ltLogger.info("Given custom tunnel params: {}", params);
    Map<String, Object> tunnelFlags = new HashMap<>(defaultTunnelFlags);
    tunnelFlags.putAll(getHashMapFromString(params, "--", " "));
    tunnelFlags.putAll(getHashMapFromString(customTunnelFlagsString, "--", " "));

    String command = tunnelBinaryPath + " " + tunnelFlags.entrySet().stream().map(
      entry -> "--" + entry.getKey() + ((entry.getValue() != null && !entry.getValue().toString().isEmpty()) ?
        " " + entry.getValue() :
        "")).collect(Collectors.joining(" "));

    tunnelName = tunnelFlags.get("tunnelName").toString();
    TEST_TUNNEL_NAME.set(tunnelName);
    TEST_TUNNEL_INFO_API_PORT.set(availableOpenPort);
    command = String.format(command + commandToPushConsoleLogsToFile, tunnelName);
    ltLogger.info("Tunnel run command: {}", command);
    TEST_REPORT.get().put("tunnel_start_command", command);
    return command;
  }

  @Override
  public void run() {
    runMacShellCommand(wrapCommandForShellInvocation(tunnelRunCommand));
  }

  public void startTunnel(String params) {
    // Create logs directory if it doesn't exist so that tunnel debug logs can be stored
    String tunnelLogsDirectory = "logs/tunnelLogs";
    createDirectoryIfNotExists(tunnelLogsDirectory);

    tunnelRunCommand = constructTunnelRunCommand(params);
    TUNNEL_START_COMMAND.set(tunnelRunCommand);
    ltLogger.info("Tunnel started with command: {}", tunnelRunCommand);
    if (thread == null) {
      thread = new Thread(this, tunnelName);
      thread.start();
    }
  }

  public String state() {
    return thread.getState().toString();
  }

  @SneakyThrows
  public boolean checkTunnelInfoAPIServerIsInitiated() {
    boolean isAPIServerStarted = true;
    for (int i = 0; i < 5; i++) {
      if (getCommandStdOutput().contains("Failed to start api server on port")) {
        ltLogger.warn("Tunnel info API server status: {}", getCommandStdOutput());
        ltLogger.info("APIServer is not started with {} port. So re-launching the tunnel with new port",
          availableOpenPort);
        isAPIServerStarted = false;
        clearGetCommandStdOutput();
        break;
      }
      TimeUnit.SECONDS.sleep(3);
    }
    return isAPIServerStarted;
  }

  @SneakyThrows
  public boolean getTunnelStatusFromAPIServer() {
    String url = LOCAL_HOST_URL + availableOpenPort + TUNNEL_INFO_API_PATH;
    ltLogger.info("Tunnel info API URL: {}", url);

    AutomationAPIHelper apiManager = new AutomationAPIHelper();
    int maxRetries = 12;
    int retryDelay = 5;

    return IntStream.range(0, maxRetries).mapToObj(i -> {
      try {
        String tunnelResponse = apiManager.getRequestAsString(url);
        ltLogger.info("Tunnel info API server response -> {}", tunnelResponse);
        if (tunnelResponse.contains("\"status\":\"SUCCESS\"") && tunnelResponse.contains(tunnelName)) {
          return true;
        }
      } catch (Exception e) {
        ltLogger.error("Exception Occurred -> {}", e.getMessage());
      }

      if (i < maxRetries - 1) {
        ltLogger.info("Looks like Tunnel is not started. Sleeping for {} seconds...", retryDelay);
        waitForTime(retryDelay);
      }
      return false;
    }).filter(Boolean::booleanValue).findFirst().orElseThrow(() -> new RuntimeException(
      "Tunnel is not started even after waiting for 60 seconds.\nTunnel logs: " + getCommandStdOutput()));
  }

  public boolean isTunnelStarted() {
    return getCommandStdOutput().contains("You can start testing now");
  }

  private ProcessBuilder stopTunnelCLI() {
    List<String> stopTunnelCLICommand = Arrays.asList("/bin/sh", "-c",
      "ps -ef | grep '" + tunnelName + "' | grep -v grep | awk '{print $2}' | xargs kill -9");
    this.ltLogger.info("Tunnel Stop Command :- {}", stopTunnelCLICommand);
    return new ProcessBuilder(stopTunnelCLICommand);
  }

  @SneakyThrows
  public void stopTunnel() {
    ProcessBuilder stopTunnelCLICommand = stopTunnelCLI();
    Process process = stopTunnelCLICommand.start();
    ltLogger.info("Reading tunnel stop process error output (if any) ...");

    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
      String line;
      while ((line = errorReader.readLine()) != null) {
        ltLogger.error(line);
      }
    }
    int exitCode = process.waitFor();
    ltLogger.info("Process exited with code :- {}", exitCode);
  }

  @SneakyThrows
  public TunnelInfoResponseDTO getTunnelInfoDetails() {
    String url = LOCAL_HOST_URL + availableOpenPort + TUNNEL_INFO_API_PATH;
    AutomationAPIHelper apiManager = new AutomationAPIHelper();
    int maxRetries = 5;
    int retryDelay = 2;

    for (int i = 0; i < maxRetries; i++) {
      try {
        String tunnelResponse = apiManager.getRequestAsString(url);

        if (tunnelResponse != null && !tunnelResponse.isEmpty()) {
          try {
            TunnelInfoResponseDTO tunnelInfo = convertJsonStringToPojo(tunnelResponse,
              new TypeToken<TunnelInfoResponseDTO>() {
              });

            com.fasterxml.jackson.databind.ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            String prettyJson = mapper.writeValueAsString(tunnelInfo);
            ltLogger.info("Tunnel info API response: {}", prettyJson);

            if ("SUCCESS".equals(tunnelInfo.getStatus()) && tunnelInfo.getData() != null) {
              return tunnelInfo;
            }
          } catch (Exception parseException) {
            ltLogger.error("Failed to parse tunnel info response: {}", parseException.getMessage());
          }
        }
      } catch (Exception e) {
        ltLogger.error("Exception occurred while getting tunnel info details -> {}", e.getMessage());
      }

      if (i < maxRetries - 1) {
        ltLogger.info("Retrying to get tunnel info in {} seconds...", retryDelay);
        waitForTime(retryDelay);
      }
    }

    throw new RuntimeException("Failed to get tunnel info details after " + maxRetries + " attempts");
  }

  public String getCurrentTunnelMode() {
    try {
      ltLogger.info("Getting current tunnel mode...");
      TunnelInfoResponseDTO tunnelInfo = getTunnelInfoDetails();
      String mode = tunnelInfo.getData().getMode();
      ltLogger.info("Successfully retrieved current tunnel mode: {}", mode);
      return mode;
    } catch (Exception e) {
      ltLogger.error("Failed to get current tunnel mode: {}", e.getMessage());
      throw new RuntimeException("Failed to get current tunnel mode", e);
    }
  }

  public String getCurrentSshConnectionType() {
    try {
      ltLogger.info("Getting current SSH connection type...");
      TunnelInfoResponseDTO tunnelInfo = getTunnelInfoDetails();
      String sshConnType = tunnelInfo.getData().getSshConnType();
      ltLogger.info("Successfully retrieved SSH connection type: {}", sshConnType);
      return sshConnType;
    } catch (Exception e) {
      ltLogger.error("Failed to get current SSH connection type: {}", e.getMessage());
      throw new RuntimeException("Failed to get current SSH connection type", e);
    }
  }

  public String getTunnelLocalProxyPort() {
    try {
      ltLogger.info("Getting tunnel local proxy port...");
      TunnelInfoResponseDTO tunnelInfo = getTunnelInfoDetails();
      String localProxyPort = tunnelInfo.getData().getLocalProxyPort();
      ltLogger.info("Successfully retrieved tunnel local proxy port: {}", localProxyPort);
      return localProxyPort;
    } catch (Exception e) {
      ltLogger.error("Failed to get tunnel local proxy port: {}", e.getMessage());
      throw new RuntimeException("Failed to get tunnel local proxy port", e);
    }
  }

  public int getCurrentTunnelPort() {
    try {
      ltLogger.info("Getting current tunnel port...");
      TunnelInfoResponseDTO tunnelInfo = getTunnelInfoDetails();
      String mode = tunnelInfo.getData().getMode();
      String sshConnType = tunnelInfo.getData().getSshConnType();

      int port = -1;
      if ("ssh".equalsIgnoreCase(mode)) {
        if ("over_22".equalsIgnoreCase(sshConnType)) {
          port = 22;
        } else if ("over_443".equalsIgnoreCase(sshConnType) || "over_ws".equalsIgnoreCase(sshConnType)) {
          port = 443;
        }
      } else if ("tcp".equalsIgnoreCase(mode) || "ws".equalsIgnoreCase(mode)) {
        port = 443;
      }

      if (port != -1) {
        ltLogger.info("Successfully determined tunnel port: {} for mode: {} and sshConnType: {}", port, mode,
          sshConnType);
      } else {
        ltLogger.warn("Unable to determine port for mode: {} and sshConnType: {}", mode, sshConnType);
      }

      return port;
    } catch (Exception e) {
      ltLogger.error("Failed to get current tunnel port: {}", e.getMessage());
      return -1;
    }
  }

}
