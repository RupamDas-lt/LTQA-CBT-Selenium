@publicAPIsVerification @sharedFeatures
Feature: Verify all the public APIs

  @testStopApiVerification @smoke_api
  Scenario Outline: User should be able to stop test via API
    Given Setup user details
    Then I start session without driver quit to test noAction with <capabilities>
    Then I stop the running test via API
    Then I confirm the test status is stopped

    Examples:
      | capabilities                                                                                          |
      | browserName=chrome,platform=win10,version=latest,idleTimeout=900,name=Test_Stop_API_Verification_Test |

  @buildStopApiVerification @smoke_api
  Scenario Outline: User should be able to stop build via API
    Given Setup user details
    Then I start session without driver quit to test noAction with <capabilities>
    Then I stop the running build via API
    Then I confirm the build status is stopped

    Examples:
      | capabilities                                                                                                                             |
      | browserName=chrome,platform=win11,version=latest,idleTimeout=900,name=Build_Stop_API_Verification_Test,build=Build_Stop_Tests_randomName |


  @tunnelStopApiVerification @smoke_api
  Scenario: User should be able to stop tunnel via API
    Given Setup user details
    Then I start tunnel
    Then I verify tunnel is running via API
    Then I stop tunnel via api
    Then I verify tunnel is stopped via API
