//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.centralserver;

import java.io.*;
import java.net.*;
import java.util.*;

public class CentralServer {

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(8000);
        while (true) {
            new ClientHandler(server.accept()).start();
        }
    }
}