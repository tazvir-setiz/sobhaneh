package ir.sobhaneh.host;

import ir.sobhaneh.common.Connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Workspace {
    private final int port;
    private final ServerSocket serverSocket;

    public Workspace(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        Thread thread = new Thread(this::acceptLoop);
        thread.start();
    }

    private void acceptLoop() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected on workspace port " + port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
