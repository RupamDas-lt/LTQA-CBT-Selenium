@commandLogAnnotations @sharedFeatures
Feature: Command Log Annotations

  Scenario Outline: Command Log Annotations testing
    Given Setup user details
    Then I start session with driver quit and without setting default test contexts to test annotationWithLambdaTestCase,annotationWithStepContext with <capabilities>
    Then I verify command log Annotations via API

    @mitmTrueWeb
    Examples:
      | capabilities                                 |
      | browserName=chrome,platform=win10,version=.* |