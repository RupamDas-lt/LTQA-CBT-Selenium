package utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mysql.cj.util.StringUtils;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static utility.FrameworkConstants.*;

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
    try {
      File file = new File(filePath);
      File parentDir = file.getParentFile();
      if (parentDir != null && !parentDir.exists()) {
        boolean directoriesCreated = parentDir.mkdirs();
        if (!directoriesCreated) {
          ltLogger.error("Failed to create the parent directory: {}", parentDir.getAbsolutePath());
        }
      }

      try (FileWriter fileWriter = new FileWriter(filePath)) {
        fileWriter.write(content);
        ltLogger.info("Response data written to file: {}", filePath);
        ltLogger.info("Response data: {}", content);
      } catch (IOException e) {
        ltLogger.error("Error writing to file: {}", e.getMessage());
      }
    } finally {
      FileLockUtility.fileLock.unlock();
    }
  }

  public File getFileWithFileLock(String filePath) {
    FileLockUtility.fileLock.lock();
    try (FileChannel channel = FileChannel.open(Paths.get(filePath), StandardOpenOption.READ);
      FileLock lock = channel.lock(0, Long.MAX_VALUE, true)) {
      File file = new File(filePath);
      ltLogger.info("Implementing file lock on: {}", filePath);
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

  public String getCurrentTimeIST(String... format) {
    String tmeFormat = format.length > 0 ? format[0] : DEFAULT_DATE_TIME_FORMAT;
    ZonedDateTime indianTime = ZonedDateTime.now(ZoneId.of(IST_TimeZone));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(tmeFormat);
    return indianTime.format(formatter);
  }

  public Duration getTimeDifference(String start, String end, String zone, String... format) {
    String tmeFormat = format.length > 0 ? format[0] : DEFAULT_DATE_TIME_FORMAT;
    ZonedDateTime startTime = ZonedDateTime.parse(start, DateTimeFormatter.ofPattern(tmeFormat));
    ZonedDateTime endTime = ZonedDateTime.parse(end, DateTimeFormatter.ofPattern(tmeFormat));
    return Duration.between(startTime, endTime);
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

  public boolean fileExists(String filePath, int retryCount, int interval) {
    int count = 0;
    while (count < retryCount) {
      File file = new File(filePath);
      boolean status = file.exists();
      if (status) {
        return true;
      }
      waitForTime(interval);
      count++;
    }
    return false;
  }

  private static void deleteDirectory(File directory) {
    if (directory.exists()) {
      File[] files = directory.listFiles();
      if (files != null) {
        for (File file : files) {
          if (file.isDirectory()) {
            deleteDirectory(file);
          } else {
            file.delete();
          }
        }
      }
      directory.delete();
    }
  }

  public JsonObject processZipFile(String zipFilePath) throws IOException {
    // Create a temporary directory
    Path tempDir = Files.createTempDirectory("zipExtract");
    JsonArray combinedJsonArray = new JsonArray();
    StringBuilder otherFileContent = new StringBuilder();
    Gson gson = new Gson();
    JsonParser parser = new JsonParser();

    try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
      ZipEntry entry;
      while ((entry = zipInputStream.getNextEntry()) != null) {
        Path filePath = tempDir.resolve(entry.getName());

        if (!filePath.normalize().startsWith(tempDir)) {
          throw new IOException("Invalid zip entry: " + entry.getName());
        }

        // Skip directories
        if (entry.isDirectory()) {
          Files.createDirectories(filePath);
        } else {
          Files.copy(zipInputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

          // Read and parse the file content using the locked readFileContent method
          File file = getFileWithFileLock(filePath.toString());
          String fileContent = new String(Files.readAllBytes(file.toPath()));

          String fileName = entry.getName().toLowerCase();
          if (fileName.endsWith(".json") || fileName.endsWith(".har")) {
            // Parse JSON or HAR files
            JsonElement jsonElement = parser.parse(fileContent);
            if (jsonElement.isJsonArray()) {
              combinedJsonArray.addAll(jsonElement.getAsJsonArray());
            } else {
              combinedJsonArray.add(jsonElement);
            }
          } else {
            // Append content of other file types (e.g., .txt)
            otherFileContent.append(fileContent).append("\n");
          }
        }
      }
    } finally {
      // Delete the temporary directory and its contents
      deleteDirectory(tempDir.toFile());
    }

    JsonObject result = new JsonObject();
    result.add("jsonData", combinedJsonArray);
    result.addProperty("otherFileContent", otherFileContent.toString());
    return result;
  }

  public JsonElement processZipFileAndExtractDataAsJson(String zipFilePath) throws IOException {
    return processZipFile(zipFilePath).get("jsonData");
  }

  public String processZipFileAndExtractDataAsString(String zipFilePath) throws IOException {
    JsonObject jsonObject = processZipFile(zipFilePath);
    JsonArray jsonArray = jsonObject.get("jsonData").getAsJsonArray();
    String otherFilesContent = jsonObject.get("otherFileContent").getAsString();
    return jsonArray.isEmpty() ? otherFilesContent : new Gson().toJson(jsonArray);
  }

  public JsonElement constructJsonFromString(String jsonString) {
    JsonElement jsonElement = JsonParser.parseString(jsonString);
    ltLogger.info("Extracted Json Element: {}", jsonElement);
    return jsonElement;
  }

  public String readDataFromDownloadedLogFile(String filePath) {
    StringBuilder content = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = reader.readLine()) != null) {
        content.append(line);
        content.append("\n");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return content.toString();
  }

  public String readFileData(String filePath) throws IOException {
    ltLogger.info("Reading file: {}", filePath);
    String content;
    if (filePath.contains(".zip"))
      content = processZipFileAndExtractDataAsString(filePath);
    else
      content = readDataFromDownloadedLogFile(filePath);
    ltLogger.info("File data: {}", content);
    return content;
  }

  public static String handleUnicodeEscapes(String input) {
    return input.replace("\\u002f", "/").replace("\\u002F", "/").replace("\\/", "/").replace("\\\\", "");
  }

  public Set<String> validateSchema(String obtainedData, String expectedJsonFilePath) {
    try {
      File schemaFile = new File(expectedJsonFilePath);
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode schemaNode = objectMapper.readTree(schemaFile);

      JsonNode apiNode = objectMapper.readTree(obtainedData);

      JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
      JsonSchema schema = schemaFactory.getSchema(schemaNode);

      Set<ValidationMessage> validationResult = schema.validate(apiNode);
      Set<String> result = new HashSet<>();

      // If there are any validation errors, return false
      if (!validationResult.isEmpty()) {
        for (ValidationMessage validationMessage : validationResult) {
          result.add(validationMessage.getMessage());
        }
      }
      ltLogger.info("Schema validation completed.");
      return result;

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String removeBasicAuthHeadersFromUrl(String url) {
    url = url.replaceAll("https?://([^@]+@)", "https://");
    return url;
  }

  private String executeFFprobeCommand(String[] command, String videoFilePath) {
    StringBuilder output = new StringBuilder();
    try {
      ProcessBuilder builder = new ProcessBuilder(command);
      builder.command().add(videoFilePath); // Add video file path to the command
      builder.redirectErrorStream(true);
      ltLogger.info("Command: {}", String.join(" ", builder.command()));
      Process process = builder.start();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          output.append(line).append("\n");
        }
      }
      process.waitFor();

    } catch (IOException | InterruptedException e) {
      throw new RuntimeException("Error executing FFprobe command: ", e);
    }
    return output.toString().trim();
  }

  public Map<String, Object> extractMetaDataOfSpecificVideoFile(String videoFilePath) {
    Map<String, Object> metadataMap = new HashMap<>();
    // Extract duration
    ltLogger.info("Extracting video duration...");
    String duration = executeFFprobeCommand(DURATION_COMMAND, videoFilePath);
    metadataMap.put(videoMetadataTypes.DURATION_IN_SECONDS.getValue(), duration);

    // Extract resolution
    ltLogger.info("Extracting video resolution...");
    String resolution = executeFFprobeCommand(RESOLUTION_COMMAND, videoFilePath);
    metadataMap.put(videoMetadataTypes.RESOLUTION.getValue(), resolution);

    // Extract codec
    ltLogger.info("Extracting video codec...");
    String codec = executeFFprobeCommand(CODEC_COMMAND, videoFilePath);
    metadataMap.put(videoMetadataTypes.CODEC.getValue(), codec);

    // Extract frame rate
    ltLogger.info("Extracting video framerate...");
    String frameRate = executeFFprobeCommand(FRAME_RATE_COMMAND, videoFilePath);
    metadataMap.put(videoMetadataTypes.FRAMERATE.getValue(), frameRate);

    // Extract bitrate
    ltLogger.info("Extracting video bitrate...");
    String bitrate = executeFFprobeCommand(BITRATE_COMMAND, videoFilePath);
    metadataMap.put(videoMetadataTypes.BITRATE.getValue(), bitrate);
    return metadataMap;
  }

}
