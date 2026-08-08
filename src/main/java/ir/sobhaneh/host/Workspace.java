package ir.sobhaneh.host;

import ir.sobhaneh.common.Connection;
import ir.sobhaneh.host.models.UserSession;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Workspace {
    @Getter
    private final int port;
    private final ServerSocket serverSocket;
    private final ConcurrentHashMap<Long, String> permanentUsernameByUserId = new ConcurrentHashMap<>();
    @Getter
    private final CentralConnectionListener centralConnectionListener;
    private final ConcurrentHashMap<Long, UserSession> onlineSessionsByUserId = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Long> userIdByUsername = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private ChatStore chatStore = new ChatStore();

    public Workspace(int port, CentralConnectionListener centralConnectionListener) throws IOException {
        this.port = port;
        this.centralConnectionListener = centralConnectionListener;
        this.serverSocket = new ServerSocket(port);
        Thread thread = new Thread(this::acceptLoop);
        thread.start();
    }

    public String findExistingUsername(long userId) {
        return permanentUsernameByUserId.get(userId);
    }
    public void addSession(UserSession session) {
        onlineSessionsByUserId.put(session.userId(), session);
        userIdByUsername.put(session.username(), session.userId());
        permanentUsernameByUserId.put(session.userId(), session.username());
    }

    public UserSession findSessionByUsername(String username) {
        Long userId = userIdByUsername.get(username);
        if (userId == null) {
            return null;
        }
        return onlineSessionsByUserId.get(userId);
    }

    public boolean isOnline(String username) {
        return findSessionByUsername(username) != null;
    }

    public void removeSession(long userId, String username) {
        onlineSessionsByUserId.remove(userId);
        userIdByUsername.remove(username);
    }

    public Map<Long, String> exportUsernames() {
        return new java.util.HashMap<>(permanentUsernameByUserId);
    }

    public void importUsernames(Map<Long, String> usernames) {
        if (usernames != null) {
            permanentUsernameByUserId.putAll(usernames);
        }
    }

    private void acceptLoop() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                Connection clientConnection = new Connection(clientSocket);
                new Thread(new ClientConnection(clientConnection, centralConnectionListener, this)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}