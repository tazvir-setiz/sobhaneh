package ir.sobhaneh.central;

import ir.sobhaneh.common.Connection;
import ir.sobhaneh.common.MessageParser;
import ir.sobhaneh.common.ParsedMessage;
import ir.sobhaneh.common.Protocol;

import java.io.IOException;

public class ClientHandler implements Runnable {
    Connection connection;
    UserService userService;

    public ClientHandler(Connection connection, UserService userService) {
        this.connection = connection;
        this.userService = userService;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String readLine = connection.readLine();
                ParsedMessage parsedReadLine = MessageParser.parse(readLine);

                String[] args = parsedReadLine.getArgs();
                switch (parsedReadLine.getCommand()) {
                    case Protocol.REGISTER -> {
                        RegisterResult result = userService.register(args[0], args[1]);
                        switch (result) {
                            case OK -> connection.sendLine(Protocol.OK);
                            case INVALID_PHONE_FORMAT -> connection.sendLine(Protocol.ERROR + " Invalid phone format");
                            case PHONE_ALREADY_EXISTS -> connection.sendLine(Protocol.ERROR + " Phone already exists");
                            case INVALID_PASSWORD_FORMAT -> connection.sendLine(Protocol.ERROR + " Invalid password format");
                        }
                        return;
                    }
                    case Protocol.LOGIN -> {
                        LoginResult result = userService.login(args[0], args[1]);
                        switch (result) {
                            case OK -> connection.sendLine(Protocol.OK);
                            case USER_NOT_FOUND -> connection.sendLine(Protocol.ERROR + " User not found");
                            case WRONG_PASSWORD -> connection.sendLine(Protocol.ERROR + " Wrong password");
                        }
                    }
                    default -> connection.sendLine(Protocol.ERROR + " Unknown command");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }
}
