//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.centralserver;

import ir.sobhan.sobhaneh.centralserver.service.CentralServerCommandHandler;
import ir.sobhan.sobhaneh.common.response.Response;

import java.io.IOException;
import java.net.Socket;
import java.util.Formatter;
import java.util.Scanner;

public class ClientHandler extends Thread{
    private final Socket socket;
    public  ClientHandler(Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        CentralServerCommandHandler centralServerCommandHandler = new CentralServerCommandHandler();
        try (Scanner in = new Scanner(socket.getInputStream());
             Formatter out = new Formatter(socket.getOutputStream())) {
            while (in.hasNextLine()) {
                String line = in.nextLine();
                Response response = centralServerCommandHandler.handleCommand(line);
                out.format("%s%n", response);
                out.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
