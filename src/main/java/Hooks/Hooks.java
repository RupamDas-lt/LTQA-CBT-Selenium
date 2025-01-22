package Hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import utility.CustomSoftAssert;
import utility.EnvSetup;

public class Hooks {
  @Before
  public void beforeScenario() {
    CustomSoftAssert softAssert = new CustomSoftAssert();
    EnvSetup.SOFT_ASSERT.set(softAssert);
  }

  @After
  public void afterScenario() {
    try {
      EnvSetup.testDriver.get().quit();
    } catch (Exception ignored) {
    }
    CustomSoftAssert softAssert = EnvSetup.SOFT_ASSERT.get();
    softAssert.assertAll();
  }
}
