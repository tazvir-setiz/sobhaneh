package ir.sobhaneh.host;

import ir.sobhaneh.common.Connection;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class HostSideWorkspaceManager {
    private final ConcurrentHashMap<Integer, Workspace> workspaces = new ConcurrentHashMap<>();
    private final Connection centralConnection;

    public HostSideWorkspaceManager(Connection centralConnection) {
        this.centralConnection = centralConnection;
    }

    public String handleCreateWorkspace(int port, long userId){
        if(workspaces.containsKey(port)){
            return "ERROR Workspace already exists on this port";
        }
        try {
            Workspace newWorkspace = new Workspace(port, centralConnection);
            workspaces.put(port, newWorkspace);
            return "OK";
        } catch (IOException e) {
            return "ERROR " + e.getMessage();
        }
    }
}
