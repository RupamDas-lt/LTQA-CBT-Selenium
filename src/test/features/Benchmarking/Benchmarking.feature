@Benchmarking
Feature: Run Benchmarking tests on LambdaTest, SauceLabs and BrowserStack and collect data

  @Benchmarking_1
  Scenario Outline: Run tests with network on with different browser versions and platforms and get benchmark data
    Given Setup user details
    Then I start session with driver quit on <cloudPlatform> to test networkLog with <capabilities>

    Examples:
      | capabilities                                                                                    | cloudPlatform |
      | os=Windows,osVersion=10,browserName=Chrome,browserVersion=latest,buildName=CBT-Selenium         | browserstack  |
      | platform=Windows 10,browserName=Chrome,version=latest,build=CBT-Selenium,extendedDebugging=true | saucelab      |