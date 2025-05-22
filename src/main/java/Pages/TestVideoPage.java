package Pages;

import TestManagers.DriverManager;
import factory.Locator;
import factory.LocatorTypes;
import factory.SoftAssertionMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utility.CustomSoftAssert;
import utility.FrameworkConstants;

import static factory.SoftAssertionMessages.*;

public class TestVideoPage extends LTDashboardCommonActions {

  private final Logger ltLogger = LogManager.getLogger(TestVideoPage.class);

  DriverManager driver;
  CustomSoftAssert softAssert;

  private static final Locator videoNotPresentMessage = new Locator(LocatorTypes.CSS,
    "div[class*='VideoContainer_errorMsg']");
  private static final Locator video = new Locator(LocatorTypes.TAG_NAME, "video");
  private static final Locator playVideoButton = new Locator(LocatorTypes.CSS, "button[aria-label='Play Video']");
  private static final Locator pauseVideoButton = new Locator(LocatorTypes.CSS, "button[aria-label='Pause Video']");

  public TestVideoPage(String testId, DriverManager driverManager, CustomSoftAssert softAssert) {
    super(testId, driverManager);
    driver = driverManager;
    this.softAssert = softAssert;
  }

  private boolean isVideoPresent(int retryCount) {
    retryCount = Math.max(1, retryCount);

    ltLogger.info("Checking if video is present");
    boolean isVideoPresent = false;

    for (int attempt = 1; attempt <= retryCount && !isVideoPresent; attempt++) {
      isVideoPresent = driver.isDisplayed(video, 20) && driver.isDisplayed(playVideoButton, 20);
      ltLogger.info("Attempt: {} -> Video status: {}", attempt, isVideoPresent);

      if (!isVideoPresent && attempt < retryCount) {
        driver.refreshPage();
      }
    }

    if (!isVideoPresent) {
      SoftAssertionMessages errorMessage = driver.isDisplayed(videoNotPresentMessage, 10) ?
        VIDEO_NOT_GENERATED_CLIENT_ERROR_MESSAGE :
        VIDEO_VERIFICATION_FAILED_CLIENT_ERROR_MESSAGE;
      softAssert.fail(softAssert.softAssertMessageFormat(errorMessage));
    }

    return isVideoPresent;
  }

  private void validateVideoIsPlayable() {
    double videoDuration = Double.parseDouble(
      driver.executeScriptAndFetchValue(FrameworkConstants.jsToGetVideoDurationFromDOM).toString());
    ltLogger.info("Video duration: {}", videoDuration);
    if (videoDuration < 10) {
      softAssert.fail(softAssert.softAssertMessageFormat(VIDEO_DURATION_MISMATCH_CLIENT_ERROR_MESSAGE, videoDuration));
      return;
    }
    driver.click(playVideoButton);
    double videoCurrentTimeStamp;
    waitForTime(10);
    videoCurrentTimeStamp = Double.parseDouble(
      driver.executeScriptAndFetchValue(FrameworkConstants.jsToGetVideoCurrentTimeStampFromDOM).toString());
    ltLogger.info("Video currentTimeStamp after clicking on video play button and waiting for 10 secs : {}",
      videoCurrentTimeStamp);
    softAssert.assertTrue(videoCurrentTimeStamp > 5,
      softAssert.softAssertMessageFormat(VIDEO_NOT_PLAYABLE_CLIENT_ERROR_MESSAGE, videoCurrentTimeStamp));
    if (driver.isDisplayed(pauseVideoButton, 2)) {
      driver.click(pauseVideoButton);
      ltLogger.info("Video paused");
    }
  }

  public void validateTestVideo() {
    if (isVideoPresent(2)) {
      validateVideoIsPlayable();
    }
  }

}
