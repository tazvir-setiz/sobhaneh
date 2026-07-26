//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.centralserver;

import ir.sobhan.sobhaneh.centralserver.service.CentralServerCommandHandler;
import ir.sobhan.sobhaneh.common.network.Session;
import ir.sobhan.sobhaneh.common.response.Response;

import java.io.IOException;
import java.net.Socket;

public class ClientHandler extends Thread{
    private final Socket socket;
    public  ClientHandler(Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        CentralServerCommandHandler centralServerCommandHandler = new CentralServerCommandHandler();
        try (Session session = new Session(socket)) {
            while (session.hasNextLine()) {
                String line = session.readLine();
                Response response = centralServerCommandHandler.handleCommand(line);
                session.sendLine(response.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
