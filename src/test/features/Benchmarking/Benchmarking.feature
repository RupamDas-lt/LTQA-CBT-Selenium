@Benchmarking
Feature: Run Benchmarking tests on LambdaTest, SauceLabs and BrowserStack and collect data

  @Benchmarking_1
  Scenario Outline: Run tests with network on with different browser versions and platforms and get benchmark data
    Given Setup user details
    Then I set test actions repeat count to <testActionsRepeatCount>
    Then I start session with driver quit on <cloudPlatform> to test herokuAppAllTests,networkLog with <capabilities>

    Examples:
      | capabilities                                                                                                 | cloudPlatform | testActionsRepeatCount |
      | os=Windows,osVersion=10,browserName=Chrome,browserVersion=latest,buildName=CBT-Selenium-Benchmarking         | browserstack  | 5                      |
      | platform=Windows 10,browserName=Chrome,version=latest,build=CBT-Selenium-Benchmarking,extendedDebugging=true | saucelab      | 5                      |
      | browserName=chrome,platform=win10,version=latest,build=CBT-Selenium-Benchmarking                             | lambdatest    | 5                      |