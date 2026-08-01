//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.central;

import ir.sobhaneh.common.Connection;

import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private static final HostManager hostManager = new HostManager();
    private static final VerificationService verificationService = new VerificationService();
    private static final UserManager userManager = new UserManager();
    private static final WorkspaceManager workspaceManager = new WorkspaceManager();

    private static final String COMMAND_CREATE_HOST = "create-host";
    private static final String COMMAND_CHECK = "check";
    private static final String COMMAND_REGISTER = "register";
    private static final String COMMAND_LOGIN = "login";
    private static final String COMMAND_CREATE_WORKSPACE = "create-workspace";

    private static final String ERROR_NOT_LOGGED_IN = "ERROR Not logged in";
    private static final String ERROR_UNKNOWN_COMMAND = "ERROR Unknown command";

    private final Socket socket;
    private final HostRegistrationSession session;

    private Long loggedInUserId = null;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.session = new HostRegistrationSession(hostManager, verificationService);
    }

    @Override
    public void run() {
        try (Connection connection = new Connection(socket)) {
            String line;
            while ((line = connection.readLine()) != null) {
                System.out.println("Received: " + line);
                dispatch(connection, line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            session.cancel();
        }
    }

    private void dispatch(Connection connection, String line) throws IOException {
        String[] parts = line.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) {
            return;
        }

        switch (parts[0]) {
            case COMMAND_CREATE_HOST -> dispatchCreateHost(connection, parts);
            case COMMAND_CHECK -> session.handleCheck(connection, socket);
            case COMMAND_REGISTER -> dispatchRegister(connection, parts);
            case COMMAND_LOGIN -> dispatchLogin(connection, parts);
            case COMMAND_CREATE_WORKSPACE -> dispatchCreateWorkspace(connection, parts);
            default -> connection.sendLine(ERROR_UNKNOWN_COMMAND);
        }
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

        session.handleCreateHost(connection, parts[1], startPort, endPort);
    }

    private void dispatchRegister(Connection connection, String[] parts) throws IOException {
        if (parts.length != 3) {
            connection.sendLine("ERROR Usage: register <phoneNumber> <password>");
            return;
        }
        connection.sendLine(registerUser(parts[1], parts[2]));
    }

    private void dispatchLogin(Connection connection, String[] parts) throws IOException {
        if (parts.length != 3) {
            connection.sendLine("ERROR Usage: login <phoneNumber> <password>");
            return;
        }
        connection.sendLine(loginUser(parts[1], parts[2]));
    }

    private void dispatchCreateWorkspace(Connection connection, String[] parts) throws IOException {
        if (parts.length != 2) {
            connection.sendLine("ERROR Usage: create-workspace <workspaceName>");
            return;
        }
        if (loggedInUserId == null) {
            connection.sendLine(ERROR_NOT_LOGGED_IN);
            return;
        }
        connection.sendLine(createWorkspace(parts[1]));
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

    private String createWorkspace(String workspaceName) throws IOException {
        return workspaceManager.createWorkspace(workspaceName, loggedInUserId, hostManager.getRegisteredHosts());
    }

    private Integer parseIntOrSendError(Connection connection, String value, String errorMessage) throws IOException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            connection.sendLine(errorMessage);
            return null;
        }
    }
}
