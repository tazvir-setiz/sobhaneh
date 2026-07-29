package ir.sobhaneh.central;

import ir.sobhaneh.common.Connection;
import ir.sobhaneh.common.MessageParser;
import ir.sobhaneh.common.ParsedMessage;
import ir.sobhaneh.common.Protocol;

import java.io.IOException;

public class ClientHandler implements Runnable {
    private final Connection connection;
    private final UserService userService;
    private final HostService hostService;

    public ClientHandler(Connection connection, UserService userService, HostService hostService) {
        this.connection = connection;
        this.userService = userService;
        this.hostService = hostService;
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
                    case Protocol.CREATE_HOST -> {
                        CreateHostResult result = hostService.createHost(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                        switch (result) {
                            case OK -> {
                                HostInfo newHost = new HostInfo(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                                connection.sendLine(Protocol.OK + " " + newHost.getAssignedPort());
                            }
                            case PORT_NUMBER_MUST_BE_AT_LEAST_10000 ->  connection.sendLine(Protocol.ERROR + " Port number must be at least 10000");
                            case AT_MOST_1000_PORTS_IS_ALLOWED ->  connection.sendLine(Protocol.ERROR + " At most 1000 ports is allowed");
                            case PORT_IN_USE_BY_ANOTHER_HOST ->   connection.sendLine(Protocol.ERROR + " Port in use by another host");
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
