package stepDefinitions;

import automationHelper.AutomationHelper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

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
  }
}
