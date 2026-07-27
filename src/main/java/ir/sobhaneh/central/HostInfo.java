

package ir.sobhaneh.central;

import java.util.Random;

public class HostInfo {
    private final String ip;
    private final int startPort;
    private final int endPort;
    private final int assignedPort;
    private boolean verified = false;
    public HostInfo(final String ip, final int startPort, final int endPort, int assignedPort) {
        this.ip = ip;
        this.startPort = startPort;
        this.endPort = endPort;
        this.assignedPort = assignedPort;
    }

    public String getIp() {
        return ip;
    }

    public int getStartPort() {
        return startPort;
    }

    public int getEndPort() {
        return endPort;
    }

    public int getAssignedPort() {
        return assignedPort;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
