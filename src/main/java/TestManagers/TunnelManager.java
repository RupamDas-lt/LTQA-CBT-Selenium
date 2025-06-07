package TestManagers;

import DTOs.Others.TunnelInfoResponseDTO;
import automationHelper.AutomationAPIHelper;
import com.google.gson.Gson;
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

    String processedParams = params;
    if (params != null && params.contains("omit=")) {
      processedParams = processOmitFunctionality(params, tunnelFlags);
    }

    Map<String, Object> customFlags = getHashMapFromString(processedParams, "--", " ");

    tunnelFlags.putAll(customFlags);
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

  private String processOmitFunctionality(String params, Map<String, Object> tunnelFlags) {
    ltLogger.info("Processing omit functionality from params: {}", params);

    String[] paramParts = params.split("--");
    StringBuilder processedParamsBuilder = new StringBuilder();

    for (String part : paramParts) {
      part = part.trim();
      if (part.isEmpty()) {
        continue;
      }

      if (part.startsWith("omit=")) {
        String omitValue = part.substring(5); // Remove "omit="
        ltLogger.info("Processing omit flags: {}", omitValue);

        String[] flagsToOmit = omitValue.split("--");
        for (String flagToOmit : flagsToOmit) {
          flagToOmit = flagToOmit.trim();
          if (!flagToOmit.isEmpty()) {
            if (tunnelFlags.containsKey(flagToOmit)) {
              ltLogger.info("Omitting flag '{}' from tunnel start command", flagToOmit);
              tunnelFlags.remove(flagToOmit);
            } else {
              ltLogger.warn("Flag '{}' specified in omit but not found in default flags", flagToOmit);
            }
          }
        }
      } else {
        if (!processedParamsBuilder.isEmpty()) {
          processedParamsBuilder.append("--");
        }
        processedParamsBuilder.append(part);
      }
    }

    String result = processedParamsBuilder.toString();
    ltLogger.info("Processed params after omit handling: {}", result);
    return result;
  }

  @Override
  public void run() {
    runMacShellCommand(wrapCommandForShellInvocation(tunnelRunCommand));
  }

  public void startTunnel(String params) {
    // Create logs directory if it doesn't exist so that tunnel debug logs can be
    // stored
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
        try {
          TimeUnit.SECONDS.sleep(retryDelay);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
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
    ltLogger.info("Getting tunnel info details from API URL: {}", url);
    ltLogger.debug("API URL: {}", url);

    AutomationAPIHelper apiManager = new AutomationAPIHelper();
    int maxRetries = 5;
    int retryDelay = 2;

    for (int i = 0; i < maxRetries; i++) {
      try {
        String tunnelResponse = apiManager.getRequestAsString(url);
        ltLogger.info("Tunnel info API response -> {}", tunnelResponse);

        if (tunnelResponse != null && !tunnelResponse.isEmpty()) {
          try {
            Gson gson = new Gson();
            TunnelInfoResponseDTO tunnelInfo = gson.fromJson(tunnelResponse, TunnelInfoResponseDTO.class);
            ltLogger.debug("Status: {}", tunnelInfo.getStatus());
            ltLogger.debug("Data object: {}", (tunnelInfo.getData() != null ? "Present" : "NULL"));

            if (tunnelInfo.getData() != null) {
              ltLogger.debug("Mode: {}", tunnelInfo.getData().getMode());
              ltLogger.debug("SSH Connection Type: {}", tunnelInfo.getData().getSshConnType());
              ltLogger.debug("Local Proxy Port: {}", tunnelInfo.getData().getLocalProxyPort());
              ltLogger.debug("Tunnel Name: {}", tunnelInfo.getData().getTunnelName());
              ltLogger.debug("Environment: {}", tunnelInfo.getData().getEnvironment());
              ltLogger.debug("Version: {}", tunnelInfo.getData().getVersion());
              ltLogger.debug("ID: {}", tunnelInfo.getData().getId());
            }

            if ("SUCCESS".equals(tunnelInfo.getStatus()) && tunnelInfo.getData() != null) {
              ltLogger.info("Tunnel info retrieved successfully: Mode={}, SshConnType={}, Port={}",
                tunnelInfo.getData().getMode(), tunnelInfo.getData().getSshConnType(),
                tunnelInfo.getData().getLocalProxyPort());
              return tunnelInfo;
            } else {
              ltLogger.debug("Invalid tunnel info response: status='{}', data={}", tunnelInfo.getStatus(),
                tunnelInfo.getData() != null ? "present" : "null");
            }
          } catch (Exception parseException) {
            ltLogger.error("Failed to parse tunnel info response: {}", parseException.getMessage());
          }
        } else {
          ltLogger.error("Empty or null response from tunnel info API");
        }
      } catch (Exception e) {
        ltLogger.error("Exception occurred while getting tunnel info details -> {}", e.getMessage());
      }

      if (i < maxRetries - 1) {
        ltLogger.info("Retrying to get tunnel info in {} seconds...", retryDelay);
        TimeUnit.SECONDS.sleep(retryDelay);
      }
    }

    throw new RuntimeException("Failed to get tunnel info details after " + maxRetries + " attempts");
  }

  public String getCurrentTunnelMode() {
    try {
      ltLogger.info("Getting current tunnel mode...");
      TunnelInfoResponseDTO tunnelInfo = getTunnelInfoDetails();
      String mode = tunnelInfo.getData().getMode();
      ltLogger.debug("Current tunnel mode: {}", mode);
      return mode;
    } catch (Exception e) {
      ltLogger.error("Failed to get current tunnel mode: {}", e.getMessage());
      throw new RuntimeException("Failed to get current tunnel mode", e);
    }
  }

  public String getCurrentSshConnectionType() {
    try {
      TunnelInfoResponseDTO tunnelInfo = getTunnelInfoDetails();
      return tunnelInfo.getData().getSshConnType();
    } catch (Exception e) {
      ltLogger.error("Failed to get current SSH connection type: {}", e.getMessage());
      throw new RuntimeException("Failed to get current SSH connection type", e);
    }
  }

  public String getTunnelLocalProxyPort() {
    try {
      TunnelInfoResponseDTO tunnelInfo = getTunnelInfoDetails();
      return tunnelInfo.getData().getLocalProxyPort();
    } catch (Exception e) {
      ltLogger.error("Failed to get tunnel local proxy port: {}", e.getMessage());
      throw new RuntimeException("Failed to get tunnel local proxy port", e);
    }
  }

  public boolean verifyTunnelMode(String expectedMode) {
    try {
      String currentMode = getCurrentTunnelMode();
      ltLogger.info("Expected mode: {}, Current mode: {}", expectedMode, currentMode);
      return expectedMode.equalsIgnoreCase(currentMode);
    } catch (Exception e) {
      ltLogger.error("Failed to verify tunnel mode: {}", e.getMessage());
      return false;
    }
  }

  public boolean verifySshConnectionType(String expectedSshConnType) {
    try {
      String currentSshConnType = getCurrentSshConnectionType();
      ltLogger.info("Expected SSH connection type: {}, Current SSH connection type: {}", expectedSshConnType,
        currentSshConnType);
      return expectedSshConnType.equalsIgnoreCase(currentSshConnType);
    } catch (Exception e) {
      ltLogger.error("Failed to verify SSH connection type: {}", e.getMessage());
      return false;
    }
  }

  public int getCurrentTunnelPort() {
    try {
      TunnelInfoResponseDTO tunnelInfo = getTunnelInfoDetails();
      String mode = tunnelInfo.getData().getMode();
      String sshConnType = tunnelInfo.getData().getSshConnType();

      if ("ssh".equalsIgnoreCase(mode)) {
        if ("over_22".equalsIgnoreCase(sshConnType)) {
          return 22;
        } else if ("over_443".equalsIgnoreCase(sshConnType) || "over_ws".equalsIgnoreCase(sshConnType)) {
          return 443;
        }
      } else if ("tcp".equalsIgnoreCase(mode) || "ws".equalsIgnoreCase(mode)) {
        return 443;
      }

      ltLogger.warn("Unable to determine port for mode: {} and sshConnType: {}", mode, sshConnType);
      return -1;
    } catch (Exception e) {
      ltLogger.error("Failed to get current tunnel port: {}", e.getMessage());
      return -1;
    }
  }

}
