@browserProfileSanity
Feature: Browser Profile Sanity

  @chromeProfileFromS3
  Scenario Outline: User is upload chrome profile to S3 and use that
    Given Setup user details
    When I upload browser profile from file path <filePath> to lambda storage
    Then I verify the browser profile is uploaded successfully with <fileName> file name
    Then I start session with driver quit to test networkLog,chromeProfile with <capabilities>
    Then I delete browser profile with <fileName> from lambda storage

    Examples:
      | fileName          | filePath                                       | capabilities                                                                          |
      | ChromeProfile.zip | src/test/resources/TestFiles/ChromeProfile.zip | browserName=chrome,platform=win10,version=latest,browserProfile=RETRIEVED_S3_URL_PATH |

  @firefoxProfileInBrowserOptions
  Scenario Outline: User is able to use firefox profile in firefox options
    Given Setup user details
    Then I start session with driver quit to test firefoxProfile with <capabilities>

    Examples:
      | capabilities                                                                                                                                                        |
      | browserName=firefox,platform=win10,version=latest-5,moz:firefoxOptions={prefs={remote.active-protocols=3},profile=[USER_DEFINED_PROFILE_WITH_CUSTOM_DOWNLOAD_PATH]} |