package ir.sobhaneh.client;

import ir.sobhaneh.common.Connection;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.Socket;

@RequiredArgsConstructor
public class CentralConnection {
    private static final String REGISTER = "register";
    private static final String LOGIN = "login";
    private static final String CREATE_WORKSPACE = "create-workspace";
    private static final String CONNECT_WORKSPACE = "connect-workspace";
    private static final String OK = "OK";

    private final String ip;
    private final int port;
    private final String phoneNumber;
    private final String password;

    private String sendAndReceive(Connection connection, String sendLine) throws IOException {
        connection.sendLine(sendLine);
        return connection.readLine();
    }

    private LoginConnectionResult openConnectionAndLogin() throws IOException {
        Connection connection = new Connection(new Socket(ip, port));
        String loginResult = sendAndReceive(connection, LOGIN + " " + phoneNumber + " " + password);
        if (loginResult.equals(OK)) {
            return new LoginConnectionResult(connection, null);
        } else {
            connection.close();
            return new LoginConnectionResult(null, loginResult);
        }
    }

    public String register() throws IOException {
        try (Connection connection = new Connection(new Socket(ip, port))) {
            return sendAndReceive(connection, REGISTER + " " + phoneNumber + " " + password);
        }
    }

    public String createWorkspace(String workspaceName) throws IOException {
        LoginConnectionResult result = openConnectionAndLogin();
        if (result.errorMessage() != null) {
            return result.errorMessage();
        }
        try (Connection connection = result.connection()) {
            return sendAndReceive(connection, CREATE_WORKSPACE + " " + workspaceName);
        }
    }
}