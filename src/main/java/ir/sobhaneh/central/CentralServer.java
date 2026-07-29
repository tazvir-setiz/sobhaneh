//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.central;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CentralServer {
    private static final int PORT = 8000;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Central server listening on port " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
