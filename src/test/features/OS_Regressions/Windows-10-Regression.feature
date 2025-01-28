Feature: Trial

  @trial
  Scenario Outline: Trial scenario
    Given Setup user details
    Then I start session to test basicAuthentication with <capabilities>

    Examples:
      | capabilities                                  |
      | browserName=chrome,version=130,platform=win10 |