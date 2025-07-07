@sharedFeatures @lambda_hooks_regression
Feature: Validate different Lambda Hooks

  @lambda_hooks_regression_1
  Scenario Outline: User is able to check downloaded file content using lambda-file-content hook
    Given Setup user details
    Then I start session with driver quit to test downloadSampleTxtFile,fetchFileContentUsingHooks with <capabilities>

    @sonoma_regression
    Examples:
      | capabilities                                     |
      | browserName=chrome,platform=win10,version=latest |