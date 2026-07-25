package ir.sobhan.sobhaneh.common;

import ir.sobhan.sobhaneh.common.response.ErrorType;
import ir.sobhan.sobhaneh.common.response.Response;
import ir.sobhan.sobhaneh.common.response.ResponseStatus;

public class Checkers {
    private Checkers() {}
    public static Response checkPhoneNumber(String phN) {
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

    public static Response checkPassword(String password) {
        if (password == null) return new Response(ErrorType.PASSWORD_INVALID);
        if (password.isBlank()) return new Response(ErrorType.PASSWORD_INVALID);
        if (password.length() < 6) return new Response(ErrorType.PASSWORD_INVALID);

        return new Response(ResponseStatus.OK);
    }
}
