package ir.sobhaneh.common;

import java.io.*;
import java.net.Socket;

public class Connection implements AutoCloseable {

    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void sendLine(String line) throws IOException {
        writer.write(line);
        writer.write("\n");
        writer.flush();
    }

    public String readLine() throws IOException {
        return reader.readLine();
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writer.close();
        } catch (IOException e) {
        }
        try {
            socket.close();
        } catch (IOException e) {
        }
    }

    public Socket getSocket() {
        return socket;
    }
}
