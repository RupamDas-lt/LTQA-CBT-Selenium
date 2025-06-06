@tunnel_regression @tunnel_verification
Feature: Automation of all tunnel test cases

  @tunnel_regression_1
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

  @tunnel_regression_2
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


  @tunnel_regression_3
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

  @tunnel_regression_4
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

  @tunnel_regression_5 @tunnel_mode
  Scenario Outline: TC-019: Tunnel Mode Configuration - Verify different tunnel modes
    Given Setup user details
    Then I start tunnel with <tunnelFlags>
    Then I start session with driver quit to test local with <capabilities>
    Then I verify tunnel connection uses <mode> protocol
    Then I stop tunnel

    Examples:
      | tunnelFlags | mode | capabilities                                             |
      | mode=ssh    | ssh  | browserName=chrome,platform=win10,version=.*,tunnel=true |
      | mode=tcp    | tcp  | browserName=chrome,platform=win10,version=.*,tunnel=true |
      | mode=ws     | ws   | browserName=chrome,platform=win10,version=.*,tunnel=true |

  @tunnel_regression_6 @ssh_connection_type
  Scenario Outline: TC-020: SSH Connection Type - Verify different SSH connection types
    Given Setup user details
    Then I start tunnel with <tunnelFlags>
    Then I start session with driver quit to test local with <capabilities>
    Then I verify tunnel uses <sshConnType> connection
    Then I stop tunnel

    Examples:
      | tunnelFlags                   | sshConnType | capabilities                                             |
      | mode=ssh sshConnType=over_22  | over_22     | browserName=chrome,platform=win10,version=.*,tunnel=true |
      | mode=ssh sshConnType=over_443 | over_443    | browserName=chrome,platform=win10,version=.*,tunnel=true |
      | mode=ssh sshConnType=over_ws  | over_ws     | browserName=chrome,platform=win10,version=.*,tunnel=true |


  @tunnel_regression_7 @default_ssh_connection
  Scenario Outline: TC-024: Default SSH Connection - Verify primary connection method
    Given Setup user details
    And I ensure port 22 is open
    Then I start tunnel
    Then I start session with driver quit to test local with <capabilities>
    Then I verify tunnel connects on port 22
    Then I stop tunnel

    Examples:
      | capabilities                                             |
      | browserName=chrome,platform=win10,version=.*,tunnel=true |

  @tunnel_regression_8 @ssh_443_fallback
  Scenario Outline: TC-025: SSH:443 Fallback - Verify fallback when port 22 is blocked
    Given Setup user details
    And I block port 22
    Then I start tunnel
    Then I start session with driver quit to test local with <capabilities>
    Then I verify tunnel connects on port 443 using SSH
    Then I stop tunnel
    Then I unblock port 22

    Examples:
      | capabilities                                             |
      | browserName=chrome,platform=win10,version=.*,tunnel=true |

  @tunnel_regression_9 @tcp_443_fallback
  Scenario Outline: TC-026: TCP:443 Fallback - Verify TCP fallback
    Given Setup user details
    And I block ports 22 and SSH:443
    Then I start tunnel
    Then I start session with driver quit to test local with <capabilities>
    Then I verify tunnel connects on port 443 using TCP
    Then I stop tunnel
    Then I unblock all ports

    Examples:
      | capabilities                                             |
      | browserName=chrome,platform=win10,version=.*,tunnel=true |

  @tunnel_regression_10 @websocket_fallback
  Scenario Outline: TC-012: WebSocket Fallback - Verify WebSocket fallback
    Given Setup user details
    And I block all SSH and TCP connections
    Then I start tunnel with <tunnelFlags>
    Then I start session with driver quit to test local with <capabilities>
    Then I verify tunnel connects using WebSocket
    Then I stop tunnel
    Then I unblock all ports

    Examples:
      | tunnelFlags | capabilities                                             |
      | mode=ws     | browserName=chrome,platform=win10,version=.*,tunnel=true |

  @tunnel_regression_11 @progressive_fallback
  Scenario Outline: TC-027: Progressive Fallback Testing - Verify sequential fallback mechanisms
    Given Setup user details
    Then I start tunnel
    And I verify tunnel connects on port 22
    Then I block port 22
    And I restart tunnel
    And I verify tunnel connects on port 443 using SSH
    Then I block ports 22 and SSH:443
    And I restart tunnel
    And I verify tunnel connects on port 443 using TCP
    Then I block all SSH and TCP connections
    And I restart tunnel with <tunnelFlags>
    And I verify tunnel connects using WebSocket
    Then I stop tunnel
    Then I unblock all ports

    Examples:
      | tunnelFlags | capabilities                                             |
      | mode=ws     | browserName=chrome,platform=win10,version=.*,tunnel=true |

  @tunnel_regression_12 @recovery_testing
  Scenario Outline: TC-028: Recovery Testing - Verify connection recovery after unblocking
    Given Setup user details
    And I block port 22
    Then I start tunnel
    And I verify tunnel connects on port 443 using SSH
    Then I unblock port 22
    And I restart tunnel
    And I verify tunnel connects on port 22
    Then I stop tunnel

    Examples:
      | capabilities                                             |
      | browserName=chrome,platform=win10,version=.*,tunnel=true |

  @tunnel_regression_13 @flags_with_fallback
  Scenario Outline: TC-029: Flags with Fallback - Verify flags work across different protocols
    Given Setup user details
    And I set up network restrictions according to <networkScenario>
    Then I start tunnel with <tunnelFlags>
    Then I start session with driver quit to test local with <capabilities>
    Then I verify tunnel uses <expectedProtocol> protocol
    And I verify all tunnel flags are applied correctly
    Then I stop tunnel
    Then I reset network restrictions

    Examples:
      | networkScenario         | tunnelFlags                           | expectedProtocol | capabilities                                             |
      | no_restrictions         | allowHosts=localhost:8080 dns=8.8.8.8 | ssh              | browserName=chrome,platform=win10,version=.*,tunnel=true |
      | block_port_22           | allowHosts=localhost:8080 dns=8.8.8.8 | ssh:443          | browserName=chrome,platform=win10,version=.*,tunnel=true |
      | block_all_but_websocket | allowHosts=localhost:8080 mode=ws     | ws               | browserName=chrome,platform=win10,version=.*,tunnel=true |

  @tunnel_regression_14 @connection_modes_verification
  Scenario Outline: TC-030: Connection Modes Verification - Test all SSH connection modes
    Given Setup user details
    Then I start tunnel with <tunnelFlags>
    Then I start session with driver quit to test local with <capabilities>
    Then I verify tunnel uses <expectedConnection> connection
    Then I stop tunnel

    Examples:
      | tunnelFlags                   | expectedConnection | capabilities                                             |
      | mode=ssh sshConnType=over_22  | over_22            | browserName=chrome,platform=win10,version=.*,tunnel=true |
      | mode=ssh sshConnType=over_443 | over_443           | browserName=chrome,platform=win10,version=.*,tunnel=true |
      | mode=ssh sshConnType=over_ws  | over_ws            | browserName=chrome,platform=win10,version=.*,tunnel=true |
      | mode=tcp                      | tcp                | browserName=chrome,platform=win10,version=.*,tunnel=true |
      | mode=ws                       | ws                 | browserName=chrome,platform=win10,version=.*,tunnel=true |

  @tunnel_regression_15 @mixed_flags_testing
  Scenario Outline: TC-031: Mixed Flags Testing - Verify complex flag combinations work with fallbacks
    Given Setup user details
    Then I start tunnel with <tunnelFlags>
    Then I start session with driver quit to test allowHost,bypassHost,local with <capabilities>
    Then I stop tunnel
    Then I verify network Log via API

    Examples:
      | tunnelFlags                                        | capabilities                                                                        |
      | allowHosts=*lambda* mode=ssh sshConnType=over_22   | browserName=chrome,platform=win10,version=.*,tunnel=true,name=Mixed_Flags_SSH_22    |
      | bypassHosts=*google* mode=ssh sshConnType=over_443 | browserName=chrome,platform=win10,version=.*,tunnel=true,name=Mixed_Flags_SSH_443   |
      | forceLocal mode=tcp                                | browserName=chrome,platform=win10,version=.*,tunnel=true,name=Mixed_Flags_TCP       |
      | allowHosts=localhost:* mode=ws                     | browserName=chrome,platform=win10,version=.*,tunnel=true,name=Mixed_Flags_WebSocket |