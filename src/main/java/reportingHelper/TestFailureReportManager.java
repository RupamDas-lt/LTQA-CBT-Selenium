package reportingHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import utility.BaseClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestFailureReportManager extends BaseClass {
  private static final String dataPath = "src/main/java/reportingHelper/dataset/testFailureAnalysis.json";

  // Holder class for lazy initialization (thread-safe by JVM)
  private static class Holder {
    static final JsonNode TEST_FAILURE_DATA = loadData();

    @SneakyThrows
    private static JsonNode loadData() {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readTree(new TestFailureReportManager().getFileWithFileLock(dataPath));
    }
  }

  // Public method to access the data (initializes on first call)
  public static JsonNode getTestFailureAnalysisData() {
    return Holder.TEST_FAILURE_DATA;
  }

  public record TestFailureReportData(String failureMessage, String category, String priority, String isKnown) {
    private static final ObjectMapper mapper = new ObjectMapper();

    public Map<String, String> toMap() {
      return mapper.convertValue(this, Map.class);
    }
  }

  public static List<Map<String, String>> getTestFailureReport(Map<String, String> failureMessageToHashKeyMap) {
    /**
     * This method will return the test failure report based on the provided failure messages.
     * The report will be a list of maps, where each map contains the following keys:
     * - "failureMessage": The failure message.
     * - "category": The category of the failure.
     * - "priority": The priority of the failure.
     * - "isKnown": Is this failure known or unknown.
     */
    List<Map<String, String>> testFailureReportDataList = new ArrayList<>();
    for (Map.Entry<String, String> entry : failureMessageToHashKeyMap.entrySet()) {
      String hashKey = entry.getValue();
      JsonNode failureData = getTestFailureAnalysisData().get(hashKey);
      if (failureData == null) {
        testFailureReportDataList.add(new TestFailureReportData(entry.getKey(), "Unknown", "p0", "false").toMap());
      } else {
        String failureMessage = entry.getKey();
        String category = failureData.get("category").asText();
        String priority = failureData.get("priority").asText();
        String isKnown = failureData.get("isKnown").asText();
        testFailureReportDataList.add(new TestFailureReportData(failureMessage, category, priority, isKnown).toMap());
      }
    }
    return testFailureReportDataList;
  }
}
