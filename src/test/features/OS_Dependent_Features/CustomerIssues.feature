@CustomerIssues
Feature: Verify all the Customer Issues that are dependent on OS

  @CustomerIssues_1 @crossSiteBlocking @23andMe
  Scenario Outline: Verify Cross Site Blocking in Safari
    # Ticket: CBT-18656
    Given Setup user details
    Then I start session with driver quit to test crossSiteTracking,networkLog with <capabilities>
    Then I verify network Log via API
    Then I verify video via API

    @sonoma_regression
    Examples:
      | capabilities                                                                   |
      | browserName=safari,platform=sonoma,version=latest,enableCrossSiteBlocking=true |

    @sequoia_regression @flaky
    Examples:
      | capabilities                                                                      |
      | browserName=safari,platform=sequoia,version=latest,preventCrossSiteTracking=false |

    @tahoe_regression @flaky
    Examples:
      | capabilities                                                                  |
      | browserName=safari,platform=tahoe,version=latest,enableCrossSiteBlocking=true |

    @ventura_regression
    Examples:
      | capabilities                                                                      |
      | browserName=safari,platform=ventura,version=latest,preventCrossSiteTracking=false |
