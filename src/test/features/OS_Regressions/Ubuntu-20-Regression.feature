@ubuntu_regression
Feature: Automation of ubuntu-20 machine with different browsers.

  @ubuntu_regression_1 @tunnel_verification
  Scenario Outline: User is able to run local test session and run session with tunnel for ubuntu-20 browser to test console,command log and video with network false
    Given Setup user details
    Then I start tunnel
    Then I start session with driver quit to test local,selfSigned,consoleLog,timezone,basicAuthentication,fillFormUsingKeyboard,networkLog,exceptionLogTesting,browserOSDetails,verifyExtension,uploadFile with <capabilities>
    Then I stop tunnel
    Then I verify console Log via API
    Then I verify selenium Log via API
    Then I verify command Log via API
    Then I verify video via API

    Examples:
      | capabilities                                                                                                                                                                                   |
      | browserName=chrome,platform=ubuntu,version=.*,resolution=.*,timezone=.*,visual=true,network=false,network.http2=false,tunnel=true,loadExtension=true,console=true                              |
      | browserName=edge,platform=ubuntu,version=.*,resolution=.*,timezone=.*,visual=true,network=false,network.http2=false,tunnel=true,loadExtension=true,selenium_version=.*,seCdp=true,console=true |


  @ubuntu_regression_2 @geoLocations_verification
  Scenario Outline: User is able to verify network log, console log, selenium log, session video, timezone via api and driver start/stop time within its limit for ubuntu-20 browser with geolocation and network false
    Given Setup user details
    Then I start session with driver quit to test uploadFile,consoleLog,basicAuthentication,geolocation,networkLog,exceptionLogTesting,timezone,browserOSDetails with <capabilities>
    Then I verify console Log via API
    Then I verify selenium Log via API
    Then I verify command Log via API

    Examples:
      | capabilities                                                                                                                                      |
      | browserName=chrome,platform=ubuntu,version=.*,resolution=.*,timezone=.*,visual=true,network=false,network.http2=false,console=true,geoLocation=.* |
      | browserName=firefox,platform=ubuntu,version=.*,resolution=.*,timezone=.*,visual=true,network=false,console=true,geoLocation=.*                    |


  @ubuntu_regression_3 @tunnel_verification @martian_verification
  Scenario Outline: User is able to run local test session and run session with tunnel for ubuntu-20 browser with network true
    Given Setup user details
    Then I start tunnel
    Then I start session with driver quit to test local,loginCacheCleaned,basicAuthentication,consoleLog,timezone,exceptionLogTesting,browserOSDetails with <capabilities>
    Then I stop tunnel
    Then I verify console Log via API
    Then I verify selenium Log via API
    Then I verify command Log via API
    Then I verify network Log via API

    @gdpr_smoke
    Examples:
      | capabilities                                                                                                                                                                      |
      | browserName=edge,platform=ubuntu,version=.*,resolution=.*,timezone=.*,visual=true,network=true,network.http2=true,tunnel=true,network.har=true,network.full.har=true,console=true |


  @ubuntu_regression_4 @martian_verification
  Scenario Outline: User is able to verify network log, console log, selenium log, session video, timezone via api for ubuntu-20 browser with geolocation and network true
    Given Setup user details
    Then I start session with driver quit to test selfSigned,uploadFile,basicAuthentication,consoleLog,timezone,exceptionLogTesting,browserOSDetails,networkLog with <capabilities>
    Then I verify console Log via API
    Then I verify selenium Log via API
    Then I verify command Log via API
    Then I verify network Log via API

    Examples:
      | capabilities                                                                                                                                                        |
      | browserName=chrome,platform=ubuntu,version=.*,resolution=.*,timezone=.*,visual=true,network=true,network.http2=true,geoLocation=.*,selenium_version=.*,console=true |

  @ubuntu_regression_5 @ui_verification @tunnel_verification @martian_verification
  Scenario Outline: PT-13416871 network logs, console & selenium logs should be generated and visible on UI for ubuntu-20 browser with tunnel true
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

    Examples:
      | capabilities                                                                                                                   |
      | browserName=chrome,platform=ubuntu,version=.*,performance=true,resolution=.*,network=true,visual=true,tunnel=true,console=true |


  @ubuntu_regression_6 @ui_verification @martian_verification
  Scenario Outline: Network logs, console & selenium logs should be generated and visible on UI for ubuntu-20 browser on specific geolocation
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

    Examples:
      | capabilities                                                                                                                   |
      | browserName=edge,platform=ubuntu,version=.*,resolution=.*,network=true,visual=true,network.har=true,terminal=true,console=true |

  @ubuntu_regression_7
  Scenario Outline: User is able to run multiple sessions on for ubuntu machine with different browsers and latest and random versions
    Given Setup user details
    Then I start session with driver quit to test browserOSDetails with <capabilities>

    @latest_browser_and_selenium_versions
    Examples:
      | capabilities                                                                                                     |
      | browserName=chrome,platform=ubuntu,version=latest,resolution=.*,timezone=.*,visual=true,selenium_version=latest  |
      | browserName=firefox,platform=ubuntu,version=latest,resolution=.*,timezone=.*,visual=true,selenium_version=latest |
      | browserName=edge,platform=ubuntu,version=latest,resolution=.*,timezone=.*,visual=true,selenium_version=latest    |

    @random_browser_and_selenium_versions
    Examples:
      | capabilities                                                                                             |
      | browserName=chrome,platform=ubuntu,version=.*,resolution=.*,timezone=.*,visual=true,selenium_version=.*  |
      | browserName=firefox,platform=ubuntu,version=.*,resolution=.*,timezone=.*,visual=true,selenium_version=.* |
      | browserName=edge,platform=ubuntu,version=.*,resolution=.*,timezone=.*,visual=true,selenium_version=.*    |
      | browserName=chrome,platform=ubuntu,version=.*,resolution=.*,timezone=.*,visual=true                      |
      | browserName=firefox,platform=ubuntu,version=.*,resolution=.*,timezone=.*,visual=true                     |
      | browserName=edge,platform=ubuntu,version=.*,resolution=.*,timezone=.*,visual=true                        |
