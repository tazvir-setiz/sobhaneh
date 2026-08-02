package ir.sobhaneh.central;

import ir.sobhaneh.central.models.Token;
import ir.sobhaneh.central.models.WorkspaceInfo;
import ir.sobhaneh.common.Connection;

import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private static final HostManager hostManager = new HostManager();
    private static final VerificationService verificationService = new VerificationService();
    private static final UserManager userManager = new UserManager();
    private static final WorkspaceManager workspaceManager = new WorkspaceManager();
    private static final TokenManager tokenManager = new TokenManager();

    private static final String COMMAND_CREATE_HOST = "create-host";
    private static final String COMMAND_CHECK = "check";
    private static final String COMMAND_REGISTER = "register";
    private static final String COMMAND_LOGIN = "login";
    private static final String COMMAND_CREATE_WORKSPACE = "create-workspace";
    private static final String COMMAND_CONNECT_WORKSPACE = "connect-workspace";

    private static final String ERROR_NOT_LOGGED_IN = "ERROR Not logged in";
    private static final String ERROR_UNKNOWN_COMMAND = "ERROR Unknown command";

    private final Socket socket;
    private final HostRegistrationSession session;

    private Long loggedInUserId = null;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.session = new HostRegistrationSession(hostManager, verificationService, tokenManager);
        System.out.println("[ClientHandler] New connection from " + socket.getRemoteSocketAddress());
    }

    @Override
    public void run() {
        Connection connection = null;
        try {
            connection = new Connection(socket);
            String line;
            while ((line = connection.readLine()) != null) {
                System.out.println("[ClientHandler] Received from " + socket.getRemoteSocketAddress() + ": " + line);
                boolean stopReading = dispatch(connection, line);
                if (stopReading) {
                    System.out.println("[ClientHandler] Closing connection (stopReading) for " + socket.getRemoteSocketAddress());
                    return;
                }
            }
            System.out.println("[ClientHandler] Connection closed by peer: " + socket.getRemoteSocketAddress());
            connection.close();
        } catch (IOException e) {
            System.out.println("[ClientHandler] IOException for " + socket.getRemoteSocketAddress() + ": " + e.getMessage());
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException ignored) {
                }
            }
        } finally {
            session.cancel();
        }
    }

    private boolean dispatch(Connection connection, String line) throws IOException {
        String[] parts = line.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) {
            return false;
        }

        switch (parts[0]) {
            case COMMAND_CREATE_HOST -> dispatchCreateHost(connection, parts);
            case COMMAND_CHECK -> {
                return session.handleCheck(connection, socket);
            }
            case COMMAND_REGISTER -> dispatchRegister(connection, parts);
            case COMMAND_LOGIN -> dispatchLogin(connection, parts);
            case COMMAND_CREATE_WORKSPACE -> dispatchCreateWorkspace(connection, parts);
            case COMMAND_CONNECT_WORKSPACE -> dispatchConnectWorkspace(connection, parts);
            default -> {
                System.out.println("[ClientHandler] Unknown command: " + parts[0]);
                connection.sendLine(ERROR_UNKNOWN_COMMAND);
            }
        }
        return false;
    }

    private void dispatchCreateHost(Connection connection, String[] parts) throws IOException {
        if (parts.length != 4) {
            connection.sendLine("ERROR Usage: create-host <ip> <startPort> <endPort>");
            return;
        }
        Integer startPort = parseIntOrSendError(connection, parts[2], "ERROR Ports must be numbers");
        if (startPort == null) return;
        Integer endPort = parseIntOrSendError(connection, parts[3], "ERROR Ports must be numbers");
        if (endPort == null) return;

        System.out.println("[ClientHandler] create-host ip=" + parts[1] + " range=[" + startPort + "," + endPort + "]");
        session.handleCreateHost(connection, parts[1], startPort, endPort);
    }

    private void dispatchRegister(Connection connection, String[] parts) throws IOException {
        if (parts.length != 3) {
            connection.sendLine("ERROR Usage: register <phoneNumber> <password>");
            return;
        }
        String result = registerUser(parts[1], parts[2]);
        System.out.println("[ClientHandler] register phone=" + parts[1] + " -> " + result);
        connection.sendLine(result);
    }

    private void dispatchLogin(Connection connection, String[] parts) throws IOException {
        if (parts.length != 3) {
            connection.sendLine("ERROR Usage: login <phoneNumber> <password>");
            return;
        }
        String result = loginUser(parts[1], parts[2]);
        System.out.println("[ClientHandler] login phone=" + parts[1] + " -> " + result
                + (loggedInUserId != null ? " (userId=" + loggedInUserId + ")" : ""));
        connection.sendLine(result);
    }

    private void dispatchCreateWorkspace(Connection connection, String[] parts) throws IOException {
        if (parts.length != 2) {
            connection.sendLine("ERROR Usage: create-workspace <workspaceName>");
            return;
        }
        if (loggedInUserId == null) {
            System.out.println("[ClientHandler] create-workspace rejected: not logged in");
            connection.sendLine(ERROR_NOT_LOGGED_IN);
            return;
        }
        System.out.println("[ClientHandler] create-workspace name=" + parts[1] + " userId=" + loggedInUserId);
        String createWorkspaceResult = workspaceManager.createWorkspace(parts[1], loggedInUserId, hostManager.getRegisteredHosts());
        System.out.println("[ClientHandler] create-workspace result: " + createWorkspaceResult);
        connection.sendLine(createWorkspaceResult);
    }

    private void dispatchConnectWorkspace(Connection connection, String[] parts) throws IOException {
        if (parts.length != 2) {
            connection.sendLine("ERROR Usage: connect-workspace <workspaceName>");
            return;
        }
        if (loggedInUserId == null) {
            System.out.println("[ClientHandler] connect-workspace rejected: not logged in");
            connection.sendLine("ERROR Not logged in");
            return;
        }
        String workspaceName = parts[1];
        WorkspaceInfo workspace = workspaceManager.findByName(workspaceName);
        if (workspace == null) {
            System.out.println("[ClientHandler] connect-workspace: workspace not found: " + workspaceName);
            connection.sendLine("ERROR workspace not found");
            return;
        }
        Token newToken = tokenManager.createToken(loggedInUserId, workspaceName);
        System.out.println("[ClientHandler] connect-workspace name=" + workspaceName
                + " userId=" + loggedInUserId + " -> token=" + newToken.token()
                + " host=" + workspace.hostIp() + ":" + workspace.port());
        connection.sendLine("OK " + workspace.hostIp() + " " + workspace.port() + " " + newToken.token());
    }

    private String registerUser(String phoneNumber, String password) {
        return userManager.register(phoneNumber, password);
    }

    private String loginUser(String phoneNumber, String password) {
        String result = userManager.login(phoneNumber, password);
        if ("OK".equals(result)) {
            loggedInUserId = userManager.findByPhone(phoneNumber).getId();
        }
        return result;
    }

    private Integer parseIntOrSendError(Connection connection, String value, String errorMessage) throws IOException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.out.println("[ClientHandler] Failed to parse int '" + value + "'");
            connection.sendLine(errorMessage);
            return null;
        }
    }
}