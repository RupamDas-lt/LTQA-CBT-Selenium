package DTOs.Others;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class TunnelInfoResponseDTO {
    private String status;
    private TunnelData data;

    @Data
    public static class TunnelData {
        private long id;

        @SerializedName("localProxyPort")
        private String localProxyPort;

        private String user;

        @SerializedName("tunnelName")
        private String tunnelName;

        private String environment;
        private String version;
        private String mode;

        @SerializedName("sshConnType")
        private String sshConnType;
    }
}