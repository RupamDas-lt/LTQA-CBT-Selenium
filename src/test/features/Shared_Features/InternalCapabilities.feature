@InternalCapabilities
Feature: User is able to use internal capabilities

  @lambdaStrict
  Scenario Outline: User is able to use lambdaStrict
    Given Setup user details
    Then I start session with driver quit to check session creation and test noAction with <capabilities>
    Then I verify session creation status is <expected_status>

    Examples:
      | capabilities                                                                               | expected_status                                                                                                               |
      | browserName=chrome,platform=win10,version=100,lambdaStrict=true,selenium_version=3.141.59  | passed                                                                                                                        |
      | browserName=chrome,platform=win10,version=100,lambdaStrict=false,selenium_version=3.141.59 | Error: Could not find a valid browserVersionData. config received, osVersion: win10 browserName: chrome browserVersion: 100.0 |