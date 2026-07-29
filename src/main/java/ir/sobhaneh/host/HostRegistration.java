//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.host;

import ir.sobhaneh.common.Connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HostRegistration {
    private static final String COMMAND_CHECK = "check";
    private static final String RESPONSE_OK = "OK";

    private final HostConfig config;

    public HostRegistration(HostConfig config) {
        this.config = config;
    }

    public boolean register(Connection centralConnection) throws IOException {
        int assignedPort = requestPortFromCentral(centralConnection);
        if (assignedPort == -1) {
            return false;
        }

        try (ServerSocket verificationServerSocket = new ServerSocket(assignedPort)) {
            System.out.println("Listening on port " + assignedPort + " for verification...");
            centralConnection.sendLine(COMMAND_CHECK);

            String verificationCode = receiveVerificationCode(verificationServerSocket);
            if (verificationCode == null) {
                return false;
            }

            return confirmVerificationCode(centralConnection, verificationCode);
        }
    }

    private int requestPortFromCentral(Connection centralConnection) throws IOException {
        String command = "create-host " + config.getIp() + " " + config.getStartPort() + " " + config.getEndPort();
        System.out.println("Sending: " + command);
        centralConnection.sendLine(command);

        String response = centralConnection.readLine();
        System.out.println("Received: " + response);

        if (response == null || !response.startsWith(RESPONSE_OK)) {
            return -1;
        }
        return Integer.parseInt(response.split(" ")[1]);
    }

    private String receiveVerificationCode(ServerSocket verificationServerSocket) throws IOException {
        try (Socket verificationSocket = verificationServerSocket.accept();
             Connection verificationConnection = new Connection(verificationSocket)) {
            String code = verificationConnection.readLine();
            System.out.println("Received verification code: " + code);
            return code;
        }
    }

    private boolean confirmVerificationCode(Connection centralConnection, String code) throws IOException {
        centralConnection.sendLine(code);
        String finalResponse = centralConnection.readLine();
        System.out.println("Final response: " + finalResponse);
        return RESPONSE_OK.equals(finalResponse);
    }
}
