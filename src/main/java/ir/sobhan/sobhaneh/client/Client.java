package ir.sobhan.sobhaneh.client;

import java.io.IOException;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: Client <phone_number> <password>");
        }

        try (Socket ignored = new Socket("localhost", 8000)) {
        }
    }
}
