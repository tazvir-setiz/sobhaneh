package ir.sobhaneh.host;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection implements AutoCloseable {
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public String readLine() throws IOException {
        return in.readLine();
    }

    public void sendLine(String line) throws IOException {
        out.println(line);
    }

    @Override
    public void close() throws IOException {
        out.close();
        in.close();
        socket.close();
    }
}
