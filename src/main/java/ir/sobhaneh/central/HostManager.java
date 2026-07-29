//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.central;

import ir.sobhaneh.central.models.HostInfo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

public class HostManager {
    private static final int MIN_START_PORT = 10000;
    private static final int MAX_RANGE_SIZE = 1000;

    private final List<HostInfo> registeredHosts = new CopyOnWriteArrayList<>();
    private final List<HostInfo> pendingHosts = new CopyOnWriteArrayList<>();

    public synchronized ReservationResult reserve(String ip, int startPort, int endPort) {
        String error = validate(ip, startPort, endPort);
        if (error != null) {
            return ReservationResult.failure(error);
        }

        HostInfo hostInfo = new HostInfo(ip, startPort, endPort);
        int port = hostInfo.allocateRandomPort();
        pendingHosts.add(hostInfo);

        return ReservationResult.success(hostInfo, port);
    }

    private String validate(String ip, int startPort, int endPort) {
        if (!isValidIp(ip)) {
            return "ERROR Invalid IP address";
        }
        if (startPort < MIN_START_PORT) {
            return "ERROR Start port must be >= " + MIN_START_PORT;
        }
        if (endPort < startPort) {
            return "ERROR End port must be >= start port";
        }
        if (endPort - startPort + 1 > MAX_RANGE_SIZE) {
            return "ERROR Port range too large (max " + MAX_RANGE_SIZE + ")";
        }
        if (overlapsExisting(startPort, endPort)) {
            return "ERROR Port in use by another host";
        }
        return null;
    }

    private boolean isValidIp(String ip) {
        return true;
    }

    private boolean overlapsExisting(int startPort, int endPort) {
        for (HostInfo h : registeredHosts) {
            if (h.overlaps(startPort, endPort)) {
                return true;
            }
        }
        for (HostInfo h : pendingHosts) {
            if (h.overlaps(startPort, endPort)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void confirm(HostInfo hostInfo) {
        pendingHosts.remove(hostInfo);
        registeredHosts.add(hostInfo);
    }

    public synchronized void cancel(HostInfo hostInfo) {
        pendingHosts.remove(hostInfo);
    }

    public List<HostInfo> getRegisteredHosts() {
        return registeredHosts;
    }
}
