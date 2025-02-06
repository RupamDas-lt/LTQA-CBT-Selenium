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
import java.util.HashMap;
import java.util.List;

import static utility.FrameworkConstants.COMPLETED;
import static utility.FrameworkConstants.FAILED;

public class Hooks {
  private final Logger ltLogger = LogManager.getLogger(Hooks.class);
  private final AutomationAPIHelper apiHelper = new AutomationAPIHelper();
  private String errorStackTrace;
  private String errorMessage = "";
  private String testStatus = "passed";

  @Before
  public void beforeScenario() {
    CustomSoftAssert softAssert = new CustomSoftAssert();
    EnvSetup.SOFT_ASSERT.set(softAssert);
    EnvSetup.TEST_SESSION_ID.set("");
    EnvSetup.TEST_REPORT.set(new HashMap<>());
    EnvSetup.TEST_ERR_REPORT.set(new HashMap<>());
  }

  // Helper method to get stack trace as a string
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
      e.printStackTrace();  // Optional: Handle logging for reflection failures
    }

    if (failResult != null) {
      errorMessage = String.valueOf(failResult.getError());
      errorStackTrace = getStackTrace(failResult.getError());
      ltLogger.debug("Scenario failed with error: {}", errorMessage);
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

  @After(order = 1)
  public void afterScenario(Scenario scenario) {
    try {
      EnvSetup.testDriver.get().quit();
    } catch (Exception ignored) {
      // Driver quit failure can be safely ignored
    }

    ltLogger.info("Test report: {}", EnvSetup.TEST_REPORT.get());
    updateTestStatusIfNeeded(scenario);

    apiHelper.waitForTime(5);

    if (COMPLETED.equalsIgnoreCase(
      apiHelper.getSpecificSessionDetailsViaAPI(EnvSetup.TEST_SESSION_ID.get(), "status_ind"))) {
      setTestStatus();
    }
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
    }
  }
}
