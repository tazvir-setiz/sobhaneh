//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.central;

import ir.sobhaneh.central.models.HostInfo;
import ir.sobhaneh.common.Connection;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientHandler implements Runnable {
    private static final HostManager hostManager = new HostManager();
    private static final VerificationService verificationService = new VerificationService();
    private static final UserManager userManager = new UserManager();
    private static final WorkspaceManager workspaceManager = new WorkspaceManager();
    private static final String COMMAND_CREATE_HOST = "create-host";
    private static final String COMMAND_CHECK = "check";
    private static final String COMMAND_REGISTER = "register";
    private static final String COMMAND_LOGIN = "login";
    private static final String COMMAND_DISCONNECT = "disconnect";
    private static final String COMMAND_CREATE_WORKSPACE = "create-workspace";

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
            case COMMAND_CREATE_WORKSPACE ->  dispatchCreateWorkspace(connection, parts);
            default -> connection.sendLine("ERROR Unknown command");
        }
    }

    private void dispatchCreateHost(Connection connection, String[] parts) throws IOException {
        if (parts.length != 4) {
            connection.sendLine("ERROR Usage: create-host <ip> <startPort> <endPort>");
            return;
        }
        try {
            String ip = parts[1];
            int startPort = Integer.parseInt(parts[2]);
            int endPort = Integer.parseInt(parts[3]);
            session.handleCreateHost(connection, ip, startPort, endPort);
        } catch (NumberFormatException e) {
            connection.sendLine("ERROR Ports must be numbers");
        }
    }

    private void dispatchRegister(Connection connection, String[] parts) throws IOException {
        if (parts.length != 3) {
            connection.sendLine("ERROR Usage: register <phoneNumber> <password>");
            return;
        }
        String phoneNumber = parts[1];
        String password = parts[2];
        String result = userManager.register(phoneNumber, password);
        connection.sendLine(result);
    }

    private void dispatchLogin(Connection connection, String[] parts) throws IOException {
        if (parts.length != 3) {
            connection.sendLine("ERROR Usage: login <phoneNumber> <password>");
            return;
        }
        String phoneNumber = parts[1];
        String password = parts[2];
        String result = userManager.login(phoneNumber, password);
        if ("OK".equals(result)) {
            loggedInUserId = userManager.findByPhone(phoneNumber).getId();
        }
        connection.sendLine(result);
    }

    private void dispatchCreateWorkspace(Connection connection, String[] parts) throws IOException {
        if (loggedInUserId == null) {
            connection.sendLine("ERROR Not logged in");
            return;
        }
        if (parts.length != 2) {
            connection.sendLine("ERROR Usage: create-workspace <workspaceName>");
            return;
        }
        String workspaceName = parts[1];
        String result = workspaceManager.createWorkspace(workspaceName, loggedInUserId, hostManager.getRegisteredHosts());
        connection.sendLine(result);
    }
}
