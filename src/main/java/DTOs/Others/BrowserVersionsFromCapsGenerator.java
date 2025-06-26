package DTOs.Others;

import lombok.Data;

import java.util.ArrayList;

@Data
public class BrowserVersionsFromCapsGenerator {
    private VersionDTO def;
    private ArrayList<VersionDTO> versions;

    @Data
    public static class VersionDTO {
        private String id;
        private String version;
        private String channel_type;
    }
}