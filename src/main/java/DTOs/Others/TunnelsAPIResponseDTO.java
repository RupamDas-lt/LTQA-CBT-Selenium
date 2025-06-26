package DTOs.Others;

import lombok.Data;

import java.util.ArrayList;

@Data
public class TunnelsAPIResponseDTO {

    private String message;
    private String status;
    private ArrayList<TunnelData> data;

    @Data
    public static class TunnelData {
        private String dns;
        private String email;
        private String name;
        private String local_domains;
        private int org_id;
        private int user_id;
        private String start_timestamp;
        private String status_ind;
        private int tunnel_id;
        private String tunnel_name;
        private boolean shared_tunnel;
        private FolderPath folder_path;
    }

    @Data
    public static class FolderPath {
        private String String;
        private boolean Valid;
    }
}
