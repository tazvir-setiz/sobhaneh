package ir.sobhaneh.central;

import com.google.gson.Gson;
import ir.sobhaneh.central.models.Token;
import ir.sobhaneh.client.CommandParser;
import ir.sobhaneh.common.Connection;
import ir.sobhaneh.host.ChatStore;
import ir.sobhaneh.host.models.ChatStoreDto;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class HostConnectionListener implements Runnable {
    private static final String COMMAND_WHOIS = "whois";
    private static final String COMMAND_WORKSPACE_NAME = "workspace-name";
    private static final String RESPONSE_OK = "OK";
    private static final String ERROR_TOKEN_NOT_FOUND = "ERROR Token not found";
    private static final String ERROR_WORKSPACE_NOT_FOUND = "ERROR Workspace not found for this port";
    private static final String COMMAND_PUSH_CHATS = "push-chats";
    private final Connection connection;
    private final TokenManager tokenManager;
    private final WorkspaceManager workspaceManager;
    private final ChatArchiveManager chatArchiveManager;
    private final Object writeLock = new Object();
    private final BlockingQueue<String> pendingResponses = new LinkedBlockingQueue<>();

    public HostConnectionListener(Connection connection, TokenManager tokenManager, WorkspaceManager workspaceManager, ChatArchiveManager chatArchiveManager) {
        this.connection = connection;
        this.tokenManager = tokenManager;
        this.workspaceManager = workspaceManager;
        this.chatArchiveManager = chatArchiveManager;
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = connection.readLine()) != null) {
                handleIncomingLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleIncomingLine(String line) throws IOException {
        String trimmed = line.trim();
        if (trimmed.startsWith(COMMAND_WHOIS + " ")) {
            handleWhois(trimmed);
            return;
        }
        if (trimmed.startsWith(COMMAND_WORKSPACE_NAME + " ")) {
            handleWorkspaceNameQuery(trimmed);
            return;
        }
        if (trimmed.startsWith(COMMAND_PUSH_CHATS + " ")) {
            handlePushChats(trimmed);
            return;
        }
        try {
            pendingResponses.put(line);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void handlePushChats(String line) throws IOException {
        CommandParser commandParser = new CommandParser(line);
        int port;
        try {
            port = Integer.parseInt(commandParser.getArgs()[0]);
        } catch (NumberFormatException e) {
            connection.sendLine("ERROR invalid port");
            return;
        }
        String workspaceName = workspaceManager.findNameByPort(port);

        Gson gson = new Gson();
        ChatStoreDto chatStoreDto = gson.fromJson(commandParser.getJson(), ChatStoreDto.class);
        ChatStore chatStore = ChatStore.fromChatStoreData(chatStoreDto);
        chatArchiveManager.addWorkspace(workspaceName, chatStore);
        connection.sendLine(RESPONSE_OK);
    }

    private void handleWhois(String line) throws IOException {
        String[] parts = line.split("\\s+");
        if (parts.length != 2) {
            connection.sendLine("ERROR Usage: whois <token>");
            return;
        }
        Token token = tokenManager.resolve(parts[1]);
        if (token == null) {
            connection.sendLine(ERROR_TOKEN_NOT_FOUND);
            return;
        }
        connection.sendLine(RESPONSE_OK + " " + token.creatorUserId());
    }

    private void handleWorkspaceNameQuery(String line) throws IOException {
        String[] parts = line.split("\\s+");
        if (parts.length != 2) {
            connection.sendLine("ERROR Usage: workspace-name <port>");
            return;
        }
        int port;
        try {
            port = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            connection.sendLine("ERROR Invalid port number");
            return;
        }
        String name = workspaceManager.findNameByPort(port);
        if (name == null) {
            connection.sendLine(ERROR_WORKSPACE_NOT_FOUND);
            return;
        }
        connection.sendLine(RESPONSE_OK + " " + name);
    }

    public String sendAndWait(String command) throws IOException {
        synchronized (writeLock) {
            connection.sendLine(command);
            try {
                return pendingResponses.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for host response", e);
            }
        }
    }
}