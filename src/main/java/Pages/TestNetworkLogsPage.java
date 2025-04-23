package Pages;

import TestManagers.DriverManager;

public class TestNetworkLogsPage extends LTDashboardCommonActions {

  DriverManager driver;

  public TestNetworkLogsPage(String testId, DriverManager driverManager) {
    super(testId, driverManager);
    driver = driverManager;
  }

}
