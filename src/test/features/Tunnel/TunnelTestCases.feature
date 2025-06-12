@tunnel_regression @tunnel_verification
Feature: Automation of all tunnel test cases

  @tunnel_regression_1 @tunnel_basic_sanity
  Scenario Outline: User is able to check allowHosts flag for tunnel
    Given Setup user details
    Then I start tunnel with <tunnelFlags>
    Then I start session with driver quit to test allowHost,publicWebsitesResolutionCheckForAllowHosts,selfSigned,local with <capabilities>
    Then I stop tunnel
    Then I verify network Log via API

    Examples:
      | tunnelFlags         | capabilities                                                                                                               |
      | allowHosts=*lambda* | browserName=chrome,platform=ventura,version=latest,tunnel=true,network=false,name=Tunnel_With_AllowHosts_And_Network_False |
      | allowHosts=*lambda* | browserName=chrome,platform=sequoia,version=latest,tunnel=true,network=true,name=Tunnel_With_AllowHosts_And_Network_True   |

  @tunnel_regression_2 @tunnel_basic_sanity
  Scenario Outline: User is able to check bypassHosts flag for tunnel
    Given Setup user details
    Then I start tunnel with <tunnelFlags>
    Then I start session with driver quit to test bypassHost,publicWebsitesResolutionCheckForBypassHosts,selfSigned,local with <capabilities>
    Then I stop tunnel
    Then I verify network Log via API

    Examples:
      | tunnelFlags          | capabilities                                                                                                                |
      | bypassHosts=*lambda* | browserName=chrome,platform=ventura,version=latest,tunnel=true,network=false,name=Tunnel_With_BypassHosts_And_Network_False |
      | bypassHosts=*lambda* | browserName=chrome,platform=sonoma,version=latest,tunnel=true,network=true,name=Tunnel_With_BypassHosts_And_Network_True    |


  @tunnel_regression_3 @tunnel_basic_sanity
  Scenario Outline: User is able to check forceLocal flag for tunnel
    Given Setup user details
    Then I start tunnel with <tunnelFlags>
    Then I start session with driver quit to test publicWebsitesResolutionCheckForForceLocal,selfSigned,local with <capabilities>
    Then I stop tunnel
    Then I verify network Log via API

    Examples:
      | tunnelFlags | capabilities                                                                                                              |
      | forceLocal  | browserName=chrome,platform=sonoma,version=latest,tunnel=true,network=false,name=Tunnel_With_ForceLocal_And_Network_False |
      | forceLocal  | browserName=chrome,platform=sequoia,version=latest,tunnel=true,network=true,name=Tunnel_With_ForceLocal_And_Network_True  |

  @tunnel_regression_4 @tunnel_basic_sanity
  Scenario Outline: User is able to check if public website is resolved in expected place based on ml_resolve_tunnel_website_in_dc flag
    Given Setup user details
    Then I start tunnel
    Then I start session with driver quit to test localWithCustomDomain,publicWebsitesResolutionCheckForDefaultFlags,selfSigned,local with <capabilities>
    Then I stop tunnel
    Then I verify network Log via API

    Examples:
      | capabilities                                                                                                                  |
      | browserName=chrome,platform=ventura,version=latest,tunnel=true,network=false,name=Tunnel_With_Default_Flags_And_Network_False |
      | browserName=chrome,platform=sequoia,version=latest,tunnel=true,network=true,name=Tunnel_With_Default_Flags_And_Network_True   |

  @tunnel_regression_5 @tunnel_mode @tunnel_basic_sanity
  Scenario Outline: Tunnel Mode Configuration - Verify different tunnel modes
    Given Setup user details
    Then I start tunnel with <tunnelFlags>
    Then I start session with driver quit to test local with <capabilities>
    Then I verify tunnel connection uses <mode> protocol
    Then I stop tunnel

    Examples:
      | tunnelFlags | mode | capabilities                                             |
      | mode ssh    | ssh  | browserName=chrome,platform=win10,version=.*,tunnel=true |
      | mode tcp    | tcp  | browserName=chrome,platform=win10,version=.*,tunnel=true |
      | mode ws     | ws   | browserName=chrome,platform=win10,version=.*,tunnel=true |

  @tunnel_regression_6 @ssh_connection_type @tunnel_basic_sanity
  Scenario Outline: SSH Connection Type - Verify different SSH connection types
    Given Setup user details
    Then I start tunnel with <tunnelFlags>
    Then I start session with driver quit to test local with <capabilities>
    Then I verify tunnel uses <sshConnType> connection
    Then I stop tunnel

    Examples:
      | tunnelFlags                    | sshConnType | capabilities                                             |
      | mode ssh--sshConnType over_22  | over_22     | browserName=chrome,platform=win10,version=.*,tunnel=true |
      | mode ssh--sshConnType over_443 | over_443    | browserName=chrome,platform=win10,version=.*,tunnel=true |
      | mode ssh--sshConnType over_ws  | over_ws     | browserName=chrome,platform=win10,version=.*,tunnel=true |

  @tunnel_regression_7 @connection_modes_verification @flaky
  Scenario Outline: Connection Modes Verification - Test all SSH connection modes
    Given Setup user details
    Then I start tunnel with <tunnelFlags>
    Then I start session with driver quit to test local with <capabilities>
    Then I verify tunnel uses <expectedConnection> connection
    Then I stop tunnel

    Examples:
      | tunnelFlags                    | expectedConnection | capabilities                                             |
      | mode ssh--sshConnType over_22  | over_22            | browserName=chrome,platform=win10,version=.*,tunnel=true |
      | mode ssh--sshConnType over_443 | over_443           | browserName=chrome,platform=win10,version=.*,tunnel=true |
      | mode ssh--sshConnType over_ws  | over_ws            | browserName=chrome,platform=win10,version=.*,tunnel=true |
      | mode tcp                       | tcp                | browserName=chrome,platform=win10,version=.*,tunnel=true |
      | mode ws                        | ws                 | browserName=chrome,platform=win10,version=.*,tunnel=true |

  @tunnel_regression_8 @mixed_flags_testing @flaky
  Scenario Outline: Mixed Flags Testing - Verify complex flag combinations work with fallbacks
    Given Setup user details
    Then I start tunnel with <tunnelFlags>
    Then I start session with driver quit to test allowHost,bypassHost,local with <capabilities>
    Then I stop tunnel
    Then I verify network Log via API

    Examples:
      | tunnelFlags                                          | capabilities                                                                        |
      | allowHosts=*lambda*--mode ssh--sshConnType over_22   | browserName=chrome,platform=win10,version=.*,tunnel=true,name=Mixed_Flags_SSH_22    |
      | bypassHosts=*google*--mode ssh--sshConnType over_443 | browserName=chrome,platform=win10,version=.*,tunnel=true,name=Mixed_Flags_SSH_443   |
      | forceLocal--mode tcp                                 | browserName=chrome,platform=win10,version=.*,tunnel=true,name=Mixed_Flags_TCP       |
      | allowHosts=localhost:*--mode ws                      | browserName=chrome,platform=win10,version=.*,tunnel=true,name=Mixed_Flags_WebSocket |