//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.common.dto;

public class WorkspaceDTO {

    private String name;
    private String hostIp;
    private int port;

    public WorkspaceDTO() {
    }

    public WorkspaceDTO(String name, String hostIp, int port) {
        this.name = name;
        this.hostIp = hostIp;
        this.port = port;
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
}}