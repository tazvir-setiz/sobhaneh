package ir.sobhan.sobhaneh.server;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Server {
    private final ServerSocket centralServer;
    private final Object hostLock = new Object();
    private final List<HostRecord> hosts = new ArrayList<>();
    private final Random random = new Random();

    public Server() throws IOException {
        this.centralServer = new ServerSocket(8000);
    }

    public void start() throws IOException {
        while (!centralServer.isClosed()) {
            handleConnection(centralServer.accept());
        }
    }

    private void handleConnection(Socket socket) {
        try (Socket currentSocket = socket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            String command = reader.readLine();
            if (command == null || command.isBlank()) {
                return;
            }

            if (command.startsWith("create-host ")) {
                handleCreateHost(command, reader, writer);
            } else {
                writer.println("ERROR Unknown command");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleCreateHost(String command, BufferedReader reader, PrintWriter writer) throws IOException {
        String[] parts = command.split("\\s+");
        if (parts.length != 4) {
            writer.println("ERROR Invalid command");
            return;
        }

        String hostAddress = parts[1];
        int startPort;
        int endPort;

        try {
            startPort = Integer.parseInt(parts[2]);
            endPort = Integer.parseInt(parts[3]);
        } catch (NumberFormatException ex) {
            writer.println("ERROR Invalid port number");
            return;
        }

        synchronized (hostLock) {
            if (startPort < 10000) {
                writer.println("ERROR Port number must be at least 10000");
                return;
            }

            if (endPort < startPort) {
                writer.println("ERROR Invalid port range");
                return;
            }

            if (endPort - startPort + 1 > 1000) {
                writer.println("ERROR At most 1000 ports is allowed");
                return;
            }

            try {
                InetAddress.getByName(hostAddress);
            } catch (IOException ex) {
                writer.println("ERROR Invalid host address");
                return;
            }

            for (HostRecord record : hosts) {
                if (record.overlaps(startPort, endPort)) {
                    writer.println("ERROR Port in use by another host");
                    return;
                }
            }

            int port = startPort + random.nextInt(endPort - startPort + 1);
            HostRecord hostRecord = new HostRecord(hostAddress, startPort, endPort, port);
            hosts.add(hostRecord);

            writer.println("OK " + port);
            if (!"check".equalsIgnoreCase(reader.readLine())) {
                hosts.remove(hostRecord);
                writer.println("ERROR Invalid command");
                return;
            }

            if (!verifyHost(hostAddress, port, reader, writer)) {
                hosts.remove(hostRecord);
                return;
            }

            writer.println("OK");
        }
    }

    private boolean verifyHost(String hostAddress, int hostPort, BufferedReader reader, PrintWriter writer) {
        String code = generateCode();
        try (Socket hostSocket = new Socket()) {
            hostSocket.connect(new InetSocketAddress(hostAddress, hostPort), 5000);
            PrintWriter hostWriter = new PrintWriter(hostSocket.getOutputStream(), true);
            hostWriter.println("OK " + code);

            String reportedCode = reader.readLine();
            if (reportedCode == null || !reportedCode.equals(code)) {
                writer.println("ERROR Invalid code");
                return false;
            }

            return true;
        } catch (IOException ex) {
            writer.println("ERROR Invalid code");
            return false;
        }
    }

    private String generateCode() {
        long code = Math.abs(random.nextLong()) % 10_000_000_000L;
        return String.format("%010d", code);
    }

    public static void main(String[] args) throws IOException {
        new Server().start();
    }

    private static final class HostRecord {
        private final String hostAddress;
        private final int startPort;
        private final int endPort;
        private final int port;

        private HostRecord(String hostAddress, int startPort, int endPort, int port) {
            this.hostAddress = hostAddress;
            this.startPort = startPort;
            this.endPort = endPort;
            this.port = port;
        }

        private boolean overlaps(int otherStartPort, int otherEndPort) {
            return startPort <= otherEndPort && otherStartPort <= endPort;
        }
    }
}
