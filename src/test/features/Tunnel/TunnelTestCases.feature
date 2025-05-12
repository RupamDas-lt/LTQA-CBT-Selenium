@tunnel_regression @tunnel_verification
Feature: Automation of all tunnel test cases

  @tunnel_regression_1
  Scenario Outline: User is able to check allowHosts flag for tunnel
    Given Setup user details
    Then I start tunnel with <tunnelFlags>
    Then I start session with driver quit to test allowHost,selfSigned with <capabilities>

    Examples:
      | tunnelFlags         | capabilities                                                                                                             |
      | allowHosts=*lambda* | browserName=chrome,platform=win10,version=latest,tunnel=true,network=false,name=Tunnel_With_AllowHosts_And_Network_False |
      | allowHosts=*lambda* | browserName=chrome,platform=win10,version=latest,tunnel=true,network=true,name=Tunnel_With_AllowHosts_And_Network_True   |

  @tunnel_regression_2
  Scenario Outline: User is able to check bypassHosts flag for tunnel
    Given Setup user details
    Then I start tunnel with <tunnelFlags>
    Then I start session with driver quit to test bypassHost,selfSigned with <capabilities>

    Examples:
      | tunnelFlags          | capabilities                                                                                                              |
      | bypassHosts=*lambda* | browserName=chrome,platform=win10,version=latest,tunnel=true,network=false,name=Tunnel_With_BypassHosts_And_Network_False |
      | bypassHosts=*lambda* | browserName=chrome,platform=win10,version=latest,tunnel=true,network=true,name=Tunnel_With_BypassHosts_And_Network_True   |


  @tunnel_regression_2
  Scenario Outline: User is able to check forceLocal flag for tunnel
    Given Setup user details
    Then I start tunnel with <tunnelFlags>
    Then I start session with driver quit to test local,forceLocal,selfSigned with <capabilities>

    Examples:
      | tunnelFlags | capabilities                                                                                                             |
      | forceLocal  | browserName=chrome,platform=win10,version=latest,tunnel=true,network=false,name=Tunnel_With_ForceLocal_And_Network_False |
      | forceLocal  | browserName=chrome,platform=win10,version=latest,tunnel=true,network=true,name=Tunnel_With_ForceLocal_And_Network_True   |