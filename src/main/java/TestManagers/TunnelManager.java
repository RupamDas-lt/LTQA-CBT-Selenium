package TestManagers;

import automationHelper.AutomationAPIHelper;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.BaseClass;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
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
        "1", "verbose", "", "mode", TUNNEL_MODES[new Random().nextInt(TUNNEL_MODES.length)], "infoAPIPort",
        availableOpenPort, "logFile", logFilePath, "mitm", ""));
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
    ltLogger.info("Tunnel run command: {}", command);
    return command;
  }

  @Override
  public void run() {
    runMacShellCommand(tunnelRunCommand);
  }

  public void startTunnel(String params) {
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

}
