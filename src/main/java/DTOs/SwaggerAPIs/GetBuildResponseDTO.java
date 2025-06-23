package DTOs.SwaggerAPIs;

import lombok.Data;

import java.util.List;

@Data public class GetBuildResponseDTO {
  private Data data;
  private String message;
  private String status;

  @lombok.Data public static class Data {
    private int build_id;
    private String name;
    private int org_id;
    private int user_id;
    private String username;
    private String status_ind;
    private String create_timestamp;
    private String end_timestamp;
    private String project_id;
    private String project_key;
    private String project_name;
    private List<String> tags;
    private String public_url;
    private int duration;
    private String dashboard_url;
    private boolean autohealed;
  }
}
