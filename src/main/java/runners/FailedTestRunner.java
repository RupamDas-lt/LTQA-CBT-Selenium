package runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;
import utility.ScenarioUtils;

import static java.lang.System.getProperty;
import static java.lang.System.setProperty;

@CucumberOptions(features = { "@rerun/failed_scenarios.txt" }, glue = { "stepDefinitions",
  "Hooks" }, monochrome = true, plugin = { "pretty", "rerun:rerunFailedFirstAttempt/failed_scenarios_post_rerun.txt",
  "json:target/cucumber-reports/CucumberTestReport_failedTest.json",
  "com.epam.reportportal.cucumber.ScenarioReporter" }) public class FailedTestRunner
  extends AbstractTestNGCucumberTests {
  public FailedTestRunner() {
    super();
    setProperty("log4j.configurationFile", "src/main/resources/log4j2.yaml");
  }

  @Override
  @DataProvider(parallel = true)
  public Object[][] scenarios() {
    if (getProperty("RUN_N_TIMES") != null) {
      int runNTimes = Integer.parseInt(getProperty("RUN_N_TIMES"));
      return ScenarioUtils.repeatScenarios(super.scenarios(), runNTimes);
    }
    return super.scenarios();
  }
}
