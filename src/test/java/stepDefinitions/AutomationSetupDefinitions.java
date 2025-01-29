package stepDefinitions;

import automationHelper.AutomationHelper;
import com.mysql.cj.util.StringUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import static utility.EnvSetup.*;
import static utility.FrameworkConstants.*;

public class AutomationSetupDefinitions {
  AutomationHelper automationHelper = new AutomationHelper();

  @Given("Run a trial scenario")
  public void runATrialScenario() {
    System.out.println("Trial scenario run");
  }

  @Then("^I start session to test ([a-zA-Z0-9_=,: ]+) with ([a-zA-Z0-9_=,:.+\\- ]+)$")
  public void startSessionAndPerformActivity(String testActions, String capability) {
    automationHelper.startSessionWithSpecificCapabilities(capability, testActions);
  }

  @Given("Setup user details")
  public void setupUserDetails() {
    testUserName.set(getPropertyOrDefault(CUSTOM_USER_NAME, USER_NAME));
    testAccessKey.set(getPropertyOrDefault(CUSTOM_USER_KEY, ACCESS_KEY));
    testEmail.set(getPropertyOrDefault(CUSTOM_USER_EMAIL, USER_EMAIL));
    testPassword.set(getPropertyOrDefault(CUSTOM_USER_PASS, USER_PASS));
    testGridUrl.set(getPropertyOrDefault(CUSTOM_GRID_URL, GRID_URL));
  }

  private String getPropertyOrDefault(String property, String defaultValue) {
    return StringUtils.isNullOrEmpty(System.getProperty(property, "")) ? defaultValue : System.getProperty(property);
  }

  @Then("I start tunnel")
  public void iStartTunnel() {
    automationHelper.startTunnel();
  }
}
