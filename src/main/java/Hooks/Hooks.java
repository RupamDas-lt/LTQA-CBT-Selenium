package Hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import utility.CustomSoftAssert;
import utility.EnvSetup;

import java.util.HashMap;

public class Hooks {
  @Before
  public void beforeScenario() {
    CustomSoftAssert softAssert = new CustomSoftAssert();
    EnvSetup.SOFT_ASSERT.set(softAssert);
    EnvSetup.TEST_SESSION_ID.set("");
    EnvSetup.TEST_REPORT.set(new HashMap<>());
  }

  @After
  public void afterScenario() {
    try {
      EnvSetup.testDriver.get().quit();
    } catch (Exception ignored) {
    }
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    softAssert.assertAll();
    System.out.println("Test report: " + EnvSetup.TEST_REPORT.get());
  }
}
