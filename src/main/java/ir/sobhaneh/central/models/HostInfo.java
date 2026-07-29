package ir.sobhaneh.central.models;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class HostInfo {
    private final String ip;
    private final int startPort;
    private final int endPort;
    private final Set<Integer> unusedPorts;
    private Socket socket;

    public HostInfo(String ip, int startPort, int endPort) {
        this.ip = ip;
        this.startPort = startPort;
        this.endPort = endPort;
        this.unusedPorts = ConcurrentHashMap.newKeySet();
        for (int port = startPort; port <= endPort; port++) {
            unusedPorts.add(port);
        }
    }

    public String getIp() { return ip; }
    public int getStartPort() { return startPort; }
    public int getEndPort() { return endPort; }
    public Socket getSocket() { return socket; }
    public void setSocket(Socket socket) { this.socket = socket; }

    public boolean overlaps(int otherStart, int otherEnd) {
        return startPort <= otherEnd && otherStart <= endPort;
    }

    public synchronized int allocateRandomPort() {
        if (unusedPorts.isEmpty()) return -1;
        int random = ThreadLocalRandom.current().nextInt(0, unusedPorts.size());
        int chosenPort = new ArrayList<>(unusedPorts).get(random);
        unusedPorts.remove(random);
        return chosenPort;
    }

    public void releasePort(int port) {
        if (port >= startPort && port <= endPort) {
            unusedPorts.add(port);
        }
    }
}