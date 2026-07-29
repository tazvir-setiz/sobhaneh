//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.central.models;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class HostInfo {
    private final String ip;
    private final int startPort;
    private final int endPort;
    private final List<Integer> unusedPorts;
    private Socket socket;

    public HostInfo(String ip, int startPort, int endPort) {
        this.ip = ip;
        this.startPort = startPort;
        this.endPort = endPort;
        this.unusedPorts = new ArrayList<>();
        for (int port = startPort; port <= endPort; port++) {
            unusedPorts.add(port);
        }
    }

    public String getIp() {
        return ip;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public boolean overlaps(int otherStart, int otherEnd) {
        return startPort <= otherEnd && otherStart <= endPort;
    }

    public synchronized int allocateRandomPort() {
        if (unusedPorts.isEmpty()) {
            return -1;
        }
        int randomIndex = ThreadLocalRandom.current().nextInt(unusedPorts.size());
        int chosenPort = unusedPorts.get(randomIndex);
        unusedPorts.remove(randomIndex);
        return chosenPort;
    }

    public synchronized void releasePort(int port) {
        if (port >= startPort && port <= endPort) {
            unusedPorts.add(port);
        }
    }
}
