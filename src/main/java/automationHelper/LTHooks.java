package automationHelper;

import TestManagers.DriverManager;
import utility.FrameworkConstants;

public class LTHooks extends FrameworkConstants {
  public static void startStepContext(DriverManager driverManager, String stepName) {
    driverManager.executeScript(LAMBDA_TEST_CASE_START + "=" + stepName);
  }

  public static void endStepContext(DriverManager driverManager, String stepName) {
    driverManager.executeScript(LAMBDA_TEST_CASE_END + "=" + stepName);
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

}
