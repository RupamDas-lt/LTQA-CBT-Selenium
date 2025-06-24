package automationHelper;

import TestManagers.DriverManager;
import utility.FrameworkConstants;

import java.util.concurrent.TimeUnit;

public class LTHooks extends FrameworkConstants {

  public static void startStepContext(DriverManager driverManager, String stepName) {
    driverManager.executeScript(LAMBDA_TEST_CASE_START + "=" + stepName);
  }

  public static void endStepContext(DriverManager driverManager, String stepName) {
    driverManager.executeScript(LAMBDA_TEST_CASE_END + "=" + stepName);
  }

  public static void startStepContextWithLambdaTestExecutor(DriverManager driverManager, String stepName,
    String level) {
    String script = String.format(
      "%s: {\"action\": \"stepcontext\", \"arguments\": {\"data\": \"%s\", \"level\": \"%s\"}}",
      LAMBDA_TEST_EXECUTOR_HOOK, stepName, level);
    driverManager.executeScript(script);
  }

  public static void performKeyboardEvent(DriverManager driverManager, String keyboardEvent) {
    driverManager.executeScript(LAMBDA_PERFORM_KEYBOARD_EVENTS + ":" + keyboardEvent);
  }

  public static void setClipboard(DriverManager driverManager, String clipboardValue) {
    driverManager.executeScript(LAMBDA_SET_CLIPBOARD + ":" + clipboardValue);
  }

  public static String getClipboard(DriverManager driverManager) {
    Object object = driverManager.executeScriptAndFetchValue(LAMBDA_GET_CLIPBOARD);
    return object == null ? null : object.toString();
  }

  public static boolean isFileExist(DriverManager driverManager, String fileNameWithExtension,
    int... customRetryCount) {
    boolean fileFound = false;
    int retryCount = customRetryCount == null || customRetryCount.length == 0 ? 1 : customRetryCount[0];
    while (retryCount-- > 0 && !fileFound) {
      fileFound = Boolean.parseBoolean(
        driverManager.executeScriptAndFetchValue(String.format("%s=%s", LAMBDA_FILE_EXIST, fileNameWithExtension))
          .toString());
      if (!fileFound && retryCount > 0) {
        try {
          TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RuntimeException("Thread was interrupted during sleep", e);
        }
      }
    }
    return fileFound;
  }

}
