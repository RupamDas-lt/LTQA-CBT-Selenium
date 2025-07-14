@sharedFeatures @customerIssues
Feature: Verify all the Customer Issues

  @CustomerIssues_1 @FakeMedia @OpenUrlInNewTab
  Scenario Outline: User is able to use Fake Media feature, open URL in new tab and switch to it
    Given Setup user details
    Then I start session with driver quit to test fakeMedia,openUrlInNewTab with <capabilities>

    Examples:
      | capabilities                                                                                                                                                                           |
      | browserName=chrome,platform=win10,version=latest,goog:chromeOptions={args=[start-maximized,--ignore-certificate-errors,use-fake-ui-for-media-stream,use-fake-device-for-media-stream]} |

  @CustomerIssues_2 @OpenUrlInNewTab
  Scenario Outline: User is able to set specific browser language
    Given Setup user details
    Then I start session with driver quit to test detectLanguageSetFromBrowserOptions with <capabilities>

    Examples:
      | capabilities                                                                         |
      | browserName=chrome,platform=win10,version=latest,goog:chromeOptions={args=[lang=fr]} |

  @CustomerIssues_3 @CheckCustomUserAgent @CloseWindowAndDriverQuit
  Scenario Outline: User is able to use custom user agent, window.close and driver.close methods and after that driver.quit shouldn't throw any exception
    Given Setup user details
    Then I start session without driver quit to test checkUserAgent,closeWindowAndDriverQuit with <capabilities>

    Examples:
      | capabilities                                                                                                                                                                                                          |
      | browserName=chrome,platform=win10,version=latest,goog:chromeOptions={args=[--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML; like Gecko) Chrome/103.0.5060.66 Safari/537.36 machine]} |
