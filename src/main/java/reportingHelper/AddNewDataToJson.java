package reportingHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import factory.SoftAssertionMessages;
import factory.SoftAssertionMessagesAccessibility;
import factory.SoftAssertionMessagesFalcon;
import utility.BaseClass;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static utility.FrameworkConstants.PRODUCT_NAME;
import static utility.FrameworkConstants.REWRITE_EXISTING_DATA;

public class AddNewDataToJson extends BaseClass {
    private static String FILE_PATH;
    private static final String FILE_PATH_SELENIUM = "src/main/java/reportingHelper/dataset/testFailureAnalysis.json";
    private static final String FILE_PATH_FALCON = "src/main/java/reportingHelper/dataset/testFailureAnalysisFalcon.json";
    private static final String FILE_PATH_ACCESSIBILITY = "src/main/java/reportingHelper/dataset/testFailureAnalysisAccessibility.json";

    public static void updateJsonFile(Map<String, String> inputMap, boolean rewriteExisting) throws Exception {
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

            // If key exists and rewriteExisting is false, skip updating that key
            if (!rewriteExisting && rootNode.has(key)) {
                continue;
            }

            ObjectNode contentNode = mapper.createObjectNode();
            contentNode.put("message", messageValue);
            contentNode.put("category", "artefacts|api|ui|performance|security|accessibility");
            contentNode.put("sub_category", "custom_sub_category_value");
            contentNode.put("priority", "p0|p1|p2|p3|p4");
            contentNode.put("isKnown", "true|false");

            rootNode.set(key, contentNode);
        }

        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, rootNode);
    }

    private static Map.Entry<String, Class<? extends Enum<?>>> getProductConfiguration(String productName) {
        return switch (productName) {
            case "falcon" -> Map.entry(FILE_PATH_FALCON, SoftAssertionMessagesFalcon.class);
            case "accessibility" -> Map.entry(FILE_PATH_ACCESSIBILITY, SoftAssertionMessagesAccessibility.class);
            default -> Map.entry(FILE_PATH_SELENIUM, SoftAssertionMessages.class);
        };
    }

    private static HashMap<String, String> processMessages(Class<? extends Enum<?>> messageEnumClass) {
        HashMap<String, String> map = new HashMap<>();
        try {
            Enum<?>[] messages = messageEnumClass.getEnumConstants();

            Method getValueMethod = messageEnumClass.getMethod("getValue");

            for (Enum<?> message : messages) {
                String value = (String) getValueMethod.invoke(message);
                map.put(stringToSha256Hex(value), value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process messages for enum class: " + messageEnumClass.getSimpleName(), e);
        }
        return map;
    }

    public static void main(String[] args) throws Exception {
        // Get configuration from system properties with defaults
        String productName = System.getProperty(PRODUCT_NAME, "selenium").toLowerCase();
        boolean rewriteExisting = Boolean.parseBoolean(System.getProperty(REWRITE_EXISTING_DATA, "false"));

        // Determine file path and enum class based on product
        Map.Entry<String, Class<? extends Enum<?>>> config = getProductConfiguration(productName);
        FILE_PATH = config.getKey();

        // Process messages
        HashMap<String, String> assertionErrorToHashKeyMap = processMessages(config.getValue());

        updateJsonFile(assertionErrorToHashKeyMap, rewriteExisting);
    }
}
