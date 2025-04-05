package runners;

import automationHelper.AutomationAPIHelper;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import utility.ScenarioUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static utility.FrameworkConstants.*;

@CucumberOptions(features = { "src/test/features" }, glue = { "stepDefinitions", "Hooks" }, plugin = { "pretty",
  "rerun:rerun/failed_scenarios.txt", "json:target/cucumber-reports/CucumberTestReport.json",
  "com.epam.reportportal.cucumber.ScenarioReporter" })

public class TestRunner extends AbstractTestNGCucumberTests {
  public TestRunner() {
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

  @BeforeSuite
  public void beforeSuite() {

    if (isReportPortalSetupRequiredWithLocalCreds())
      initializeReportPortalParameters();

    if (getProperty(TEST_PREREQUISITES, "true").equals("true"))
      setPrerequisite();
  }

  private boolean isReportPortalSetupRequiredWithLocalCreds() {
    if (Boolean.parseBoolean(System.getProperty(PUSH_DATA_LOGS_TO_RP_FROM_LOCAL_CONFIG, "true"))) {
      return REPORT_PORTAL_KEYS.stream().allMatch(key -> Boolean.parseBoolean(System.getProperty(key, "false")));
    }
    return false;
  }

  private void initializeReportPortalParameters() {
    try (InputStream input = TestRunner.class.getClassLoader().getResourceAsStream("reportportal.properties")) {
      if (input == null) {
        System.err.println("Sorry, unable to find reportportal.properties");
        return;
      }
      Properties prop = new Properties();
      prop.load(input);
      prop.forEach((key, value) -> System.setProperty(key.toString(), value.toString()));
    } catch (IOException ex) {
      System.err.println("Unable to set reportportal properties: " + ex);
    }
  }

  private void setPrerequisite() {
    AutomationAPIHelper automationAPIHelper = new AutomationAPIHelper();
    automationAPIHelper.fetchAllGeoLocationsFromCapsGeneratorAndStoreInJsonFile();
  }
}
