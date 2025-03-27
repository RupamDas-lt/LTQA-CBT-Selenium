package DTOs.SwaggerAPIs;

import lombok.Data;

@Data public class LighthouseReportDTO {

  private Data data;
  private String message;
  private String status;

  @lombok.Data public static class Data {
    private String json_report;
    private String html_report;
  }
}