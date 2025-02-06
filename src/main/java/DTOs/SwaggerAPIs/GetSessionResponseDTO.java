package DTOs.SwaggerAPIs;

import lombok.Data;

@Data public class GetSessionResponseDTO {
  private Data data;
  private String message;
  private String status;

  @lombok.Data public static class Data {
    private String test_id;
    private int build_id;
    private String name;
    private int user_id;
    private String username;
    private int duration;
    private String platform;
    private String browser;
    private String browser_version;
    private String device;
    private String status_ind;
    private String session_id;
    private String build_name;
    private String create_timestamp;
    private String start_timestamp;
    private String end_timestamp;
    private String remark;
    private String console_logs_url;
    private String network_logs_url;
    private String command_logs_url;
    private String selenium_logs_url;
    private String screenshot_url;
    private String video_url;
    private CustomData customData;
  }

  @lombok.Data public static class CustomData {
    // Define any fields if necessary for customData
  }
}
