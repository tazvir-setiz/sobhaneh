package ir.sobhaneh.central.persistence;

import ir.sobhaneh.central.UserManager;
import ir.sobhaneh.central.WorkspaceManager;

import java.io.*;

public class DataStore {
    private static final String FILE_PATH = "central-data.ser";

    public void save(UserManager userManager, WorkspaceManager workspaceManager) throws IOException {
        CentralPersistedState centralPersistedState = new CentralPersistedState(userManager.exportUsers(), workspaceManager.exportWorkspaces());
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(centralPersistedState);
        }
    }

    public void load(UserManager userManager, WorkspaceManager workspaceManager) {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            CentralPersistedState centralPersistedState = (CentralPersistedState) ois.readObject();
            userManager.importUsers(centralPersistedState.users());
            workspaceManager.importWorkspaces(centralPersistedState.workspaces());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
