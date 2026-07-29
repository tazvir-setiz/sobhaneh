package ir.sobhaneh.central;

import ir.sobhaneh.central.models.Connection;
import ir.sobhaneh.central.models.HostInfo;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

public class ClientHandler implements Runnable {
    private static final String COMMAND_CREATE_HOST = "create-host";
    private static final String COMMAND_CHECK = "check";

    private static final String ERROR_UNKNOWN_COMMAND = "ERROR Unknown command";
    private static final String ERROR_CREATE_HOST_USAGE = "ERROR Usage: create-host <ip> <startPort> <endPort>";
    private static final String ERROR_PORTS_NOT_NUMBERS = "ERROR Ports must be numbers";
    private static final String ERROR_NO_PENDING_REQUEST = "ERROR No pending create-host request";
    private static final String ERROR_VERIFICATION_CONNECTION_FAILED = "ERROR Could not connect to host port for verification";
    private static final String ERROR_CODE_MISMATCH = "ERROR Verification code mismatch";
    private static final String RESPONSE_OK = "OK";

    private static final long VERIFICATION_CODE_MAX_EXCLUSIVE = 10_000_000_000L;
    private static final String VERIFICATION_CODE_FORMAT = "%010d";

    private static final HostManager hostManager = new HostManager();

    private final Socket socket;

    private HostInfo pendingHost;
    private int pendingPort = -1;
    private String pendingVerificationCode;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (Connection connection = new Connection(socket)) {
            String line;
            while ((line = connection.readLine()) != null) {
                System.out.println("Received: " + line);
                handleCommand(connection, line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            cancelPendingIfAny();
        }
    }

    private void handleCommand(Connection connection, String line) throws IOException {
        String[] parts = line.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) {
            return;
        }

        String command = parts[0];
        if (command.equals(COMMAND_CREATE_HOST)) {
            handleCreateHost(connection, parts);
        } else if (command.equals(COMMAND_CHECK)) {
            handleCheck(connection);
        } else {
            connection.sendLine(ERROR_UNKNOWN_COMMAND);
        }
    }


    private void handleCreateHost(Connection connection, String[] parts) throws IOException {
        if (parts.length != 4) {
            connection.sendLine(ERROR_CREATE_HOST_USAGE);
            return;
        }

        String ip = parts[1];
        int startPort;
        int endPort;
        try {
            startPort = Integer.parseInt(parts[2]);
            endPort = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            connection.sendLine(ERROR_PORTS_NOT_NUMBERS);
            return;
        }

        ReservationResult result = hostManager.reserve(ip, startPort, endPort);

        if (!result.isSuccess()) {
            connection.sendLine(result.getErrorMessage());
            return;
        }

        pendingHost = result.getHostInfo();
        pendingPort = result.getPort();
        connection.sendLine(RESPONSE_OK + " " + pendingPort);
    }


    private void handleCheck(Connection connection) throws IOException {
        if (pendingHost == null) {
            connection.sendLine(ERROR_NO_PENDING_REQUEST);
            return;
        }

        pendingVerificationCode = generateVerificationCode();

        if (!sendVerificationCodeToHost()) {
            connection.sendLine(ERROR_VERIFICATION_CONNECTION_FAILED);
            cancelPendingIfAny();
            return;
        }

        String receivedCode = connection.readLine();
        if (receivedCode == null) {
            cancelPendingIfAny();
            return;
        }

        if (pendingVerificationCode.equals(receivedCode.trim())) {
            finalizeRegistration();
            connection.sendLine(RESPONSE_OK);
        } else {
            connection.sendLine(ERROR_CODE_MISMATCH);
            cancelPendingIfAny();
        }
    }

    private String generateVerificationCode() {
        long randomNumber = ThreadLocalRandom.current().nextLong(0, VERIFICATION_CODE_MAX_EXCLUSIVE);
        return String.format(VERIFICATION_CODE_FORMAT, randomNumber);
    }

    private boolean sendVerificationCodeToHost() {
        try (Socket checkSocket = new Socket(pendingHost.getIp(), pendingPort); Connection checkConnection = new Connection(checkSocket)) {
            checkConnection.sendLine(pendingVerificationCode);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void finalizeRegistration() {
        pendingHost.setSocket(socket);
        hostManager.confirm(pendingHost);
        clearPendingState();
    }

    private void cancelPendingIfAny() {
        if (pendingHost != null) {
            hostManager.cancel(pendingHost);
        }
        clearPendingState();
    }

    private void clearPendingState() {
        pendingHost = null;
        pendingPort = -1;
        pendingVerificationCode = null;
    }
}
