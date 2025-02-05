package Hooks;

import automationHelper.AutomationAPIHelper;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.plugin.event.Result;
import lombok.SneakyThrows;
import utility.CustomSoftAssert;
import utility.EnvSetup;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

public class Hooks {
  AutomationAPIHelper apiHelper = new AutomationAPIHelper();

  String errorStackTrace;
  String errorMessage;
  String testStatus = "passed";

  @Before
  public void beforeScenario() {
    CustomSoftAssert softAssert = new CustomSoftAssert();
    EnvSetup.SOFT_ASSERT.set(softAssert);
    EnvSetup.TEST_SESSION_ID.set("");
    EnvSetup.TEST_REPORT.set(new HashMap<>());
  }

  private void getTestRunStatus(Scenario scenario) {
    Result failResult = null;

    try {
      // Get the delegate from the scenario
      Field delegate = scenario.getClass().getDeclaredField("delegate");
      delegate.setAccessible(true);
      TestCaseState testCaseState = (TestCaseState) delegate.get(scenario);

      // Get the test case results from the delegate
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
      e.printStackTrace();
    }
    assert failResult != null;
    Throwable error = failResult.getError();
    StringWriter sw = new StringWriter();
    error.printStackTrace(new PrintWriter(sw));
    errorStackTrace = sw.toString();
    errorMessage = String.valueOf(failResult.getError());
    System.out.println("Scenario status: " + scenario.getStatus());
    System.out.println("Scenario name: " + scenario.getName());
    System.out.println("After test executed even after error occurred. Error: " + errorMessage);
  }

  private void setTestStatus() {
    HashMap<String, String> updatedPayload = new HashMap<>();
    updatedPayload.put("status_ind", testStatus);
    updatedPayload.put("reason", errorMessage);
    apiHelper.updateSessionDetailsViaAPI(EnvSetup.TEST_SESSION_ID.get(), updatedPayload);
  }

  @SneakyThrows
  @After
  public void afterScenario(Scenario scenario) {
    try {
      EnvSetup.testDriver.get().quit();
    } catch (Exception ignored) {
    }
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    softAssert.assertAll();
    System.out.println("Test report: " + EnvSetup.TEST_REPORT.get());
    getTestRunStatus(scenario);
    apiHelper.waitForTime(5);
    if (apiHelper.getSpecificSessionDetailsViaAPI(EnvSetup.TEST_SESSION_ID.get()).equalsIgnoreCase("completed")) {
      setTestStatus();
    }
  }
}
