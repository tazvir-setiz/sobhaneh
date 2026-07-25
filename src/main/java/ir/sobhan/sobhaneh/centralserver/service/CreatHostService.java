package ir.sobhan.sobhaneh.centralserver.service;

import ir.sobhan.sobhaneh.centralserver.repository.HostRepository;
import ir.sobhan.sobhaneh.common.Checkers;
import ir.sobhan.sobhaneh.common.dto.HostDTO;
import ir.sobhan.sobhaneh.common.response.ErrorType;
import ir.sobhan.sobhaneh.common.response.Response;
import ir.sobhan.sobhaneh.common.response.ResponseStatus;

public class CreatHostService {
    public CreatHostService() {}
    public Response createHost(String ip, int startPort, int endPort){
        Response response = Checkers.checkIp(ip);
        if(response.getStatus() !=  ResponseStatus.OK) return response;
        response = Checkers.checkPortRange(startPort, endPort);
        if(response.getStatus() !=  ResponseStatus.OK) return response;
        HostDTO newHost = new HostDTO(ip, startPort, endPort);
        if(!HostRepository.addHost(newHost)) return new Response(ErrorType.INVALID_PORT_RANGE);

        return new Response(ResponseStatus.OK);
    }
}
