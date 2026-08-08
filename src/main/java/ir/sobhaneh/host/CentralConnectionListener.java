//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.host;

import ir.sobhaneh.common.Connection;
import lombok.Getter;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class CentralConnectionListener implements Runnable {
    private static final String COMMAND_CREATE_WORKSPACE = "create-workspace";
    private static final String COMMAND_GET_CHAT_STORE = "get-chat-store";
    private static final String COMMAND_RESTORE_CHATS = "restore-chats";
    @Getter
    private final Connection centralConnection;
    private final HostSideWorkspaceManager hostSideWorkspaceManager;
    private final Object writeLock = new Object();
    private final BlockingQueue<String> pendingResponses = new LinkedBlockingQueue<>();

    public CentralConnectionListener(Connection centralConnection, HostSideWorkspaceManager hostSideWorkspaceManager) {
        this.centralConnection = centralConnection;
        this.hostSideWorkspaceManager = hostSideWorkspaceManager;
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = centralConnection.readLine()) != null) {
                handleIncomingLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Connection to central server closed.");
    }

    private void handleIncomingLine(String line) throws IOException {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        if (trimmed.startsWith(COMMAND_CREATE_WORKSPACE + " ")) {
            dispatchCreateWorkspace(trimmed);
            return;
        }
        if (trimmed.startsWith(COMMAND_GET_CHAT_STORE + " ")) {
            dispatchGetChatStore(trimmed);
            return;
        }
        if (trimmed.startsWith(COMMAND_RESTORE_CHATS + " ")) {
            dispatchRestoreChats(trimmed);
            return;
        }
        try {
            pendingResponses.put(line);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void dispatchRestoreChats(String line) throws IOException {
        String[] parts = line.split("\\s+", 3);
        if (parts.length != 3) {
            centralConnection.sendLine("ERROR Usage: restore-chats <port> <json>");
            return;
        }
        int port;
        try {
            port = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            centralConnection.sendLine("ERROR Invalid port number");
            return;
        }
        String json = parts[2];
        boolean success = hostSideWorkspaceManager.restoreChatStore(port, json);
        centralConnection.sendLine(success ? "OK" : "ERROR Workspace not found for this port");
    }

    private void dispatchCreateWorkspace(String line) throws IOException {
        System.out.println("[CentralConnectionListener] Received create-workspace: " + line);
        String[] parts = line.split("\\s+");
        int port;
        try {
            port = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            centralConnection.sendLine("ERROR Invalid port number");
            return;
        }
        long userId;
        try {
            userId = Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            centralConnection.sendLine("ERROR Invalid user id");
            return;
        }
        String result = hostSideWorkspaceManager.handleCreateWorkspace(port, userId);
        System.out.println("[CentralConnectionListener] create-workspace port=" + port + " userId=" + userId + " -> " + result);
        centralConnection.sendLine(result);
    }

    private void dispatchGetChatStore(String line) throws IOException {
        String[] parts = line.split("\\s+");
        int port;
        try {
            port = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            centralConnection.sendLine("ERROR Invalid port number");
            return;
        }
        String json = hostSideWorkspaceManager.getChatStoreJson(port);
        if (json == null) {
            centralConnection.sendLine("ERROR Workspace not found for this port");
            return;
        }
        centralConnection.sendLine("OK " + json);
    }

    public String sendAndWait(String command) throws IOException {
        synchronized (writeLock) {
            System.out.println("[CentralConnectionListener] Sending to central: " + command);
            centralConnection.sendLine(command);
            try {
                String response = pendingResponses.take();
                System.out.println("[CentralConnectionListener] Response from central: " + response);
                return response;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for central response", e);
            }
        }
    }


}