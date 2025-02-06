package utility;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mysql.cj.util.StringUtils;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class BaseClass {

  private final Logger ltLogger = LogManager.getLogger(BaseClass.class);
  private final StringBuilder commandErrOutput = new StringBuilder();
  private StringBuilder commandStdOutput = new StringBuilder();

  public static String getRandomLowerUpperCaseOfSpecificString(String string) {
    Random random = new Random();
    char[] chars = string.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if (random.nextBoolean())
        chars[i] = Character.toLowerCase(chars[i]);
    }
    return String.valueOf(chars);
  }

  public static <T> T convertJsonStringToPojo(String jsonString, TypeToken<T> type) {
    Object object = null;
    try {
      object = new Gson().fromJson(jsonString, type.getType());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return (T) object;
  }

  public String getRandomAlphaNumericString(int length) {
    String alphaNumericChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    Random random = new Random();
    StringBuilder strBuilder = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int randomIndex = random.nextInt(alphaNumericChars.length());
      strBuilder.append(alphaNumericChars.charAt(randomIndex));
    }
    return strBuilder.toString();
  }

  public void runMacShellCommand(String command) {
    String s = null;
    try {
      Process p = Runtime.getRuntime().exec(command);
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
      BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
      while ((s = stdInput.readLine()) != null) {
        commandStdOutput.append(s);
        ltLogger.info("standard command output :- {} ", s);
      }
      while ((s = stdError.readLine()) != null) {
        commandErrOutput.append(s);
        ltLogger.info("Command error output :- {} ", s);
      }
    } catch (IOException e) {
      ltLogger.info("exception happened - here's what I know: ");
      ltLogger.info(e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
      ltLogger.info(e.getMessage());
    }
  }

  public HashMap<String, Object> getHashMapFromString(String string, String... separators) {
    HashMap<String, Object> hashmapList = new HashMap<>();
    if (StringUtils.isNullOrEmpty(string)) {
      return hashmapList;
    }

    String actualSeparator = separators.length > 0 ? separators[0] : ",";
    String keyValueSeparator = separators.length > 1 ? separators[1] : "=";
    String[] list = string.split(actualSeparator);
    for (String s : list) {
      String[] keyValue = s.split(keyValueSeparator);
      if (keyValue.length == 2) {
        hashmapList.put(keyValue[0].trim(), keyValue[1].trim());
      }
      if (keyValue.length == 1) {
        hashmapList.put(keyValue[0].trim(), null);
      }
    }
    ltLogger.info("Retrieved hashmap from string: {} is: {}", string, hashmapList);
    return hashmapList;
  }

  @SneakyThrows
  public String getOpenPort() {
    ServerSocket serverSocket = new ServerSocket(0);
    serverSocket.close();
    return String.valueOf(serverSocket.getLocalPort());
  }

  public String getCommandStdOutput() {
    return commandStdOutput.toString();
  }

  public void clearGetCommandStdOutput() {
    commandStdOutput = new StringBuilder();
  }

  public String createStringBodyFromHashMap(HashMap<String, String> hashmap) {
    StringBuilder stringBuilder = new StringBuilder("{");
    hashmap.forEach((key, value) -> stringBuilder.append("\"").append(key).append("\":\"").append(value).append("\","));
    // Remove the trailing comma if the map is not empty
    if (!hashmap.isEmpty()) {
      stringBuilder.setLength(stringBuilder.length() - 1);
    }
    stringBuilder.append("}");
    return stringBuilder.toString();
  }

  public void waitForTime(int seconds) {
    try {
      TimeUnit.SECONDS.sleep(seconds);
      ltLogger.info("Sleeping for {} seconds", seconds);
    } catch (InterruptedException e) {
      ltLogger.error("Unexpected error while waiting for sleep {}", e.getMessage());
    }
  }
}
