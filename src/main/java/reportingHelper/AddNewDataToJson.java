package reportingHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import factory.SoftAssertionMessages;
import utility.BaseClass;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AddNewDataToJson extends BaseClass {
  private static final String FILE_PATH = "src/main/java/reportingHelper/dataset/testFailureAnalysis.json";

  public static void updateJsonFile(Map<String, String> inputMap) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    File jsonFile = new File(FILE_PATH);

    ObjectNode rootNode;

    if (jsonFile.exists()) {
      rootNode = (ObjectNode) mapper.readTree(jsonFile);
    } else {
      rootNode = mapper.createObjectNode();
    }

    for (Map.Entry<String, String> entry : inputMap.entrySet()) {
      String key = entry.getKey();
      String messageValue = entry.getValue();

      if (key == null || messageValue == null) {
        System.err.println("Skipping invalid entry, key or value is null");
        continue;
      }

      ObjectNode contentNode = mapper.createObjectNode();
      contentNode.put("message", messageValue);
      contentNode.put("category", "artefacts|api|ui|performance|security|accessibility");
      contentNode.put("priority", "p0|p1|p2|p3|p4");

      rootNode.set(key, contentNode);
    }

    mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, rootNode);
  }

  public static void main(String[] args) throws Exception {
    HashMap<String, String> assertionErrorToHashKeyMap = new HashMap<>();
    for (SoftAssertionMessages message : SoftAssertionMessages.values()) {
      String hashKey = stringToSha256Hex(message.getValue());
      assertionErrorToHashKeyMap.put(hashKey, message.getValue());
    }
    updateJsonFile(assertionErrorToHashKeyMap);
  }
}
