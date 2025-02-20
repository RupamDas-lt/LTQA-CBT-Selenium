package utility;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mysql.cj.util.StringUtils;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class BaseClass {

  private static final Map<Class<?>, Function<String, Object>> typeConverters = Map.of(String.class, v -> v,
    String[].class, v -> new String[] { v }, Integer.class, v -> v.length(), Boolean.class,
    v -> new Random().nextBoolean());

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

    try {
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
    } catch (Exception e) {
      throw new RuntimeException(
        "Exception occurred while parsing string: " + string + " to hashmap with separator: " + string, e);
    }
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

  public void writeStringToFile(String filePath, String content) {
    FileLockUtility.fileLock.lock();
    try (FileChannel channel = FileChannel.open(Paths.get(filePath), StandardOpenOption.WRITE,
      StandardOpenOption.CREATE); FileLock lock = channel.lock(); FileWriter fileWriter = new FileWriter(filePath)) {
      File file = new File(filePath);
      File parentDir = file.getParentFile();
      if (parentDir != null && !parentDir.exists()) {
        boolean directoriesCreated = parentDir.mkdirs();
        if (!directoriesCreated) {
          ltLogger.error("Failed to create the parent directory: {}", parentDir.getAbsolutePath());
        }
      }
      fileWriter.write(content);
      ltLogger.info("Response data written to file: {}", filePath);
      ltLogger.info("Response data: {}", content);
    } catch (IOException e) {
      ltLogger.error("Error writing to file: {}", e.getMessage());
    } finally {
      FileLockUtility.fileLock.unlock();
    }
  }

  public File readFileContent(String filePath) {
    FileLockUtility.fileLock.lock();
    try (FileChannel channel = FileChannel.open(Paths.get(filePath), StandardOpenOption.READ);
      FileLock lock = channel.lock(0, Long.MAX_VALUE, true)) {
      File file = new File(filePath);
      ltLogger.info("Reading file: {}", filePath);
      ltLogger.info("File status: {}", file.exists());
      return file;
    } catch (IOException e) {
      ltLogger.error("Error reading file: {}", e.getMessage());
      throw new RuntimeException(e);
    } finally {
      FileLockUtility.fileLock.unlock();
    }
  }

  public String constructTimeZoneFromTimeOffset(String timeOffset) {
    int offset = Math.abs(Integer.parseInt(timeOffset));
    StringBuilder timezoneHours = new StringBuilder(String.valueOf(offset / 60));
    StringBuilder timezoneMinutes = new StringBuilder(String.valueOf(offset % 60));
    while (timezoneHours.length() < 2) {
      timezoneHours.insert(0, "0");
    }
    while (timezoneMinutes.length() < 2) {
      timezoneMinutes.insert(0, "0");
    }
    String UTC;
    if (Integer.parseInt(timeOffset) > 0)
      UTC = "UTC-";
    else
      UTC = "UTC+";
    String timeZone = UTC + timezoneHours + ":" + timezoneMinutes;
    ltLogger.info("Retrieved Time Zone : {} from offset value: {}", timeZone, timeOffset);
    return timeZone;
  }

  public void insertToMapWithRandom(Map<String, Object> map, String key, String value, Class<?>... returnTypes) {
    Random random = new Random();
    if (map == null || key == null || value == null || returnTypes == null || returnTypes.length == 0) {
      throw new IllegalArgumentException("Invalid input parameters.");
    }
    Class<?> selectedType = returnTypes[random.nextInt(returnTypes.length)];
    Object returnValue = typeConverters.getOrDefault(selectedType, v -> {
      throw new IllegalArgumentException("Unsupported type: " + selectedType);
    }).apply(value);

    map.put(key, returnValue);
  }
}
