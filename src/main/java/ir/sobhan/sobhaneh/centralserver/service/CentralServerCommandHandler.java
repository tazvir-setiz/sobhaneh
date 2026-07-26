package ir.sobhan.sobhaneh.centralserver.service;

import ir.sobhan.sobhaneh.common.protocol.Command;
import ir.sobhan.sobhaneh.common.protocol.CommandParser;
import ir.sobhan.sobhaneh.common.response.ErrorType;
import ir.sobhan.sobhaneh.common.response.Response;

public class CentralServerCommandHandler {
    CreateHostService createHostService = new CreateHostService();
    ConnectWorkspaceService connectWorkspaceService = new ConnectWorkspaceService();
    CreateWorkspaceService createWorkspaceService = new CreateWorkspaceService();
    LoginService loginService = new LoginService();
    RegisterService registerService = new RegisterService();
    WhoIsService whoIsService = new WhoIsService();

    public CentralServerCommandHandler() {
    }

    public Response handleCommand(String commandLine) {
        Command command = CommandParser.parse(commandLine);
        switch (command.getType()) {
            case REGISTER:
                return registerService.register(command.getArgs()[0], command.getArgs()[1]);
            case LOGIN:
                return loginService.login(command.getArgs()[0], command.getArgs()[1]);
            case CREATE_HOST:
                return createHostService.createHost(command.getArgs()[0], Integer.parseInt(command.getArgs()[1]), Integer.parseInt(command.getArgs()[2]));
            case CREATE_WORKSPACE:
                return createWorkspaceService.createWorkspace(0/*should be change*/, command.getArgs()[0]);
            case CONNECT_WORKSPACE:
                return connectWorkspaceService.connectWorkspace(0/*should be change*/, command.getArgs()[0]);
            default:
                return new Response(ErrorType.UNKNOWN_ERROR);
        }
    }
}
