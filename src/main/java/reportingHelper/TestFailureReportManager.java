package reportingHelper;

import automationHelper.AutomationAPIHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.cj.util.StringUtils;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.BaseClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utility.EnvSetup.*;
import static utility.FrameworkConstants.PUT_CUSTOM_DATA_TO_SUMO_PAYLOAD;
import static utility.FrameworkConstants.TEST_ATTEMPT;

public class TestFailureReportManager extends BaseClass {

    private final Logger ltLogger = LogManager.getLogger(TestFailureReportManager.class);

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

    public record TestFailureReportData(String failureMessage, String category, String subCategory, String priority,
                                        String isKnown) {
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
                /**
                 * This method will handle the unexpected failure report based on the provided failure messages.
                 */
                testFailureReportDataList.add(
                        new TestFailureReportData(entry.getKey(), "unknown-error", "session-creation-or-unexpected-error", "p0",
                                "false").toMap());
            } else {
                String failureMessage = entry.getKey();
                String category = failureData.get("category").asText();
                String subCategory = failureData.get("sub_category").asText();
                String priority = failureData.get("priority").asText();
                String isKnown = failureData.get("isKnown").asText();
                testFailureReportDataList.add(
                        new TestFailureReportData(failureMessage, category, subCategory, priority, isKnown).toMap());
            }
        }
        return testFailureReportDataList;
    }

    private HashMap<String, Object> getTestErrorDataPayload(Map<String, String> errorData) {
        HashMap<String, Object> payload = new HashMap<>(errorData);
        payload.put("test_session_id", TEST_SESSION_ID.get());
        payload.put("client_test_session_id", CLIENT_SESSION_ID.get());
        payload.put("test_env", TEST_ENV);
        payload.put("caps", TEST_CAPS_MAP.get());
        payload.put("attempt", System.getProperty(TEST_ATTEMPT, "first"));
        payload.put("message_identifier", "LTQA-CBT-Selenium-Test-Failure-Reports");

        String customDataFromEnvVar = System.getProperty(PUT_CUSTOM_DATA_TO_SUMO_PAYLOAD, "");
        if (!StringUtils.isNullOrEmpty(customDataFromEnvVar)) {
            HashMap<String, Object> customDataMapFromCLI = getHashMapFromString(customDataFromEnvVar);
            payload.put("custom_data_from_cli", customDataMapFromCLI);
        }
        ltLogger.info("Finalized error data payload to push to Sumo Logic: {}", payload);
        return payload;
    }

    public void publishTestFailureReportToSumoLogic(Map<String, String> failureMessageToErrorHashKeyMap) {
        /**
         * This method will send the test failure report to Sumo Logic.
         * It will create a JSON object with the failure messages and their corresponding hash keys.
         */
        ltLogger.info("Test error message to hashkey map: {}", failureMessageToErrorHashKeyMap);
        if (!failureMessageToErrorHashKeyMap.isEmpty()) {

            // Update Existing test report with test failure report
            List<Map<String, String>> testFailureReport = getTestFailureReport(failureMessageToErrorHashKeyMap);
            TEST_REPORT.get().put("test_failure_report", testFailureReport);
            ltLogger.info("Test failure report: {}", testFailureReport);

            // Push each failure data separately to Sumo Logic, remove this if dashboard can be prepared from the entire report
            for (Map<String, String> map : testFailureReport) {
                new AutomationAPIHelper().sendCustomDataToSumo(getTestErrorDataPayload(map));
            }

        } else {
            ltLogger.info("No test failure report to push.");
        }

    }
}
