package ir.sobhan.sobhaneh.centralserver.service;

import ir.sobhan.sobhaneh.centralserver.repository.UserRepository;
import ir.sobhan.sobhaneh.common.Checkers;
import ir.sobhan.sobhaneh.common.dto.UserDTO;
import ir.sobhan.sobhaneh.common.response.ErrorType;
import ir.sobhan.sobhaneh.common.response.Response;
import ir.sobhan.sobhaneh.common.response.ResponseStatus;

public class LoginService {
    public LoginService() {}

    public Response login(String phoneNumber, String password) {
        Response response = Checkers.checkPhoneNumber(phoneNumber);
        if (response.getStatus() != ResponseStatus.OK) return response;
        response = Checkers.checkPassword(password);
        if (response.getStatus() != ResponseStatus.OK) return response;

        UserDTO user = UserRepository.findByPhone(phoneNumber);
        if(user == null) return new Response(ErrorType.USER_NOT_FOUND);
        if(!(user.getPassword().equals(password))) return new Response(ErrorType.WRONG_PASSWORD);
        user.setLoggedIn(true);
        return new Response(ResponseStatus.OK);
    }
}
