//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.host;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class HostSideWorkspaceManager {
    private final ConcurrentHashMap<Integer, Workspace> workspaces = new ConcurrentHashMap<>();
    private CentralConnectionListener centralConnectionListener;

    public void setCentralConnectionListener(CentralConnectionListener centralConnectionListener) {
        this.centralConnectionListener = centralConnectionListener;
    }

    public String handleCreateWorkspace(int port, long userId) {
        if (workspaces.containsKey(port)) {
            System.out.println("[HostSideWorkspaceManager] Workspace already exists on port=" + port);
            return "ERROR Workspace already exists on this port";
        }
        try {
            Workspace newWorkspace = new Workspace(port, centralConnectionListener);
            workspaces.put(port, newWorkspace);
            System.out.println("[HostSideWorkspaceManager] Created workspace on port=" + port + " for userId=" + userId);
            return "OK";
        } catch (IOException e) {
            System.out.println("[HostSideWorkspaceManager] Failed to create workspace on port=" + port + ": " + e.getMessage());
            return "ERROR " + e.getMessage();
        }
    }
}