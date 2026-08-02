package ir.sobhaneh.host;

import ir.sobhaneh.common.Connection;
import ir.sobhaneh.host.models.UserSession;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Workspace {
    private final int port;
    private final ServerSocket serverSocket;
    private final Connection centralConnection;
    private final ConcurrentHashMap<Long, UserSession> onlineSessionsByUserId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> userIdByUsername = new ConcurrentHashMap<>();

    public Workspace(int port, Connection centralConnection) throws IOException {
        this.port = port;
        this.centralConnection = centralConnection;
        this.serverSocket = new ServerSocket(port);
        Thread thread = new Thread(this::acceptLoop);
        thread.start();
    }

    public Connection getCentralConnection() {
        return centralConnection;
    }

    public String findExistingUsername(long userId) {
        UserSession session = onlineSessionsByUserId.get(userId);
        return session == null ? null : session.username();
    }

    public void addSession(UserSession session) {
        onlineSessionsByUserId.put(session.userId(), session);
        userIdByUsername.put(session.username(), session.userId());
    }

    private void acceptLoop() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                Connection clientConnection = new Connection(clientSocket);
                new Thread(new ClientConnection(clientConnection, centralConnection, this)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public record WorkspaceDate4Token(String hostIp, int workSpacePort){}
}