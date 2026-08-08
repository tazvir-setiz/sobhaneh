//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.host;

import ir.sobhaneh.client.CommandParser;
import ir.sobhaneh.common.Connection;
import ir.sobhaneh.host.models.IncomingMessagePayload;
import ir.sobhaneh.host.models.Message;
import ir.sobhaneh.host.models.UserSession;

import java.io.IOException;

public class ClientConnection implements Runnable {
    private final Connection connection;
    private final CentralConnectionListener centralConnectionListener;
    private final Workspace workspace;
    private final JsonMapper jsonMapper = new JsonMapper();

    public ClientConnection(Connection connection, CentralConnectionListener centralConnectionListener, Workspace workspace) {
        this.connection = connection;
        this.centralConnectionListener = centralConnectionListener;
        this.workspace = workspace;
    }

    @Override
    public void run() {
        try {
            long userId = authenticate();
            if (userId == -1) {
                return;
            }
            String username = resolveUsername(userId);
            if (username == null) {
                return;
            }
            UserSession session = new UserSession(connection, userId, username);
            workspace.addSession(session);
            System.out.println("[ClientConnection] Session established: userId=" + userId + " username=" + username);
            try {
                handleCommands(userId, username);
            } finally {
                workspace.removeSession(userId, username);
            }
        } catch (IOException e) {
            System.out.println("[ClientConnection] IOException: " + e.getMessage());
        }
    }

    private void handleCommands(long userId, String username) throws IOException {
        String line;
        while ((line = connection.readLine()) != null) {
            boolean shouldStop = dispatchCommand(line, username);
            if (shouldStop) {
                return;
            }
        }
    }

    private boolean dispatchCommand(String line, String username) throws IOException {
        CommandParser parsedCommand = new CommandParser(line);
        switch (parsedCommand.getCommand()) {
            case "send-message" -> handleSendMessage(parsedCommand, username);
            case "get-chats" -> handleGetChats(username);
            case "get-messages" -> handleGetMessages(parsedCommand, username);
            case "disconnect" -> {
                return true;
            }
            default -> connection.sendLine("ERROR Unknown command");
        }
        return false;
    }

    private void handleSendMessage(CommandParser parsedCommand, String fromUsername) throws IOException {
        String toUsername = parsedCommand.getArgs()[0];
        String json = parsedCommand.getJson();


        IncomingMessagePayload payload = jsonMapper.parseIncomingMessage(json);

        UserSession recipientSession = workspace.findSessionByUsername(toUsername);
        boolean recipientOnline = recipientSession != null;
        ChatStore chatStore = workspace.getChatStore();
        int seq = chatStore.addMessage(fromUsername, toUsername, payload.type(), payload.body(), recipientOnline);
        connection.sendLine("OK " + seq);

        if (recipientOnline) {
            Message message = new Message(seq, fromUsername, payload.type(), payload.body());
            String messageJson = jsonMapper.messageToJson(message);
            recipientSession.connection().sendLine("receive-message " + fromUsername + " " + messageJson);
        }
    }

    private void handleGetChats(String username) throws IOException {
        ChatStore chatStore = workspace.getChatStore();
        String json = chatStore.buildChatsJson(username);
        connection.sendLine("OK " + json);
    }

    private void handleGetMessages(CommandParser parsedCommand, String username) throws IOException {
        ChatStore chatStore = workspace.getChatStore();
        String otherParty = parsedCommand.getArgs()[0];
        String json = chatStore.buildMessagesJson(username, otherParty);
        connection.sendLine("OK " + json);
    }

    private long authenticate() throws IOException {
        String line = connection.readLine();
        if (line == null || !line.startsWith("connect ")) {
            connection.sendLine("ERROR Invalid connect command");
            return -1;
        }

        String[] parts = line.trim().split("\\s+");
        if (parts.length != 2) {
            connection.sendLine("ERROR Usage: connect <token>");
            return -1;
        }
        String token = parts[1];
        System.out.println("[ClientConnection] authenticate with token=" + token);

        String response = centralConnectionListener.sendAndWait("whois " + token);

        if (response == null || !response.startsWith("OK ")) {
            System.out.println("[ClientConnection] whois failed for token=" + token + " response=" + response);
            connection.sendLine("ERROR Invalid or expired token");
            return -1;
        }

        try {
            long userId = Long.parseLong(response.split("\\s+")[1]);
            System.out.println("[ClientConnection] authenticated userId=" + userId);
            return userId;
        } catch (NumberFormatException e) {
            connection.sendLine("ERROR Invalid user id from central");
            return -1;
        }
    }

    private String resolveUsername(long userId) throws IOException {
        String existingUsername = workspace.findExistingUsername(userId);
        System.out.println("[DEBUG] resolveUsername userId=" + userId + " existingUsername=" + existingUsername);
        if (existingUsername != null) {
            connection.sendLine("OK");
            return existingUsername;
        }
        connection.sendLine("username?");
        String newUsername = connection.readLine();
        if (newUsername == null) {
            connection.sendLine("ERROR Invalid username");
            return null;
        }
        connection.sendLine("OK");
        return newUsername;
    }
}