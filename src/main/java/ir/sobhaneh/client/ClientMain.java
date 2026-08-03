package ir.sobhaneh.client;

import java.io.IOException;

public class ClientMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        CentralConnection central = new CentralConnection("localhost", 8000, "09123456789", "Aa@12345");
        WorkspaceLocation loc = central.connectWorkspace("test1");
        WorkspaceConnection workspace = new WorkspaceConnection();
        boolean connected = workspace.connect(loc);
        System.out.println("Connected: " + connected);

        workspace.sendMessage("saeed", "{\"type\":\"text\",\"body\":\"salam\"}");
        Thread.sleep(5000); // یکم صبر کن تا reader thread جواب رو چاپ کنه
    }
}
