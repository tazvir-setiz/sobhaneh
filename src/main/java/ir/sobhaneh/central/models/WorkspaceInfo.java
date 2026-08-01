//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.central.models;

public class WorkspaceInfo {
    private final String name;
    private final String hostIp;
    private final int port;
    private final long creatorUserId;

    public WorkspaceInfo(String name, String hostIp, int port, long creatorUserId) {
        this.name = name;
        this.hostIp = hostIp;
        this.port = port;
        this.creatorUserId = creatorUserId;
    }

    public String getName() {
        return name;
    }

    public String getHostIp() {
        return hostIp;
    }

    public int getPort() {
        return port;
    }

    public long getCreatorUserId() {
        return creatorUserId;
    }
}
