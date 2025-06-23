package DTOs.SwaggerAPIs;

import lombok.Data;

import java.util.List;

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
    private String test_type;
    private int duration;
    private String platform;
    private String browser;
    private String browser_version;
    private String device;
    private String status_ind;
    private String test_execution_status;
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
    private GeoInfo geoInfo;
    private List<String> tags;
    private String public_url;
    private String resolution;
    private boolean autohealed;
    private CustomData customData;
  }

  @lombok.Data public static class GeoInfo {

    private String country;
    private String provider;
    private String region;
    private String regionName;
    private String state;
  }

  @lombok.Data public static class CustomData {
    // Define any fields if necessary for customData
  }
}
