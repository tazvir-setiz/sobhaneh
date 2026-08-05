//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.central;

import ir.sobhaneh.central.models.HostInfo;
import ir.sobhaneh.common.Connection;

import java.io.IOException;
import java.net.Socket;

public class HostRegistrationSession {
    private static final String RESPONSE_OK = "OK";
    private static final String ERROR_NO_PENDING = "ERROR No pending create-host request";
    private static final String ERROR_SEND_FAILED = "ERROR Could not connect to host port for verification";
    private static final String ERROR_INVALID_CODE = "ERROR Invalid code";

    private final HostManager hostManager;
    private final VerificationService verificationService;
    private final TokenManager tokenManager;
    private final WorkspaceManager workspaceManager;

    private HostInfo pendingHost;
    private int pendingPort = -1;

    public HostRegistrationSession(HostManager hostManager, WorkspaceManager workspaceManager, VerificationService verificationService, TokenManager tokenManager) {
        this.hostManager = hostManager;
        this.workspaceManager = workspaceManager;
        this.verificationService = verificationService;
        this.tokenManager = tokenManager;
    }

    public void handleCreateHost(Connection connection, String ip, int startPort, int endPort) throws IOException {
        System.out.println("[HostRegistrationSession] handleCreateHost ip=" + ip + " range=[" + startPort + "," + endPort + "]");
        ReservationResult result = hostManager.reserve(ip, startPort, endPort);

        if (!result.isSuccess()) {
            System.out.println("[HostRegistrationSession] reserve failed: " + result.getErrorMessage());
            connection.sendLine(result.getErrorMessage());
            return;
        }

        pendingHost = result.getHostInfo();
        pendingPort = result.getPort();
        System.out.println("[HostRegistrationSession] reserved port=" + pendingPort);
        connection.sendLine(RESPONSE_OK + " " + pendingPort);
    }

    public boolean handleCheck(Connection connection, Socket ownerSocket) throws IOException {
        if (pendingHost == null) {
            connection.sendLine(ERROR_NO_PENDING);
            return false;
        }

        String expectedCode = sendVerificationCode(connection);
        if (expectedCode == null) {
            return false;
        }

        String receivedCode = connection.readLine();
        if (receivedCode == null) {
            cancel();
            return false;
        }

        return finalizeVerification(connection, expectedCode, receivedCode.trim());
    }

    private String sendVerificationCode(Connection connection) throws IOException {
        String code = verificationService.generateCode();
        boolean sent = verificationService.sendCode(pendingHost.getIp(), pendingPort, code);
        if (!sent) {
            connection.sendLine(ERROR_SEND_FAILED);
            cancel();
            return null;
        }
        return code;
    }

    private boolean finalizeVerification(Connection connection, String expectedCode, String receivedCode) throws IOException {
        if (!expectedCode.equals(receivedCode)) {
            connection.sendLine(ERROR_INVALID_CODE);
            cancel();
            return false;
        }
        System.out.println("[HostRegistrationSession] Host verified and confirmed: " + pendingHost.getIp());
        HostConnectionListener listener = new HostConnectionListener(connection, tokenManager, workspaceManager);
        pendingHost.setConnectionListener(listener);
        hostManager.confirm(pendingHost);
        connection.sendLine(RESPONSE_OK);

        Thread listenerThread = new Thread(listener);
        listenerThread.setDaemon(true);
        listenerThread.start();

        clearPending();
        return true;
    }

    public void cancel() {
        if (pendingHost != null) {
            System.out.println("[HostRegistrationSession] Cancelling pending host reservation: " + pendingHost.getIp());
            hostManager.cancel(pendingHost);
        }
        clearPending();
    }

    private void clearPending() {
        pendingHost = null;
        pendingPort = -1;
    }
}