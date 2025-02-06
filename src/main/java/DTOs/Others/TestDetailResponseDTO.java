package DTOs.Others;

import java.util.List;

public class TestDetailResponseDTO {
  public Meta Meta;
  public List<TestData> data;

  public static class Meta {
    public Attributes attributes;
    public ResultSet result_set;
  }

  public static class Attributes {
    public int org_id;
  }

  public static class ResultSet {
    public int count;
    public int limit;
    public int offset;
    public int total;
  }

  public static class TestData {
    public String test_id;
    public int build_id;
    public String build_name;
    public int user_id;
    public String username;
    public String status_ind;
    public String create_timestamp;
    public String end_timestamp;
    public String remark;
    public String browser;
    public String platform;
    public String version;
    public String name;
    public String session_id;
    public String duration;
    public String test_type;
    public String selenium_logs;
    public String console_logs;
    public String network_logs;
    public String command_logs;
    public String video_url;
    public String screenshot_url;
  }
}
