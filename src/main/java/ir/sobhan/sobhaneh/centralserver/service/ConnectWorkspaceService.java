package ir.sobhan.sobhaneh.centralserver.service;

import ir.sobhan.sobhaneh.centralserver.repository.TokenRepository;
import ir.sobhan.sobhaneh.centralserver.repository.UserRepository;
import ir.sobhan.sobhaneh.centralserver.repository.WorkspaceRepository;
import ir.sobhan.sobhaneh.common.Checkers;
import ir.sobhan.sobhaneh.common.dto.TokenDTO;
import ir.sobhan.sobhaneh.common.dto.UserDTO;
import ir.sobhan.sobhaneh.common.dto.WorkspaceDTO;
import ir.sobhan.sobhaneh.common.response.ErrorType;
import ir.sobhan.sobhaneh.common.response.Response;
import ir.sobhan.sobhaneh.common.response.ResponseStatus;

import java.util.Random;

public class ConnectWorkspaceService {
    public ConnectWorkspaceService() {
    }
    private String generateToken(){
        String avilableChars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder token = new StringBuilder();
        do{
            Random rnd = new Random();
            for (int i = 0; i < 10; i++){
                char ch = avilableChars.charAt(rnd.nextInt(avilableChars.length()));
                token.append(ch);
            }
        }while (TokenRepository.findByToken(token.toString()) != null);
        return token.toString();

    }
    public Response connectWorkspace(int userId, String workspaceName) {
        Response response = Checkers.checkWorkspaceName(workspaceName);
        if (response.getStatus() != ResponseStatus.OK) return response;
        WorkspaceDTO workspace = WorkspaceRepository.findByName(workspaceName);
        if (workspace == null) return new Response(ErrorType.WORKSPACE_NOT_FOUND);
        UserDTO user = UserRepository.findById(userId);
        if(user == null) return new Response(ErrorType.USER_NOT_FOUND);
        if(!user.isLoggedIn()) return new Response(ErrorType.USER_NOT_LOGGED_IN);
        TokenDTO newToken = new TokenDTO(generateToken(), userId,  workspaceName);
        TokenRepository.addToken(newToken);
        String protocolLine = workspace.getHostIp() + " " + workspace.getPort() + " " + newToken.getToken();
        return new Response(protocolLine);
    }
}
