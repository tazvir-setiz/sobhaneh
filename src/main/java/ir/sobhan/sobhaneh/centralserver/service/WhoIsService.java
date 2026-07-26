//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.centralserver.service;

import ir.sobhan.sobhaneh.centralserver.repository.TokenRepository;
import ir.sobhan.sobhaneh.centralserver.repository.UserRepository;
import ir.sobhan.sobhaneh.common.Checkers;
import ir.sobhan.sobhaneh.common.dto.TokenDTO;
import ir.sobhan.sobhaneh.common.dto.UserDTO;
import ir.sobhan.sobhaneh.common.response.ErrorType;
import ir.sobhan.sobhaneh.common.response.Response;
import ir.sobhan.sobhaneh.common.response.ResponseStatus;

public class WhoIsService {
    public WhoIsService() {}
    public Response whois(String token){
        TokenRepository.removeExpiredTokens();
        Response response = Checkers.checkToken(token);
        if(response.getStatus() != ResponseStatus.OK) return response;
        TokenDTO token_ = TokenRepository.findByToken(token);
        if(token_ == null) return new Response(ErrorType.INVALID_TOKEN);
        if(token_.isExpired()) return new Response(ErrorType.TOKEN_EXPIRED);
        UserDTO owner = UserRepository.findById(token_.getUserId());
        TokenRepository.removeToken(token);
        return new Response(owner);
    }
}
