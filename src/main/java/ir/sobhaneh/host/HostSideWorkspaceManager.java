//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.host;

import com.google.gson.Gson;
import ir.sobhaneh.host.models.ChatStoreDto;
import lombok.Setter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class HostSideWorkspaceManager {
    private final ConcurrentHashMap<Integer, Workspace> workspaces = new ConcurrentHashMap<>();
    @Setter
    private CentralConnectionListener centralConnectionListener;

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

    public List<Workspace> getWorkspaces() {
        return (List<Workspace>) workspaces.values();
    }

    public void shutdownAndPushChats() {
        List<Workspace> workspaces = getWorkspaces();
        workspaces.forEach((workspace) -> {
            try {
                centralConnectionListener.sendAndWait("push-chats " +
                        workspace.getPort() + " " +
                        new Gson().toJson(workspace, Workspace.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean restoreChatStore(int port, String json) {
        Workspace workspace = workspaces.get(port);
        if (workspace == null) {
            return false;
        }
        ChatStoreDto dto = new JsonMapper().chatStoreDataFromJson(json);
        ChatStore restored = ChatStore.fromChatStoreData(dto);
        workspace.setChatStore(restored);
        return true;
    }

    public String getChatStoreJson(int port) {
        Workspace workspace = workspaces.get(port);
        if (workspace == null) {
            return null;
        }
        ChatStoreDto dto = workspace.getChatStore().toChatStoreData();
        return new JsonMapper().chatStoreDataToJson(dto);
    }
}