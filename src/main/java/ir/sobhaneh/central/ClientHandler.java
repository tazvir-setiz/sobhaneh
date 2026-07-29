package ir.sobhaneh.central;

import ir.sobhaneh.central.models.Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    public final Socket socket;
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        try (Connection connection = new Connection(socket)) {
            String line;
            while ((line = connection.readLine()) != null) {
                System.out.println("Received: " + line);
                handleCommand(connection, line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void handleCommand(Connection connection, String line) throws IOException {
    }
}
