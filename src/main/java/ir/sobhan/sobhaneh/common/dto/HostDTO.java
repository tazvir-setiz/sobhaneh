package ir.sobhan.sobhaneh.common.dto;

import java.util.HashSet;
import java.util.Iterator;

public class HostDTO {
    private String ip;
    private int startPort;
    private int endPort;
    private final HashSet<Integer> unusedPorts = new HashSet<>();

    public HostDTO(String ip, int startPort, int endPort) {
        this.ip = ip;
        this.startPort = startPort;
        this.endPort = endPort;
        for (int i = startPort; i <= endPort; i++) {
            unusedPorts.add(i);
        }
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

    public int getFreePort() {
        if(unusedPorts.isEmpty())
            return -1;
        Iterator<Integer> iterator = unusedPorts.iterator();
        return iterator.next().intValue();
    }
    public boolean occupyPort(int port) {
        if (unusedPorts.contains(port)) {
            unusedPorts.remove(port);
            return true;
        }
        return false;
    }
    public boolean releasePort(int port) {
        if(port >= startPort && port <= endPort) return false;
        if (unusedPorts.contains(port)) {
            return false;
        }
        unusedPorts.add(port);
        return true;
    }
}
