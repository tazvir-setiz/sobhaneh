package ir.sobhaneh.central;

import java.util.Random;

public class HostInfo {
    private final String ip;
    private final int startPort;
    private final int endPort;
    private final int assignedPort;

    public HostInfo(final String ip, final int startPort, final int endPort) {
        this.ip = ip;
        this.startPort = startPort;
        this.endPort = endPort;
        Random rand = new Random();
        this.assignedPort = rand.nextInt(endPort - startPort + 1) + startPort;
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
}
