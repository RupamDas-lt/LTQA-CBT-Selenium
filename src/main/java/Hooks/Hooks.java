package Hooks;

import automationHelper.AutomationAPIHelper;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.plugin.event.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.CustomSoftAssert;
import utility.EnvSetup;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static utility.EnvSetup.TEST_REPORT;
import static utility.FrameworkConstants.COMPLETED;
import static utility.FrameworkConstants.FAILED;

public class Hooks {
  private final Logger ltLogger = LogManager.getLogger(Hooks.class);
  private final AutomationAPIHelper apiHelper = new AutomationAPIHelper();
  private String errorMessage = "";
  private String scenarioName;
  private String testStatus = "passed";

  private void resetEnvironment() {
    CustomSoftAssert softAssert = new CustomSoftAssert();
    EnvSetup.SOFT_ASSERT.set(softAssert);
    EnvSetup.TEST_SESSION_ID.set("");
    TEST_REPORT.set(new HashMap<>());
    EnvSetup.TEST_ERR_REPORT.set(new HashMap<>());
    EnvSetup.TEST_VERIFICATION_DATA.set(new HashMap<>());
  }

  @Before(order = 1)
  public void beforeScenario() {
    if (System.getProperty("ENV") == null)
      throw new RuntimeException("ENV not set");
    resetEnvironment();
  }

  private String getStackTrace(Throwable error) {
    StringWriter sw = new StringWriter();
    error.printStackTrace(new PrintWriter(sw));
    return sw.toString();
  }

  private void updateTestStatusIfNeeded(Scenario scenario) {
    Result failResult = null;

    try {
      Field delegate = scenario.getClass().getDeclaredField("delegate");
      delegate.setAccessible(true);
      TestCaseState testCaseState = (TestCaseState) delegate.get(scenario);

      Field stepResults = testCaseState.getClass().getDeclaredField("stepResults");
      stepResults.setAccessible(true);

      List<Result> results = (List<Result>) stepResults.get(testCaseState);
      for (Result result : results) {
        if (result.getStatus().name().equalsIgnoreCase("FAILED")) {
          failResult = result;
          testStatus = "failed";
        }
      }
    } catch (NoSuchFieldException | IllegalAccessException e) {
      System.err.println(Arrays.toString(e.getStackTrace()));
    }

    if (failResult != null) {
      errorMessage = errorMessage + "\n" + failResult.getError();
      String errorStackTrace = getStackTrace(failResult.getError());
      ltLogger.debug("Scenario failed with error: {}", errorMessage);
      ltLogger.debug("Scenario failed with stacktrace: {}", errorStackTrace);
    }
  }

  private void setTestStatus() {
    HashMap<String, String> updatedPayload = new HashMap<>();
    updatedPayload.put("status_ind", testStatus);

    if (FAILED.equalsIgnoreCase(testStatus)) {
      String formattedErrorMessage = errorMessage.replaceAll("(\r\n|\n|\r)", "\\\\n").replaceAll("\t", "");
      updatedPayload.put("reason", formattedErrorMessage);
    }

    apiHelper.updateSessionDetailsViaAPI(EnvSetup.TEST_SESSION_ID.get(), updatedPayload);
  }

  private void updateTestReport() {
    TEST_REPORT.get().put("scenarioName", scenarioName);
    TEST_REPORT.get().put("userName", EnvSetup.testUserName.get());
    TEST_REPORT.get().put("accessKey", EnvSetup.testAccessKey.get());
    TEST_REPORT.get().put("hub", EnvSetup.testGridUrl.get());
  }

  @After(order = 1)
  public void afterScenario(Scenario scenario) {
    try {
      EnvSetup.testDriver.get().quit();
    } catch (Exception ignored) {
      // Driver quit failure can be safely ignored
    }

    scenarioName = scenario.getName();
    ltLogger.info("Test report: {}", TEST_REPORT.get());
    updateTestStatusIfNeeded(scenario);

    apiHelper.waitForTime(5);

    if (COMPLETED.equalsIgnoreCase(
      apiHelper.getSpecificSessionDetailsViaAPI(EnvSetup.TEST_SESSION_ID.get(), "status_ind"))) {
      setTestStatus();
    }

    updateTestReport();
    apiHelper.sendCustomDataToSumo(TEST_REPORT.get());
  }

  @After(order = 2)
  public void assertAll() {
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    try {
      softAssert.assertAll();
    } catch (AssertionError e) {
      errorMessage = e.getLocalizedMessage();
      testStatus = "failed";
      ltLogger.debug("Assertion error: {}", errorMessage);
      throw e;
    }
  }
}
