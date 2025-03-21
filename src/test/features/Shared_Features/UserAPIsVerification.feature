@publicAPIsVerification
Feature: Verify all the public APIs

  @testStopApiVerification
  Scenario Outline: User should be able to stop test via API
    Given Setup user details
    Then I start session without driver quit to test noAction with <capabilities>
    Then I stop the running test via API
    Then I confirm the test status is stopped

    Examples:
      | capabilities                                                     |
      | browserName=chrome,platform=win10,version=latest,idleTimeout=900 |