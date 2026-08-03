package ir.sobhaneh.client;

import java.io.IOException;

public class ClientMain {
    public static void main(String[] args) throws IOException {
        CentralConnection conn = new CentralConnection("localhost", 8000, "09123456789", "Aa@12345");
        System.out.println(conn.register());
        System.out.println(conn.createWorkspace("test1"));
    }
}
