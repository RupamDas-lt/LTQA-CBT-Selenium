package automationHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.BaseClass;

import java.util.HashMap;
import java.util.Random;

import static utility.EnvSetup.*;
import static utility.FrameworkConstants.CUSTOM_TUNNEL_FLAGS;
import static utility.FrameworkConstants.OS_NAME;

public class TunnelManager extends BaseClass implements Runnable {

  //  Constants
  private static final String tunnelBinaryParentDir = "./LT_Tunnel_Binary";
  private static final String tunnelBinaryPathWin = "/Windows/LT.exe";
  private static final String tunnelBinaryPathMac = "/Mac/LT";
  private static final String tunnelBinaryPathLinux = "/Linux/LT";
  private static final String tunnelBinaryPath = getTunnelBinaryPath();
  private static final String[] TUNNEL_MODES = new String[] { "tcp", "ssh" };
  private final Logger ltLogger = LogManager.getLogger(TunnelManager.class);
  private final String TUNNEL_NAME = getRandomAlphaNumericString(30);
  private final String LOG_FILE_PATH = "logs/tunnelLogs/" + TUNNEL_NAME + ".log";
  private final HashMap<String, Object> defaultTunnelFlagsMap;

  // Tunnel flags
  private final HashMap<String, Object> tunnelFlagsMap = new HashMap<>();
  private final String customTunnelFlagsString = System.getProperty(CUSTOM_TUNNEL_FLAGS, "");
  // variables
  private String tunnelRunCommand;

  TunnelManager() {
    defaultTunnelFlagsMap = new HashMap<>() {{
      put("key", testAccessKey.get());
      put("user", testUserName.get());
      put("tunnelName", TUNNEL_NAME);
      put("maxDataConnections", "1");
      put("verbose", null);
      put("mode", TUNNEL_MODES[new Random().nextInt(TUNNEL_MODES.length)]);
      put("infoAPIPort", getOpenPort());
      put("logFile", LOG_FILE_PATH);
      put("mitm", null);
    }};
    if (TEST_ENV.contains("stage"))
      defaultTunnelFlagsMap.put("env", TEST_ENV);
  }

  private static String getTunnelBinaryPath() {
    String clientOS = System.getProperty(OS_NAME).toLowerCase();
    String tunnelBinaryPath = null;
    if (clientOS.toLowerCase().contains("win"))
      tunnelBinaryPath = tunnelBinaryParentDir + tunnelBinaryPathWin;
    else if (clientOS.toLowerCase().contains("mac"))
      tunnelBinaryPath = tunnelBinaryParentDir + tunnelBinaryPathMac;
    else if (clientOS.toLowerCase().contains("linux"))
      tunnelBinaryPath = tunnelBinaryParentDir + tunnelBinaryPathLinux;
    return tunnelBinaryPath;
  }

  private String constructTunnelRunCommand(String params) {
    StringBuilder command = new StringBuilder();
    tunnelFlagsMap.putAll(defaultTunnelFlagsMap);
    HashMap<String, Object> getTunnelFlagsMap = getHashMapFromString(params, "--", " ");
    for (String key : getTunnelFlagsMap.keySet()) {
      tunnelFlagsMap.put(key.trim(), getTunnelFlagsMap.get(key).toString().trim());
    }
    HashMap<String, Object> getCustomTunnelFlagsMap = getHashMapFromString(customTunnelFlagsString, "--", " ");
    for (String key : getCustomTunnelFlagsMap.keySet()) {
      tunnelFlagsMap.put(key.trim(), getCustomTunnelFlagsMap.get(key).toString().trim());
    }
    command.append(tunnelBinaryPath);
    ltLogger.info("Tunnel flags: {}", tunnelFlagsMap);
    for (String key : tunnelFlagsMap.keySet()) {
      command.append(" ").append("--").append(key);
      if (tunnelFlagsMap.get(key) != null)
        command.append(" ").append(tunnelFlagsMap.get(key).toString().trim());
    }
    String commandString = command.toString();
    ltLogger.info("Tunnel run command: {}", commandString);
    return commandString;
  }

  @Override
  public void run() {
    runMacShellCommand(tunnelRunCommand);
  }

  public void startTunnel(String params, int retryCount) {
    tunnelRunCommand = constructTunnelRunCommand(params);
    TUNNEL_START_COMMAND.set(tunnelRunCommand);
    ltLogger.info("Tunnel started with command: {}", tunnelRunCommand);
  }
}
