@Accessibility_Manual
Feature: Android & iOS A11y App Manual on Real Device

  @A11yAppManualSmoke
  Scenario Outline: Checking A11y of Android & iOS Apps through Manual tests
    Given Setup user details
    Then I start client test session
    And Login to LT dashboard
    Then I open Accessibility Manual Home page and select the App and the device to start the Manual Accessibility test on <OS>
    Then I verify that testStartedAndAppInstalled,scanHappeningAndTestGettingSaved,issueTab
    Then I verify the Left Navbar features like screenshot,recordSession,rotate,stopTest
    Then I validate the accessibilityReport,allIssuesTab,mobileView
    Then I stop client test session

    Examples:
      | OS      |
      | Android |
#      | IOS     |


