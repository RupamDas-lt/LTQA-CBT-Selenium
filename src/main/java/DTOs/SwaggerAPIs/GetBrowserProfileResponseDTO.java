package DTOs.SwaggerAPIs;

import lombok.Data;

import java.util.List;

@Data
public class GetBrowserProfileResponseDTO {
    private OrgMeta Meta;
    private List<UploadData> data;
    private String message;
    private String status;

    @Data
    public static class OrgMeta {
        private int org_id;
        private int total;
    }

    @Data
    public static class UploadData {
        private String key;
        private String last_modified_at;
        private int size;
        private String url;

    }
}
