@shareLinksValidation
Feature: Test, Build and Video Share links

  @testShareLink @testShareLinkAPI
  Scenario Outline: User is able to share test
    Given Setup user details
    Then I start session with driver quit to test networkLog,consoleLog,exceptionLogTesting with <capabilities>
    Then I create test share link
    Then I verify test share link via API

    Examples:
      | capabilities                                                                 |
      | browserName=chrome,platform=win10,version=latest,network=TRUE,console=TRUE   |
      | browserName=chrome,platform=sequoia,version=latest,network=TRUE,console=TRUE |
      | browserName=chrome,platform=ubuntu,version=latest,network=TRUE,console=TRUE  |


  @buildShareLink @buildShareLinkAPI
  Scenario Outline: User is able to share test
    Given Setup user details
    Then I start 3 sessions with driver quit to test networkLog,consoleLog,exceptionLogTesting with <capabilities>
    Then I create test share link
    Then I verify test share link via API

    Examples:
      | capabilities                                                                 |
      | browserName=chrome,platform=win10,version=latest,network=TRUE,console=TRUE   |
      | browserName=chrome,platform=sequoia,version=latest,network=TRUE,console=TRUE |
      | browserName=chrome,platform=ubuntu,version=latest,network=TRUE,console=TRUE  |