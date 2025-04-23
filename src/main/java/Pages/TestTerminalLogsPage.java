package Pages;

import TestManagers.DriverManager;

public class TestTerminalLogsPage extends LTDashboardCommonActions {

  DriverManager driver;

  public TestTerminalLogsPage(String testId, DriverManager driverManager) {
    super(testId, driverManager);
    driver = driverManager;
  }

}
