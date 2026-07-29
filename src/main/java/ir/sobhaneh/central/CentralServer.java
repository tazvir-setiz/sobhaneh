package ir.sobhaneh.central;

import ir.sobhaneh.central.models.Connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CentralServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8000)){
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
