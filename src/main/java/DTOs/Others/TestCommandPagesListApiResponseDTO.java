package DTOs.Others;

import lombok.Data;

import java.util.List;

@Data
public class TestCommandPagesListApiResponseDTO {
    private List<CommandLogFile> data;
    private String message;
    private String status;

    @Data
    public static class CommandLogFile {
        private String name;
        private String last_modified_at;
        private long size;
        private String capabilityUrl;
        private String file_path;
        private String page_start_time;
    }
}
