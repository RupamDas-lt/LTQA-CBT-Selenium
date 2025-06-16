@shareLinksValidation
Feature: Test, Build and Video Share links

  @testShareLink @testShareLinkAPI
  Scenario Outline: User is able to share test
    Given Setup user details
    Then I start session with driver quit to test networkLog,consoleLog,exceptionLogTesting with <capabilities>
    Then I create test share link
    Then I verify test share link via API
    Then I start client test session
    Then I verify test share link via UI
    Then I stop client test session

    Examples:
      | capabilities                                                                                                                                           |
      | browserName=chrome,platform=win10,version=latest,network=TRUE,console=TRUE,build=testShareLinkVerificationBuild,name=testShareLinkVerification_windows |
      | browserName=chrome,platform=sequoia,version=latest,network=TRUE,console=TRUE,build=testShareLinkVerificationBuild,name=testShareLinkVerification_mac   |

  @buildShareLink @buildShareLinkAPI
  Scenario Outline: User is able to share build
    Given Setup user details
    Then I start 3 sessions with driver quit to test networkLog,consoleLog,exceptionLogTesting with <capabilities>
    Then I extract build id from session ID
    Then I create build share link
    Then I verify build share link via API
    Then I start client test session
    Then I verify build share link via UI
    Then I stop client test session

    Examples:
      | capabilities                                                                                                                                     |
      | browserName=chrome,platform=win11,version=latest,network=TRUE,console=TRUE,build=buildShareLinkVerificationBuild,name=buildShareLinkVerification |