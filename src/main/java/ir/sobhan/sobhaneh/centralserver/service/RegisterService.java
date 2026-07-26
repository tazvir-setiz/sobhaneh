package ir.sobhan.sobhaneh.centralserver.service;

import ir.sobhan.sobhaneh.centralserver.repository.UserRepository;
import ir.sobhan.sobhaneh.common.Checkers;
import ir.sobhan.sobhaneh.common.dto.UserDTO;
import ir.sobhan.sobhaneh.common.response.ErrorType;
import ir.sobhan.sobhaneh.common.response.Response;
import ir.sobhan.sobhaneh.common.response.ResponseStatus;

public class RegisterService {

    public RegisterService() {
    }

    public Response register(String phoneNumber, String password) {
        Response response = Checkers.checkPhoneNumber(phoneNumber);
        if (response.getStatus() != ResponseStatus.OK) return response;

        response = Checkers.checkPassword(password);
        if (response.getStatus() != ResponseStatus.OK) return response;

        if (UserRepository.findByPhone(phoneNumber) != null) return new Response(ErrorType.USER_ALREADY_EXISTS);

        UserDTO newUser = new UserDTO(UserRepository.getNextId(), phoneNumber, password);

        UserRepository.addUser(newUser);
        return new Response(new RegisterService());
    }
}
