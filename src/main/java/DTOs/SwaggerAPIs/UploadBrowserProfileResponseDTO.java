package DTOs.SwaggerAPIs;

import lombok.Data;

import java.util.List;

@Data
public class UploadBrowserProfileResponseDTO {
    private List<UploadData> data;
    private String status;
 
    @Data
    public static class UploadData {
        private String error;
        private String url;
        private String message;

    }
}
