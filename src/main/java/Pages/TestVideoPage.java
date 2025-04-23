package Pages;

import TestManagers.DriverManager;

public class TestVideoPage extends LTDashboardCommonActions {

  DriverManager driver;

  public TestVideoPage(String testId, DriverManager driverManager) {
    super(testId, driverManager);
    driver = driverManager;
  }

}
