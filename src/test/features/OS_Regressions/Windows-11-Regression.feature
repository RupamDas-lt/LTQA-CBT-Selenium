@win11_regression
Feature: Automation of windows11 machine with different browsers.

  @win11_regression_1 @tunnel_verification
  Scenario Outline: User is able to run local test session and run session with tunnel for windows11 browser to test console,command log and video with network false
    Given Setup user details
    Then I start tunnel
    Then I start session with driver quit to test local,selfSigned,consoleLog,timezone,basicAuthentication,fillFormUsingKeyboard,networkLog,exceptionLogTesting,browserOSDetails,uploadFile with <capabilities>
    Then I stop tunnel
    Then I verify console Log via API
    Then I verify selenium Log via API
    Then I verify command Log via API
    Then I verify video via API

    Examples:
      | capabilities                                                                                                                                  |
      | browserName=chrome,platform=win11,version=.*,resolution=.*,timezone=.*,visual=true,network=false,network.http2=false,tunnel=true,console=true |

    @oregon_smoke @frankfurt_smoke_new
    Examples:
      | capabilities                                                                                                                                                               |
      | browserName=edge,platform=win11,version=.*,resolution=.*,timezone=.*,visual=true,network=false,network.http2=false,tunnel=true,selenium_version=.*,seCdp=true,console=true |


  @win11_regression_2 @geoLocations_verification
  Scenario Outline: User is able to verify network log, console log, selenium log, session video, timezone via api and driver start/stop time within its limit for windows11 browser with geolocation and network false
    Given Setup user details
    Then I start session with driver quit to test uploadFile,consoleLog,basicAuthentication,geolocation,networkLog,exceptionLogTesting,timezone,browserOSDetails with <capabilities>
    Then I verify console Log via API
    Then I verify selenium Log via API
    Then I verify command Log via API
    Then I verify video via API

    @ohio_smoke_new
    Examples:
      | capabilities                                                                                                                 |
      | browserName=chrome,platform=win11,version=.*,resolution=.*,timezone=.*,visual=true,network=false,console=true,geoLocation=.* |

    @london_smoke
    Examples:
      | capabilities                                                                                                                                   |
      | browserName=edge,platform=win11,version=.*,resolution=.*,timezone=.*,visual=true,network=false,network.http2=false,console=true,geoLocation=.* |


  @win11_regression_3 @tunnel_verification @martian_verification
  Scenario Outline: User is able to run local test session and run session with tunnel for windows11 browser with network true
    Given Setup user details
    Then I start tunnel
    Then I start session with driver quit to test local,loginCacheCleaned,basicAuthentication,consoleLog,timezone,exceptionLogTesting,browserOSDetails with <capabilities>
    Then I stop tunnel
    Then I verify console Log via API
    Then I verify selenium Log via API
    Then I verify command Log via API
    Then I verify network Log via API
    Then I verify video via API

    @paris_smoke @london_smoke_new
    Examples:
      | capabilities                                                                                                                                                                        |
      | browserName=firefox,platform=win11,version=.*,resolution=.*,timezone=.*,visual=true,network=true,network.http2=true,tunnel=true,network.har=true,network.full.har=true,console=true |


  @win11_regression_4 @martian_verification
  Scenario Outline: User is able to verify network log, console log, selenium log, session video, timezone via api for windows11 browser with geolocation and network true
    Given Setup user details
    Then I start session with driver quit to test selfSigned,uploadFile,basicAuthentication,consoleLog,timezone,exceptionLogTesting,browserOSDetails,networkLog with <capabilities>
    Then I verify console Log via API
    Then I verify selenium Log via API
    Then I verify command Log via API
    Then I verify network Log via API
    Then I verify video via API

    @singapore_smoke @singapore_smoke_new
    Examples:
      | capabilities                                                                                                                                                       |
      | browserName=chrome,platform=win11,version=.*,resolution=.*,timezone=.*,visual=true,network=true,network.http2=true,geoLocation=.*,selenium_version=.*,console=true |

  @win11_regression_5 @ui_verification @tunnel_verification @martian_verification
  Scenario Outline: Network logs, console & selenium logs should be generated and visible on UI for windows11 browser with tunnel true
    Given Setup user details
    Then I start tunnel
    Then I start session with driver quit to test local,consoleLog,exceptionLogTesting,networkLog with <capabilities>
    Then I stop tunnel
    Then I start client test session
    And Login to LT dashboard
    Then I verify command logs from UI
    Then I verify network logs from UI
    Then I verify system logs from UI
    Then I verify console logs from UI
    Then I verify test video from UI
    Then I verify test performanceReport from UI
    Then I verify performance report Log via API
    Then I stop client test session

    @virginia_smoke
    Examples:
      | capabilities                                                                                                                  |
      | browserName=chrome,platform=win11,version=.*,performance=true,resolution=.*,network=true,visual=true,tunnel=true,console=true |


  @win11_regression_6 @ui_verification @martian_verification
  Scenario Outline: Network logs, console & selenium logs should be generated and visible on UI for windows11 browser on specific geolocation
    Given Setup user details
    Then I start session with driver quit to test consoleLog,exceptionLogTesting,networkLog with <capabilities>
    Then I upload sample terminal logs
    Then I start client test session
    And Login to LT dashboard
    Then I verify command logs from UI
    Then I verify network logs from UI
    Then I verify system logs from UI
    Then I verify console logs from UI
    Then I verify test video from UI
    Then I stop client test session

    @ohio_smoke
    Examples:
      | capabilities                                                                                                                  |
      | browserName=edge,platform=win11,version=.*,resolution=.*,network=true,visual=true,network.har=true,terminal=true,console=true |

  @win11_regression_7
  Scenario Outline: User is able to run multiple sessions on for win11 machine with different browsers and latest and random versions
    Given Setup user details
    Then I start session with driver quit to test browserOSDetails with <capabilities>
    Then I verify video via API

    @latest_browser_and_selenium_versions
    Examples:
      | capabilities                                                                                                    |
      | browserName=chrome,platform=win11,version=latest,resolution=.*,timezone=.*,visual=true,selenium_version=latest  |
      | browserName=firefox,platform=win11,version=latest,resolution=.*,timezone=.*,visual=true,selenium_version=latest |
      | browserName=edge,platform=win11,version=latest,resolution=.*,timezone=.*,visual=true,selenium_version=latest    |

    @random_browser_and_selenium_versions
    Examples:
      | capabilities                                                                                            |
      | browserName=chrome,platform=win11,version=.*,resolution=.*,timezone=.*,visual=true,selenium_version=.*  |
      | browserName=firefox,platform=win11,version=.*,resolution=.*,timezone=.*,visual=true,selenium_version=.* |
      | browserName=edge,platform=win11,version=.*,resolution=.*,timezone=.*,visual=true,selenium_version=.*    |
      | browserName=chrome,platform=win11,version=.*,resolution=.*,timezone=.*,visual=true                      |
      | browserName=firefox,platform=win11,version=.*,resolution=.*,timezone=.*,visual=true                     |
      | browserName=edge,platform=win11,version=.*,resolution=.*,timezone=.*,visual=true                        |


  @win11_regression_8 @extension_verification
  Scenario Outline: User is able to run test session with Extension on Windows 11
    Given Setup user details
    Then I start session with driver quit to test browserOSDetails,verifyExtension with <capabilities>
    Then I verify video via API

    Examples:
      | capabilities                                                                                          |
      | browserName=edge,platform=win11,version=135,visual=true,network=false,loadExtension=true,console=true |

    @frankfurt_smoke_new
    Examples:
      | capabilities                                                                                           |
      | browserName=chrome,platform=win11,version=135,visual=true,network=true,loadExtension=true,console=true |