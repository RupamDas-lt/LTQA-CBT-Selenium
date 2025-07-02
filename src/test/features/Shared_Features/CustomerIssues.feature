Feature: Verify all the Customer Issues

  @CustomerIssues_1 @FakeMedia
  Scenario Outline: User is able to use Fake Media feature
    Given Setup user details
    Then I start session with driver quit to test fakeMedia with <capabilities>

    Examples:
      | capabilities                                                                                                                                                                           |
      | browserName=chrome,platform=win10,version=latest,goog:chromeOptions={args=[start-maximized,--ignore-certificate-errors,use-fake-ui-for-media-stream,use-fake-device-for-media-stream]} |

  @CustomerIssues_2 @OpenUrlInNewTab
  Scenario Outline: User is able to open URL in new tab and switch to it
    Given Setup user details
    Then I start session with driver quit to test openUrlInNewTab with <capabilities>

    Examples:
      | capabilities                                     |
      | browserName=chrome,platform=win10,version=latest |