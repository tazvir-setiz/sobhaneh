//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.central;

import ir.sobhaneh.common.Connection;

import java.io.IOException;
import java.net.ServerSocket;

public class CentralServer {
    private ServerSocket serverSocket;
    private final int portNumber;
    private final UserService userService = new UserService();
    private final HostService hostService = new HostService();
    public CentralServer() {
        this.portNumber = 8000;
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void start() {
        while (true) {
            try {
                Connection newConnection = new Connection(serverSocket.accept());
                new Thread(new ClientHandler(newConnection, userService, hostService)).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }



}
