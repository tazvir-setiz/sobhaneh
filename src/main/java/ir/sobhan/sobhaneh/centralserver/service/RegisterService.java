package ir.sobhan.sobhaneh.centralserver.service;

import ir.sobhan.sobhaneh.centralserver.repository.UserRepository;
import ir.sobhan.sobhaneh.common.dto.UserDTO;
import ir.sobhan.sobhaneh.common.response.ErrorType;
import ir.sobhan.sobhaneh.common.response.Response;
import ir.sobhan.sobhaneh.common.response.ResponseStatus;

public class RegisterService {

    private Response checkPhoneNumber(String phN) {
        if (phN == null) return new Response(ErrorType.PHONE_NUMBER_INVALID);
        if (phN.length() != 11) return new Response(ErrorType.PHONE_NUMBER_INVALID);
        if (phN.startsWith("09") == false) return new Response(ErrorType.PHONE_NUMBER_INVALID);
        for (int i = 0; i < phN.length(); i++) {
            if (!Character.isDigit(phN.charAt(i))) {
                return new Response(ErrorType.PHONE_NUMBER_INVALID);
            }
        }

        return new Response(ResponseStatus.OK);
    }

    private Response checkPassword(String password) {
        if (password == null) return new Response(ErrorType.PASSWORD_INVALID);
        if (password.isBlank()) return new Response(ErrorType.PASSWORD_INVALID);
        if (password.length() < 6) return new Response(ErrorType.PASSWORD_INVALID);

        return new Response(ResponseStatus.OK);
    }

    public RegisterService() {
    }

    public Response register(String phoneNumber, String password) {
        Response response = checkPhoneNumber(phoneNumber);
        if (response.getStatus() != ResponseStatus.OK) return response;

        response = checkPassword(password);
        if (response.getStatus() != ResponseStatus.OK) return response;

        if (UserRepository.findByPhone(phoneNumber) != null) return new Response(ErrorType.USER_ALREADY_EXISTS);

        UserDTO newUser = new UserDTO(UserRepository.getNextId(), phoneNumber, password);

        UserRepository.addUser(newUser);
        return new Response(ResponseStatus.OK);
    }
}
