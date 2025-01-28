package utility;

import com.mysql.cj.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Random;

public class BaseClass {

  private final Logger ltLogger = LogManager.getLogger(BaseClass.class);

  public static String getRandomLowerUpperCaseOfSpecificString(String string) {
    Random random = new Random();
    char[] chars = string.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if (random.nextBoolean())
        chars[i] = Character.toLowerCase(chars[i]);
    }
    return String.valueOf(chars);
  }

  public HashMap<String, Object> getHashMapFromString(String string, String... separator) {
    HashMap<String, Object> hashmapList = new HashMap<>();
    if (StringUtils.isNullOrEmpty(string)) {
      return hashmapList;
    }

    String actualSeparator = separator.length > 0 ? separator[0] : ",";
    String[] list = string.split(actualSeparator);
    for (String s : list) {
      String[] keyValue = s.split("=");
      if (keyValue.length == 2) {
        hashmapList.put(keyValue[0].trim(), keyValue[1].trim());
      }
    }
    ltLogger.info("Retrieved hashmap from string: {} is: {}", string, hashmapList);
    return hashmapList;
  }
}
