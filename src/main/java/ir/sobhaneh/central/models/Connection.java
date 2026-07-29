package ir.sobhaneh.central.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection implements AutoCloseable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Object readLock = new Object();
    private final Object writeLock = new Object();

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    public String readLine() throws IOException {
        synchronized (readLock) {
            return in.readLine();
        }
    }

    public void sendLine(String line) throws IOException {
        synchronized (writeLock) {
            out.println(line);
        }
    }
    @Override
    public void close() throws IOException {
        out.close();
        in.close();
        socket.close();
    }
}
