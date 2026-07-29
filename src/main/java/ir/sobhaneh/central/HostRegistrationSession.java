//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.central;

import ir.sobhaneh.central.models.HostInfo;
import ir.sobhaneh.common.Connection;

import java.io.IOException;
import java.net.Socket;

public class HostRegistrationSession {
    private static final String RESPONSE_OK = "OK";

    private final HostManager hostManager;
    private final VerificationService verificationService;

    private HostInfo pendingHost;
    private int pendingPort = -1;

    public HostRegistrationSession(HostManager hostManager, VerificationService verificationService) {
        this.hostManager = hostManager;
        this.verificationService = verificationService;
    }

    public void handleCreateHost(Connection connection, String ip, int startPort, int endPort) throws IOException {
        ReservationResult result = hostManager.reserve(ip, startPort, endPort);

        if (!result.isSuccess()) {
            connection.sendLine(result.getErrorMessage());
            return;
        }

        pendingHost = result.getHostInfo();
        pendingPort = result.getPort();
        connection.sendLine(RESPONSE_OK + " " + pendingPort);
    }

    public void handleCheck(Connection connection, Socket ownerSocket) throws IOException {
        if (pendingHost == null) {
            connection.sendLine("ERROR No pending create-host request");
            return;
        }

        String code = verificationService.generateCode();
        boolean sent = verificationService.sendCode(pendingHost.getIp(), pendingPort, code);
        if (!sent) {
            connection.sendLine("ERROR Could not connect to host port for verification");
            cancel();
            return;
        }

        String receivedCode = connection.readLine();
        if (receivedCode == null) {
            cancel();
            return;
        }

        if (code.equals(receivedCode.trim())) {
            pendingHost.setSocket(ownerSocket);
            hostManager.confirm(pendingHost);
            connection.sendLine(RESPONSE_OK);
            clearPending();
        } else {
            connection.sendLine("ERROR Invalid code");
            cancel();
        }
    }

    /** در صورت قطع اتصال یا خطا، رزرو معلق را آزاد می‌کند. باید در finally صدا زده شود. */
    public void cancel() {
        if (pendingHost != null) {
            hostManager.cancel(pendingHost);
        }
        clearPending();
    }

    private void clearPending() {
        pendingHost = null;
        pendingPort = -1;
    }
}
