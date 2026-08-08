package ir.sobhaneh.central.persistence;

import ir.sobhaneh.central.ChatArchiveManager;
import ir.sobhaneh.central.UserManager;
import ir.sobhaneh.central.WorkspaceManager;
import ir.sobhaneh.host.ChatStore;

import java.io.*;
import java.util.Map;

public class DataStore {
    private static final String FILE_PATH = "central-data.ser";

    public void save(UserManager userManager, WorkspaceManager workspaceManager, ChatArchiveManager chatArchiveManager) throws IOException {
        CentralPersistedState centralPersistedState = new CentralPersistedState(
                userManager.exportUsers(),
                workspaceManager.exportWorkspaces(),
                chatArchiveManager.getWorkspacesChatStore(),
                chatArchiveManager.exportWorkspacesUsernames()
        );
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(centralPersistedState);
        }
    }

    public void load(UserManager userManager, WorkspaceManager workspaceManager, ChatArchiveManager chatArchiveManager) throws IOException {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            CentralPersistedState centralPersistedState = (CentralPersistedState) ois.readObject();
            userManager.importUsers(centralPersistedState.users());
            workspaceManager.importWorkspaces(centralPersistedState.workspaces());
            Map<String, ChatStore> chats = centralPersistedState.workspacesChats();
            chatArchiveManager.importWorkspacesChatStore(chats != null ? chats : new java.util.HashMap<>());
            chatArchiveManager.importWorkspacesUsernames(centralPersistedState.workspacesUsernames());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
