package ir.sobhan.sobhaneh.host.workspace;

import java.io.*;
import java.net.*;
import java.util.*;

public class Workspace extends Thread {
    private int port;
    private List<PrintWriter> clients = new ArrayList<>();

    public Workspace(int port) { this.port = port; }

    public void run() {
        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                Socket client = server.accept();
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                synchronized (clients) { clients.add(out); }
                new Thread(() -> {
                    try (Scanner in = new Scanner(client.getInputStream())) {
                        while (in.hasNextLine()) {
                            String msg = in.nextLine();
                            synchronized (clients) {
                                for (PrintWriter c : clients) c.println(msg);
                            }
                        }
                    } catch (Exception e) {}
                }).start();
            }
        } catch (Exception e) {}
    }
}