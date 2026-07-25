//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.common.dto;

public class WorkspaceDTO {

    private String name;
    private String hostIp;
    private int port;
    private final int ownerId;

    public WorkspaceDTO(String name, String hostIp, int port,  int ownerId) {
        this.name = name;
        this.hostIp = hostIp;
        this.port = port;
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getOwnerId() {
        return ownerId;
    }
}