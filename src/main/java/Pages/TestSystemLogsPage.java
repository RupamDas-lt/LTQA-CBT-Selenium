package Pages;

import TestManagers.DriverManager;

public class TestSystemLogsPage extends LTDashboardCommonActions {

  DriverManager driver;

  public TestSystemLogsPage(String testId, DriverManager driverManager) {
    super(testId, driverManager);
    driver = driverManager;
  }

}
