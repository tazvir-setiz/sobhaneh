package ir.sobhan.sobhaneh.client;

import java.io.IOException;
import java.net.Socket;

public class Client {

    Socket clientToServer = new Socket("localhost", 8000);

    public Client() throws IOException {
    }
}
