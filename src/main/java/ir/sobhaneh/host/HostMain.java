//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.host;

import ir.sobhaneh.common.Connection;

import java.io.IOException;
import java.net.Socket;

public class HostMain {
    private static final String CENTRAL_SERVER_IP = "localhost";
    private static final int CENTRAL_SERVER_PORT = 8000;
    private static final WorkspaceManager workspaceManager = new WorkspaceManager();

    public static void main(String[] args) {
        HostConfig config = new HostConfig("127.0.0.1", 10000, 10500);

        try (Socket centralSocket = new Socket(CENTRAL_SERVER_IP, CENTRAL_SERVER_PORT);
             Connection centralConnection = new Connection(centralSocket)) {

            HostRegistration registration = new HostRegistration(config);
            boolean success = registration.register(centralConnection);

            if (success) {
                System.out.println("SUCCESS: Host registered correctly.");
                HostCommandHandler hostCommandHandler = new HostCommandHandler(centralConnection, workspaceManager);
                hostCommandHandler.listen();
            } else {
                System.out.println("FAILED: Host registration failed.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
