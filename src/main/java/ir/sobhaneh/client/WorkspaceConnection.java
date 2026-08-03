package ir.sobhaneh.client;

import ir.sobhaneh.common.Connection;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class WorkspaceConnection {
    private static final String CONNECT = "connect";
    private static final String USERNAMEq = "username?";
    private static final String OK = "OK";
    private static final String SEND_MESSAGE = "send-message";
    private static final String GET_CHATS = "get-chats";
    private static final String GET_MESSAGES = "get-messages";
    private static final String DISCONNECT = "disconnect";

    private Connection connection;

    private void openConnection(String ip, int port) throws IOException {
        connection = new Connection(new Socket(ip, port));
    }

    public boolean connect(WorkspaceLocation workspaceLocation) throws IOException {
        openConnection(workspaceLocation.ip(), workspaceLocation.port());
        boolean authenticateResult = authenticate(workspaceLocation.token());
        if (authenticateResult) startReaderThread();
        return authenticateResult;
    }

    private boolean authenticate(String token) {
        try {
            connection.sendLine(CONNECT + " " + token);
            String response = connection.readLine();
            if (USERNAMEq.equals(response)) {
                Scanner scanner = new Scanner(System.in);
                System.out.println(USERNAMEq);
                String username = scanner.next();
                connection.sendLine(username);
                response = connection.readLine();
            }
            return OK.equals(response);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void startReaderThread() {
        Thread readerThread = new Thread(this::readLoop);
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void readLoop() {
        try {
            String line;
            while ((line = connection.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Connection to workspace closed.");
        }
    }

    public void sendMessage(String toUsername, String json) throws IOException {
        connection.sendLine(SEND_MESSAGE + " " + toUsername + " " + json);
    }

    public void getChats() throws IOException {
        connection.sendLine(GET_CHATS);
    }

    public void getMessages(String username) throws IOException {
        connection.sendLine(GET_MESSAGES + " " + username);
    }

    public void disconnect() throws IOException {
        connection.sendLine(DISCONNECT);
    }
}
