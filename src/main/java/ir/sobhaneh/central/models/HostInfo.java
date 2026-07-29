package ir.sobhaneh.central.models;

import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HostInfo {
    private String ip;
    private int startPort;
    private int endPort;
    private Set<Integer> usedPorts;
    Socket socket;
    public HostInfo(String ip, int startPort, int endPort) {
        this.ip = ip;
        this.startPort = startPort;
        this.endPort = endPort;
        usedPorts = ConcurrentHashMap.newKeySet();
    }
}
