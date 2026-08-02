//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.host;

import ir.sobhaneh.common.Connection;
import ir.sobhaneh.host.models.UserSession;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ClientConnection implements Runnable {
    private static final ConcurrentHashMap<Long, UserSession> userSessions = new ConcurrentHashMap<>();
    private final Connection connection;
    private final Connection centralConnection;
    private final Workspace workspace;
    public ClientConnection(Connection connection, Connection centralConnection, Workspace workspace) {
        this.connection = connection;
        this.centralConnection = centralConnection;
        this.workspace = workspace;
    }
    @Override
    public void run() {
        try {
            long userId = authenticate();
            if (userId == -1) {
                return;
            }
            String username = resolveUsername(userId);
            if (username == null) {
                return;
            }
            UserSession session = new UserSession(connection, userId, username);
            workspace.addSession(session);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private long authenticate() throws IOException {
        String line = connection.readLine();
        if (line == null || !line.startsWith("connect ")) {
            connection.sendLine("ERROR Invalid connect command");
            return -1;
        }

        String[] parts = line.trim().split("\\s+");
        if (parts.length != 2) {
            connection.sendLine("ERROR Usage: connect <token>");
            return -1;
        }
        String token = parts[1];

        String response;
        synchronized (centralConnection) {
            centralConnection.sendLine("whois " + token);
            response = centralConnection.readLine();
        }

        if (response == null || !response.startsWith("OK ")) {
            connection.sendLine("ERROR Invalid or expired token");
            return -1;
        }

        try {
            return Long.parseLong(response.split("\\s+")[1]);
        } catch (NumberFormatException e) {
            connection.sendLine("ERROR Invalid user id from central");
            return -1;
        }
    }

    private String resolveUsername(long userId) throws IOException {
        String existingUsername = workspace.findExistingUsername(userId);
        if (existingUsername != null) {
            return existingUsername;
        }
        connection.sendLine("username?");
        String newUsername = connection.readLine();
        if (newUsername == null) {
            connection.sendLine("ERROR Invalid username");
            return null;
        }
        connection.sendLine("OK");
        return newUsername;
    }


}
