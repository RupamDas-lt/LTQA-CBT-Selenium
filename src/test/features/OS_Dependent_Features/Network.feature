@network_regression
Feature: Network features like network.http2, network.throttling, network.tunnel, network.full.har, network.sse

  @network_regression_1 @network_sse_verification
  Scenario Outline: User is able to run test with WebSocket
    Given Setup user details
    Then I start session with driver quit to test networkSSE with <capabilities>
    Then I verify network Log via API


    @sonoma_regression
    Examples:
      | capabilities                                                   |
      | browserName=chrome,platform=sonoma,version=latest,network=true |

    @win10_regression
    Examples:
      | capabilities                                                  |
      | browserName=chrome,platform=win10,version=latest,network=true |

    @ubuntu_regression
    Examples:
      | capabilities                                                   |
      | browserName=chrome,platform=ubuntu,version=latest,network=true |

