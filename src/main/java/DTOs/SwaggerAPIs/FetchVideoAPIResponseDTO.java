package DTOs.SwaggerAPIs;

import lombok.Data;

@Data public class FetchVideoAPIResponseDTO {
  private String message;
  private String status;
  private String url;
  private String view_video_url;
}
