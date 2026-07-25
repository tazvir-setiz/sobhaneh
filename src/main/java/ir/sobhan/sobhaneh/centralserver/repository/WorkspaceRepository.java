package ir.sobhan.sobhaneh.centralserver.repository;

import ir.sobhan.sobhaneh.common.dto.WorkspaceDTO;

import java.util.HashMap;

public class WorkspaceRepository {
    private static final HashMap<String, WorkspaceDTO> workspaces = new HashMap<>();
    private WorkspaceRepository() {}
    public static boolean addWorkspace(WorkspaceDTO workspaceDTO) {
        if (workspaces.containsKey(workspaceDTO.getName())) {
            return false;
        }
        workspaces.put(workspaceDTO.getName(), workspaceDTO);
        return true;
    }
    public static boolean removeWorkspace(String name) {
        if (workspaces.containsKey(name)) {
            workspaces.remove(name);
            return true;
        }
        return false;
    }
    public static WorkspaceDTO findByName(String name) {
        return workspaces.get(name);
    }
    public static HashMap<String, WorkspaceDTO> getAll() {
        return workspaces;
    }
}
