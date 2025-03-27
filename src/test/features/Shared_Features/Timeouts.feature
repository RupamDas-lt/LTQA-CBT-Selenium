@testTimeoutsVerification
Feature: Verify test timeouts

  @testIdleTimeoutVerification
  Scenario Outline: The test should idleTimeout after remaining in idle state for a specific time
    Given Setup user details
    Then I start session without driver quit to test consoleLog,networkLog,idleTimeout with <capabilities>
    Then I confirm the test status is idle_timeout
    Then I verify selenium Log via API
    Then I verify console Log via API
    Then I verify network Log via API
    Then I verify command Log via API

    Examples:
      | capabilities                                                                                                 |
      | browserName=chrome,platform=win10,version=.*,idleTimeout=120,name=Test_IdleTimeout,network=true,console=true |