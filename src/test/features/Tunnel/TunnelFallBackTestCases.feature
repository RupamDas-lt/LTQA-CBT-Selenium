@tunnel_regression @tunnel_fallback_regression @tunnel_verification
Feature: Automation of all tunnel fallback test cases


  @tunnel_fallback_regression_1 @default_ssh_connection
  Scenario Outline: Default SSH Connection - Verify primary connection method
    Given Setup user details
    And I ensure port 22 is open
    Then I start tunnel
    Then I start session with driver quit to test local with <capabilities>
    Then I verify tunnel connects on port 22
    Then I stop tunnel

    Examples:
      | capabilities                                             |
      | browserName=chrome,platform=win10,version=.*,tunnel=true |

  @tunnel_fallback_regression_2 @ssh_443_fallback
  Scenario Outline: SSH:443 Fallback - Verify fallback when port 22 is blocked
    Given Setup user details
    And I set up network restrictions according to block_port_22
    Then I start tunnel
    Then I start session with driver quit to test local with <capabilities>
    Then I verify tunnel connects on port 443 using SSH
    Then I stop tunnel
    And I ensure port 22 is open

    Examples:
      | capabilities                                             |
      | browserName=chrome,platform=win10,version=.*,tunnel=true |

  @tunnel_fallback_regression_3 @tcp_443_fallback
  Scenario Outline: TCP:443 Fallback - Verify TCP fallback
    Given Setup user details
    And I set up network restrictions according to block_ssh_ports
    Then I start tunnel
    Then I start session with driver quit to test local with <capabilities>
    Then I verify tunnel connects on port 443 using TCP
    Then I stop tunnel
    And I set up network restrictions according to no_restrictions

    Examples:
      | capabilities                                             |
      | browserName=chrome,platform=win10,version=.*,tunnel=true |

  @tunnel_fallback_regression_4 @websocket_fallback
  Scenario Outline: WebSocket Fallback - Verify WebSocket fallback
    Given Setup user details
    # And I set up network restrictions according to block_all_ssh_tcp
    Then I start tunnel with <tunnelFlags>
    Then I start session with driver quit to test local with <capabilities>
    Then I verify tunnel connects using WebSocket
    Then I stop tunnel
    # And I set up network restrictions according to no_restrictions

    Examples:
      | tunnelFlags | capabilities                                             |
      | mode ws     | browserName=chrome,platform=win10,version=.*,tunnel=true |

  @tunnel_fallback_regression_5 @progressive_fallback
  Scenario Outline: Progressive Fallback Testing - Verify sequential fallback mechanisms
    Given Setup user details
    Then I start tunnel
    And I verify tunnel connects on port 22
    And I set up network restrictions according to block_port_22
    And I restart tunnel
    And I verify tunnel connects on port 443 using SSH
    And I set up network restrictions according to block_ssh_ports
    And I restart tunnel
    And I verify tunnel connects on port 443 using TCP
    And I set up network restrictions according to block_all_ssh_tcp
    And I restart tunnel with <tunnelFlags>
    And I verify tunnel connects using WebSocket
    Then I stop tunnel
    And I set up network restrictions according to no_restrictions

    Examples:
      | tunnelFlags | capabilities                                             |
      | mode=ws     | browserName=chrome,platform=win10,version=.*,tunnel=true |

  @tunnel_fallback_regression_6 @recovery_testing
  Scenario Outline: Recovery Testing - Verify connection recovery after unblocking
    Given Setup user details
    And I set up network restrictions according to block_port_22
    Then I start tunnel
    And I verify tunnel connects on port 443 using SSH
    And I ensure port 22 is open
    And I restart tunnel
    And I verify tunnel connects on port 22
    Then I stop tunnel

    Examples:
      | capabilities                                             |
      | browserName=chrome,platform=win10,version=.*,tunnel=true |

  @tunnel_fallback_regression_7 @flags_with_fallback
  Scenario Outline: Flags with Fallback - Verify flags work across different protocols
    Given Setup user details
    And I set up network restrictions according to <networkScenario>
    Then I start tunnel with <tunnelFlags>
    Then I start session with driver quit to test local with <capabilities>
    Then I verify tunnel uses <expectedProtocol> protocol
    And I verify all tunnel flags are applied correctly
    Then I stop tunnel
    And I set up network restrictions according to no_restrictions

    Examples:
      | networkScenario   | tunnelFlags                           | expectedProtocol | capabilities                                             |
      | no_restrictions   | allowHosts=localhost:8080 dns=8.8.8.8 | ssh              | browserName=chrome,platform=win10,version=.*,tunnel=true |
      | block_port_22     | allowHosts=localhost:8080 dns=8.8.8.8 | ssh:443          | browserName=chrome,platform=win10,version=.*,tunnel=true |
      | block_all_ssh_tcp | allowHosts=localhost:8080 mode=ws     | ws               | browserName=chrome,platform=win10,version=.*,tunnel=true |
