package DTOs.Others;

import lombok.Data;

@Data public class TunnelInfoResponseDTO {
  private String status;
  private TunnelData data;

  @Data public static class TunnelData {
    private long id;
    private String localProxyPort;
    private String user;
    private String tunnelName;
    private String environment;
    private String version;
    private String mode;
    private String sshConnType;
  }
}
