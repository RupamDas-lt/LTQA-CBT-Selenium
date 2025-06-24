@buildTagTestTagProjectNameCheck @sharedFeatures
Feature: User is able to use build tags, test tags and project name

  @buildTagTestTagProjectNameCheck_1 @buildTagTestTagProjectNameCheckViaAPI
  Scenario Outline: User is able to run the test with buildTag, testTag and project name
    Given Setup user details
    Then I start session with driver quit to test networkLog with <capabilities>
    Then I verify test tags via swagger test details API
    Then I verify build tags via swagger build details API
    Then I verify project name via swagger build details API

    Examples:
      | capabilities                                                                                                                                                                                        |
      | browserName=chrome,platform=win10,version=latest,buildTags=[BUILD_TAGS_MAX_LENGTH],tags=[TEST_TAGS_MAX_LENGTH],projectName=CBT_Selenium_Project_PROJECT_NAME_MAX_LENGTH,name=testTagsAndProjectName |