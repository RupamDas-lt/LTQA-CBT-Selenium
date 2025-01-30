Feature: Trial

  @trial
  Scenario Outline: Trial scenario
    Given Setup user details
    Then I start tunnel
    Then I start session to test local,basicAuthentication with <capabilities>
    Then I stop tunnel

    Examples:
      | capabilities                                              |
      | browserName=chrome,version=130,platform=win10,tunnel=true |