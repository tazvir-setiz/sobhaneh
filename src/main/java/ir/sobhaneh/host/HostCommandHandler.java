package ir.sobhaneh.host;

import ir.sobhaneh.common.Connection;

import java.io.IOException;

public class HostCommandHandler {
    private static final String COMMAND_CREATE_WORKSPACE = "create-workspace";
    private final Connection centralConnection;
    private final WorkspaceManager workspaceManager;

    public HostCommandHandler(Connection centralConnection, WorkspaceManager workspaceManager) {
        this.centralConnection = centralConnection;
        this.workspaceManager = workspaceManager;
    }

    public void listen() throws IOException {
        String line;
        while ((line = centralConnection.readLine()) != null) {
            dispatch(line);
        }
        System.out.println("Connection to central server closed.");
    }

    private void dispatch(String line) throws IOException {
        String[] parts = line.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) {
            return;
        }

        switch (parts[0]) {
            case COMMAND_CREATE_WORKSPACE -> dispatchCreateWorkspace(parts);
            default -> centralConnection.sendLine("ERROR Unknown command");
        }
    }

    private void dispatchCreateWorkspace(String[] parts) throws IOException {
        if (parts.length == 0 || parts[0].isEmpty()) {
            return;
        }
        int port;
        try {
            port = Integer.parseInt(parts[1]);
        }catch (NumberFormatException e) {
            centralConnection.sendLine("ERROR Invalid port number");
            return;
        }
        Long userId;
        try {
            userId = Long.parseLong(parts[2]);
        }catch (NumberFormatException e) {
            centralConnection.sendLine("ERROR Invalid user id");
            return;
        }
        centralConnection.sendLine(workspaceManager.handleCreateWorkspace(port, userId));
    }


}