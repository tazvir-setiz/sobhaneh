package ir.sobhan.sobhaneh.common;

import ir.sobhan.sobhaneh.common.response.ErrorType;
import ir.sobhan.sobhaneh.common.response.Response;
import ir.sobhan.sobhaneh.common.response.ResponseStatus;

public class Checkers {
    private Checkers() {
    }

    public static Response checkPhoneNumber(String phN) {
        if (phN == null) return new Response(ErrorType.PHONE_NUMBER_INVALID);
        if (phN.length() != 11) return new Response(ErrorType.PHONE_NUMBER_INVALID);
        if (!phN.startsWith("09")) return new Response(ErrorType.PHONE_NUMBER_INVALID);
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

    public static Response checkIp(String ip) {
        //باید درستش کنم یکم سخته
        return new Response(ResponseStatus.OK);
    }

    public static Response checkPortRange(int startPort, int endPort) {
        if(startPort >= endPort) return new Response(ErrorType.INVALID_PORT_RANGE);
        if(startPort < 10000) return new Response(ErrorType.PORT_NUMBER_MUST_BE_AT_LEAST_10000);
        if(endPort - startPort + 1 > 1000) return new Response(ErrorType.AT_MOST_1000_PORTS_ALLOWED);
        return new Response(ResponseStatus.OK);
    }

    public static Response checkWorkspaceName(String workspaceName) {
        if(workspaceName == null) return new Response(ErrorType.INVALID_WORKSPACE_NAME);
        for(char c : workspaceName.toCharArray()) {
            if(!(Character.isLetterOrDigit(c) || Character.isDigit(c) || Character.isUpperCase(c))) return new Response(ErrorType.INVALID_WORKSPACE_NAME);
        }
        return new Response(ResponseStatus.OK);
    }

    public static Response checkToken(String token) {
        if(token == null) return new Response(ErrorType.INVALID_TOKEN);
        for(char c : token.toCharArray()) {
            if(!(Character.isLetterOrDigit(c) || Character.isDigit(c))) return new Response(ErrorType.INVALID_WORKSPACE_NAME);
        }
        return new Response(ResponseStatus.OK);
    }
}
