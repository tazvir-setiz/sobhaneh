//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientMain {
    private static final String CENTRAL_IP = "localhost";
    private static final int CENTRAL_PORT = 8000;

    private static final String COMMAND_REGISTER = "register";
    private static final String COMMAND_CREATE_WORKSPACE = "create-workspace";
    private static final String COMMAND_CONNECT_WORKSPACE = "connect-workspace";
    private static final String COMMAND_SEND_MESSAGE = "send-message";
    private static final String COMMAND_GET_CHATS = "get-chats";
    private static final String COMMAND_GET_MESSAGES = "get-messages";
    private static final String COMMAND_DISCONNECT = "disconnect";

    private final CentralConnection centralConnection;
    private final WorkspaceConnection workspaceConnection;
    private final BufferedReader console;

    public ClientMain(String phoneNumber, String password) {
        this.centralConnection = new CentralConnection(CENTRAL_IP, CENTRAL_PORT, phoneNumber, password);
        this.workspaceConnection = new WorkspaceConnection();
        this.console = new BufferedReader(new InputStreamReader(System.in));
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: ClientMain <phoneNumber> <password>");
            return;
        }
        ClientMain client = new ClientMain(args[0], args[1]);
        client.run();
    }

    private void run() throws IOException {
        String line;
        while ((line = console.readLine()) != null) {
            handleLine(line);
        }
    }

    private void handleLine(String line) {
        if (line.trim().isEmpty()) {
            return;
        }
        try {
            dispatch(new CommandParser(line));
        } catch (IOException e) {
            System.out.println("ERROR Connection problem: " + e.getMessage());
        }
    }

    private void dispatch(CommandParser parsedCommand) throws IOException {
        switch (parsedCommand.getCommand()) {
            case COMMAND_REGISTER -> handleRegister();
            case COMMAND_CREATE_WORKSPACE -> handleCreateWorkspace(parsedCommand);
            case COMMAND_CONNECT_WORKSPACE -> handleConnectWorkspace(parsedCommand);
            case COMMAND_SEND_MESSAGE -> handleSendMessage(parsedCommand);
            case COMMAND_GET_CHATS -> workspaceConnection.getChats();
            case COMMAND_GET_MESSAGES -> handleGetMessages(parsedCommand);
            case COMMAND_DISCONNECT -> workspaceConnection.disconnect();
            default -> System.out.println("ERROR Unknown command");
        }
    }

    private void handleRegister() throws IOException {
        String response = centralConnection.register();
        System.out.println(response);
    }

    private void handleCreateWorkspace(CommandParser parsedCommand) throws IOException {
        String workspaceName = parsedCommand.getArgs()[0];
        String response = centralConnection.createWorkspace(workspaceName);
        System.out.println(response);
    }

    private void handleConnectWorkspace(CommandParser parsedCommand) throws IOException {
        String workspaceName = parsedCommand.getArgs()[0];
        WorkspaceLocation location = centralConnection.connectWorkspace(workspaceName);
        if (location == null) {
            System.out.println("ERROR Could not connect to workspace");
            return;
        }
        boolean connected = workspaceConnection.connect(location);
        System.out.println(connected ? "OK" : "ERROR Could not authenticate with workspace");
    }

    private void handleSendMessage(CommandParser parsedCommand) throws IOException {
        String toUsername = parsedCommand.getArgs()[0];
        String json = parsedCommand.getJson();
        workspaceConnection.sendMessage(toUsername, json);
    }

    private void handleGetMessages(CommandParser parsedCommand) throws IOException {
        String username = parsedCommand.getArgs()[0];
        workspaceConnection.getMessages(username);
    }
}