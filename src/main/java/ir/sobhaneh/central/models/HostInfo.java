//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.central.models;

import ir.sobhaneh.common.Connection;
import lombok.Getter;
import lombok.Setter;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
@Getter
@Setter
public class HostInfo {
    private final String ip;
    private final int startPort;
    private final int endPort;
    private final List<Integer> unusedPorts;
    private Connection connection;

    public HostInfo(String ip, int startPort, int endPort) {
        this.ip = ip;
        this.startPort = startPort;
        this.endPort = endPort;
        this.unusedPorts = new ArrayList<>();
        for (int port = startPort; port <= endPort; port++) {
            unusedPorts.add(port);
        }
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
