package ir.sobhaneh.central;

import ir.sobhaneh.host.ChatStore;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class ChatArchiveManager {
    private final ConcurrentHashMap<String, ChatStore> workspacesChatStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map<Long, String>> workspaceUsernames = new ConcurrentHashMap<>();

    public void addWorkspaceUsernames(String workspaceName, Map<Long, String> usernames) {
        workspaceUsernames.put(workspaceName, usernames);
    }

    public Map<Long, String> getWorkspaceUsernames(String workspaceName) {
        return workspaceUsernames.get(workspaceName);
    }
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

    public Map<String, Map<Long, String>> exportWorkspacesUsernames() {
        return new java.util.HashMap<>(workspaceUsernames);
    }

    public void importWorkspacesUsernames(Map<String, Map<Long, String>> savedUsernames) {
        workspaceUsernames.clear();
        if (savedUsernames != null) {
            workspaceUsernames.putAll(savedUsernames);
        }
    }
}
