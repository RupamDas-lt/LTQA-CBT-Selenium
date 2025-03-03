@win10_regression
Feature: Automation of windows10 machine with different browsers.

  @win10_regression_1
  Scenario Outline: User is able to run local test session and run session with tunnel for windows10 browser to test console,command log and video with network false
    Given Setup user details
    Then I start tunnel
    Then I start session to test local,selfSigned,consoleLog,timezone,basicAuthentication,fillFormUsingKeyboard,networkLog,exceptionLogTesting,browserOSDetails,verifyExtension,uploadFile with <capabilities>
    Then I stop tunnel
    Then I start client test session
    And Login to LT dashboard
    Then I stop client test session

    Examples:
      | capabilities                                                                                                          |
      | browserName=chrome,version=latest,resolution=.*,platform=win10,geoLocation=.*,timezone=UTC+01:00,loadExtension=true   |
      | browserName=chrome,version=latest,resolution=.*,platform=win10,geoLocation=.*,timezone=UTC+01:00,loadExtension=true   |
      | browserName=chrome,version=latest-1,resolution=.*,platform=win10,geoLocation=.*,timezone=UTC+01:00,loadExtension=true |
      | browserName=chrome,version=latest-1,resolution=.*,platform=win10,geoLocation=.*,timezone=UTC+01:00,loadExtension=true |
      | browserName=chrome,version=latest,resolution=.*,platform=win11,geoLocation=.*,timezone=UTC+01:00,loadExtension=true   |
      | browserName=chrome,version=latest,resolution=.*,platform=win11,geoLocation=.*,timezone=UTC+01:00,loadExtension=true   |
      | browserName=chrome,version=latest-1,resolution=.*,platform=win11,geoLocation=.*,timezone=UTC+01:00,loadExtension=true |
      | browserName=chrome,version=latest-1,resolution=.*,platform=win11,geoLocation=.*,timezone=UTC+01:00,loadExtension=true |