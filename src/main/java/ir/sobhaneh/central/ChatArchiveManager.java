package ir.sobhaneh.central;

import ir.sobhaneh.host.ChatStore;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class ChatArchiveManager {
    private final ConcurrentHashMap<String, ChatStore> workspacesChatStore = new ConcurrentHashMap<>();

    public void addWorkspace(String workspaceName, ChatStore chatStore) {
        workspacesChatStore.put(workspaceName, chatStore);
    }

    public ChatStore getWorkspaceChatStore(String workspaceName) {
        return workspacesChatStore.get(workspaceName);
    }

    public Map<String, ChatStore> exportWorkspacesChatStore() {
        return workspacesChatStore;
    }

    public void importWorkspacesChatStore(Map<String, ChatStore> workspacesChatStore) {
        this.workspacesChatStore.clear();
        this.workspacesChatStore.putAll(workspacesChatStore);
    }
}
