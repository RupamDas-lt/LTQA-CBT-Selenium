package Pages;

import TestManagers.DriverManager;

public class TestConsoleLogsPage extends LTDashboardCommonActions {

  DriverManager driver;

  public TestConsoleLogsPage(String testId, DriverManager driverManager) {
    super(testId, driverManager);
    driver = driverManager;
  }

}
