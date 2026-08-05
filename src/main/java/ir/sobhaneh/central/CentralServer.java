package ir.sobhaneh.central;

import ir.sobhaneh.central.persistence.DataStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class CentralServer {
    private static final int PORT = 8000;
    private static final String COMMAND_SHUTDOWN = "shutdown";

    public static void main(String[] args) throws IOException {
        UserManager userManager = new UserManager();
        HostManager hostManager = new HostManager();
        WorkspaceManager workspaceManager = new WorkspaceManager();
        TokenManager tokenManager = new TokenManager();
        ChatArchiveManager chatArchiveManager = new ChatArchiveManager();

        DataStore dataStore = new DataStore();
        dataStore.load(userManager, workspaceManager, chatArchiveManager);

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Central server listening on port " + PORT);

            startShutdownListener(dataStore, userManager, workspaceManager, chatArchiveManager, serverSocket);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket, userManager, hostManager, workspaceManager, tokenManager, chatArchiveManager)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startShutdownListener(DataStore dataStore, UserManager userManager,
                                              WorkspaceManager workspaceManager, ChatArchiveManager chatArchiveManager, ServerSocket serverSocket) {
        Thread shutdownThread = new Thread(() -> {
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            String line;
            try {
                while ((line = console.readLine()) != null) {
                    if (COMMAND_SHUTDOWN.equals(line.trim())) {
                        dataStore.save(userManager, workspaceManager, chatArchiveManager);
                        System.out.println("Saved. Shutting down.");
                        serverSocket.close();
                        System.exit(0);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        shutdownThread.setDaemon(true);
        shutdownThread.start();
    }
}