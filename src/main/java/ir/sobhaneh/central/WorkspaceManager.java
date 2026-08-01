//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.central;

import ir.sobhaneh.central.models.HostInfo;
import ir.sobhaneh.central.models.WorkspaceInfo;
import ir.sobhaneh.common.Connection;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkspaceManager {
    private static final String RESPONSE_OK = "OK";

    public final Object createLock = new Object();
    private final ConcurrentHashMap<String, WorkspaceInfo> workspaces = new ConcurrentHashMap<>();

    public String createWorkspace(String name, long creatorUserId, List<HostInfo> registeredHosts) throws IOException {
        synchronized (createLock) {
            if (workspaces.containsKey(name)) {
                return "ERROR Workspace already exists";
            }

            AtomicInteger allocatedPort = new AtomicInteger(-1);
            HostInfo allocatedHost = allocateHost(registeredHosts, allocatedPort);

            if (allocatedHost == null) {
                return "ERROR No available host";
            }

            int port = allocatedPort.get();
            boolean confirmed = notifyHost(allocatedHost, port, creatorUserId);

            if (!confirmed) {
                allocatedHost.releasePort(port);
                return "ERROR Host failed to create workspace";
            }

            WorkspaceInfo info = new WorkspaceInfo(name, allocatedHost.getIp(), port, creatorUserId);
            workspaces.put(name, info);

            return RESPONSE_OK + " " + allocatedHost.getIp() + " " + port;
        }
    }

    private HostInfo allocateHost(List<HostInfo> hosts, AtomicInteger port) {
        for (HostInfo host : hosts) {
            port.set(host.allocateRandomPort());
            if (port.get() != -1) {
                return host;
            }
        }
        return null;
    }

    private boolean notifyHost(HostInfo host, int port, long creatorUserId) throws IOException {
        Connection hostConnection = new Connection(host.getSocket());
        hostConnection.sendLine("create-workspace " + port + " " + creatorUserId);
        String response = hostConnection.readLine();
        return RESPONSE_OK.equals(response);
    }
}