@Accessibility_Manual
Feature: Android & iOS A11y App Manual on Real Device

  @A11yAppManualSmoke
  Scenario Outline: Checking A11y of Android & iOS Apps through Manual tests
    Given Setup user details
    Then I start client test session
    And Login to LT dashboard
    Then I open Accessibility Manual Home page
    Then I select the App and the device to start the Manual Accessibility test on <OS>
    Then I verify that the test is started and app is installed for accessibility testing
    Then I verify Scan is happening and test is getting saved of not
    Then I check the Issue tab for Screenshots of the pages
#    Then I verify the Left Navbar features like appControls,screenshot,recordSession,rotate
    Then I verify the Left Navbar features like screenshot,recordSession,rotate
    Then I stop the Manual Accessibility test
    Then I open the Manual Accessibility Dashboard to verify test Entry
    Then I validate the Manual Accessibility report, All Issues tab and Mobile View
    Then I stop client test session

    Examples:
      | OS      |
      | Android |
#      | IOS     |


