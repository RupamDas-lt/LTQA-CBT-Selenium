@autoHeal_regression
Feature: Verification of AutoHeal functionality

  @autoHeal_regression_1
  Scenario Outline: User is able to use auto heal feature
    Given Setup user details
    Then I start session with driver quit to test autoHealBaseLineCapture with <capabilities>
    Then I start session with driver quit to test autoHealWithOldExistingLocators,autoHealWithNewLocators with <capabilities>

    @monterey_regression
    Examples:
      | capabilities                                                                                  |
      | browserName=edge,platform=monterey,version=latest,network=true,autoHeal=TRUE,tags=[auto-heal] |

    @high_sierra_regression
    Examples:
      | capabilities                                                                                       |
      | browserName=chrome,platform=high_sierra,version=latest,network=TRUE,autoHeal=TRUE,tags=[auto-heal] |

    @ubuntu_regression
    Examples:
      | capabilities                                                                                   |
      | browserName=firefox,platform=ubuntu,version=latest,network=TRUE,autoHeal=TRUE,tags=[auto-heal] |

    @sonoma_regression
    Examples:
      | capabilities                                                                                  |
      | browserName=safari,platform=sonoma,version=latest,network=TRUE,autoHeal=TRUE,tags=[auto-heal] |

    @sequoia_regression
    Examples:
      | capabilities                                                                                  |
      | browserName=chrome,platform=sonoma,version=latest,network=TRUE,autoHeal=TRUE,tags=[auto-heal] |

    @win11_regression
    Examples:
      | capabilities                                                                                 |
      | browserName=chrome,platform=win11,version=latest,network=TRUE,autoHeal=TRUE,tags=[auto-heal] |

    @ventura_regression
    Examples:
      | capabilities                                                                                   |
      | browserName=chrome,platform=ventura,version=latest,network=TRUE,autoHeal=TRUE,tags=[auto-heal] |

    @win10_regression
    Examples:
      | capabilities                                                                                  |
      | browserName=firefox,platform=win10,version=latest,network=TRUE,autoHeal=TRUE,tags=[auto-heal] |