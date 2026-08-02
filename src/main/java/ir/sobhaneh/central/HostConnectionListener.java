//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.central;

import ir.sobhaneh.central.models.Token;
import ir.sobhaneh.common.Connection;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class HostConnectionListener implements Runnable {
    private static final String COMMAND_WHOIS = "whois";

    private final Connection connection;
    private final TokenManager tokenManager;
    private final Object writeLock = new Object();
    private final BlockingQueue<String> pendingResponses = new LinkedBlockingQueue<>();

    public HostConnectionListener(Connection connection, TokenManager tokenManager) {
        this.connection = connection;
        this.tokenManager = tokenManager;
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = connection.readLine()) != null) {
                handleIncomingLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleIncomingLine(String line) throws IOException {
        String trimmed = line.trim();
        if (trimmed.startsWith(COMMAND_WHOIS + " ")) {
            handleWhois(trimmed);
            return;
        }
        try {
            pendingResponses.put(line);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void handleWhois(String line) throws IOException {
        System.out.println("Got token line: " + line);
        String[] parts = line.split("\\s+");
        if (parts.length != 2) {
            connection.sendLine("ERROR Usage: whois <token>");
            return;
        }
        Token token = tokenManager.resolve(parts[1]);
        System.out.println("Resolved token '" + parts[1] + "' -> " + token);
        if (token == null) {
            connection.sendLine("ERROR Token not found");
            System.out.println("Sent: ERROR Token not found");
            return;
        }
        connection.sendLine("OK " + token.creatorUserId());
        System.out.println("Sent: OK " + token.creatorUserId());
    }

    public String sendAndWait(String command) throws IOException {
        synchronized (writeLock) {
            connection.sendLine(command);
            try {
                return pendingResponses.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for host response", e);
            }
        }
    }
}