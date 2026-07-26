//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.common.network;

import java.io.IOException;
import java.net.Socket;
import java.util.Formatter;
import java.util.Scanner;

public class Session implements AutoCloseable{
    private final Socket socket;
    private final Scanner in;
    private final Formatter out;

    public Session(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new Scanner(socket.getInputStream());
        this.out = new Formatter(socket.getOutputStream());
    }

    public boolean hasNextLine() {
        return in.hasNextLine();
    }

    public String readLine() {
        return in.nextLine();
    }

    public void sendLine(String line) {
        out.format("%s%n", line);
        out.flush();
    }
    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}