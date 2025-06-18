@browserProfileSanity
Feature: Browser Profile Sanity

  @browserProfileFromS3
  Scenario Outline: User is upload chrome profile to S3 and use that
    Given Setup user details
    When I upload browser profile from file path <filePath> to lambda storage
    Then I verify the browser profile is uploaded successfully with <fileName> file name
    Then I start session with driver quit to test networkLog,chromeProfile with <capabilities>
    Then I delete browser profile with <fileName> from lambda storage

    Examples:
      | fileName          | filePath                                       | capabilities                                                                          |
      | ChromeProfile.zip | src/test/resources/TestFiles/ChromeProfile.zip | browserName=chrome,platform=win10,version=latest,browserProfile=RETRIEVED_S3_URL_PATH |
