package ir.sobhaneh.host;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class HostSideWorkspaceManager {
    private final ConcurrentHashMap<Integer, Workspace> workspaces = new ConcurrentHashMap<>();
    public String handleCreateWorkspace(int port, long userId){
        if(workspaces.containsKey(port)){
            return "ERROR Workspace already exists on this port";
        }
        try {
            Workspace newWorkspace = new Workspace(port);
            workspaces.put(port, newWorkspace);
            return "OK";
        } catch (IOException e) {
            return "ERROR " + e.getMessage();
        }
    }
}
