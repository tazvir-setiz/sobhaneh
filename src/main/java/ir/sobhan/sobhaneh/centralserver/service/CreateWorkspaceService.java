//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.centralserver.service;

import ir.sobhan.sobhaneh.centralserver.repository.HostRepository;
import ir.sobhan.sobhaneh.centralserver.repository.WorkspaceRepository;
import ir.sobhan.sobhaneh.common.Checkers;
import ir.sobhan.sobhaneh.common.dto.HostDTO;
import ir.sobhan.sobhaneh.common.dto.WorkspaceDTO;
import ir.sobhan.sobhaneh.common.response.ErrorType;
import ir.sobhan.sobhaneh.common.response.Response;
import ir.sobhan.sobhaneh.common.response.ResponseStatus;

public class CreateWorkspaceService {
    public CreateWorkspaceService() {}
    public Response createWorkspace(int ownerId, String workspaceName) {
        Response response = Checkers.checkWorkspaceName(workspaceName);
        if(response.getStatus() != ResponseStatus.OK) return response;
        if(WorkspaceRepository.findByName(workspaceName) != null) return new Response(ErrorType.WORKSPACE_ALREADY_EXISTS);
        HostDTO host = HostRepository.getFreeHost();
        if(host == null) return new Response(ErrorType.NO_FREE_HOST);
        int freePort = host.getFreePort();
        host.occupyPort(freePort);
        WorkspaceDTO newWorkspace = new WorkspaceDTO(workspaceName, host.getIp(), freePort, ownerId);
        if(!WorkspaceRepository.addWorkspace(newWorkspace)){
            host.releasePort(freePort);
            return new Response(ErrorType.WORKSPACE_ALREADY_EXISTS);
        }
        return new  Response(newWorkspace);
    }
}
