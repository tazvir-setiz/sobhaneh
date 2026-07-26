package ir.sobhan.sobhaneh.centralserver.service;

import ir.sobhan.sobhaneh.centralserver.repository.HostRepository;
import ir.sobhan.sobhaneh.common.Checkers;
import ir.sobhan.sobhaneh.common.dto.HostDTO;
import ir.sobhan.sobhaneh.common.network.Session;
import ir.sobhan.sobhaneh.common.response.ErrorType;
import ir.sobhan.sobhaneh.common.response.Response;
import ir.sobhan.sobhaneh.common.response.ResponseStatus;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;

public class CreateHostService {
    public CreateHostService() {
    }

    public Response createHost(String ip, int startPort, int endPort) {
        Response response = Checkers.checkIp(ip);
        if (response.getStatus() != ResponseStatus.OK) return response;
        response = Checkers.checkPortRange(startPort, endPort);
        if (response.getStatus() != ResponseStatus.OK) return response;
        if (HostRepository.hasConflict(ip, startPort, endPort))
            return new Response(ErrorType.PORT_IN_USE_BY_ANOTHER_HOST);

        HostDTO newHost = new HostDTO(ip, startPort, endPort);
        newHost.setReservedPortCheck(newHost.reserveFreePort());
        return new Response(newHost.getIp() + " " + newHost.getReservedPortCheck());
    }

    public String generateVerifyCode(HostDTO host) throws IOException {
        String avilableChars = "0123456789";
        StringBuilder code = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 10; i++) {
            char ch = avilableChars.charAt(rnd.nextInt(avilableChars.length()));
            code.append(ch);
        }
        int portCheck = host.getReservedPortCheck();
        Socket socket = new Socket(host.getIp(), portCheck);
        Session session = new Session(socket);
        session.sendLine("OK " + code);
        session.close();
        return code.toString();
    }
    public Response verifyAndRegister(HostDTO host, String expectedCode, String receivedCode) {
        host.releasePort(host.getReservedPortCheck());
        if (!expectedCode.equals(receivedCode)) {
            return new Response(ErrorType.INVALID_CODE);
        }
        if (!HostRepository.addHost(host)) {
            return new Response(ErrorType.PORT_IN_USE_BY_ANOTHER_HOST);
        }
        return new Response(ResponseStatus.OK);
    }
}
