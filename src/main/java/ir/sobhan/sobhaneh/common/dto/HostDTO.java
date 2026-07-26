//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.common.dto;

import ir.sobhan.sobhaneh.common.response.ProtocolFormattable;

import java.util.HashSet;
import java.util.Iterator;

public class HostDTO implements ProtocolFormattable {
    private final String ip;
    private final int startPort;
    private final int endPort;
    private final HashSet<Integer> freePorts = new HashSet<>();

    public int getReservedPortCheck() {
        return reservedPortCheck;
    }

    public void setReservedPortCheck(int reservedPortCheck) {
        this.reservedPortCheck = reservedPortCheck;
    }

    private int reservedPortCheck;

    public HostDTO(String ip, int startPort, int endPort) {
        this.ip = ip;
        this.startPort = startPort;
        this.endPort = endPort;
        for (int i = startPort; i <= endPort; i++) {
            freePorts.add(i);
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
        if (freePorts.isEmpty())
            return -1;
        Iterator<Integer> iterator = freePorts.iterator();
        return iterator.next();
    }

    public boolean occupyPort(int port) {
        if (freePorts.contains(port)) {
            freePorts.remove(port);
            return true;
        }
        return false;
    }

    public boolean releasePort(int port) {
        if (!(port >= startPort && port <= endPort)) return false;
        if (freePorts.contains(port)) {
            return false;
        }
        freePorts.add(port);
        return true;
    }

    public synchronized int reserveFreePort() {
        if (freePorts.isEmpty()) return -1;
        int port = getFreePort();
        occupyPort(port);
        return port;
    }

    @Override
    public String toProtocolString() {
        return String.valueOf(getFreePort());
    }
}
