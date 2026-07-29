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
        while (true) {
            try (Connection connection = new Connection(socket)) {
                connection.readLine();

            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
