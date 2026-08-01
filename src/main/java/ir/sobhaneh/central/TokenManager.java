package ir.sobhaneh.central;

import ir.sobhaneh.central.models.Token;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class TokenManager {
    private static final ConcurrentHashMap<String, Token> tokens = new ConcurrentHashMap<>();
    private static final int MAX_TOKEN_LENGTH = 10;
    private static final long TOKEN_EXPIRATION_MILLISECONDS = 5 * 60 * 1000;

    private String tokenGenerator() {
        String validChar = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder token = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < MAX_TOKEN_LENGTH; i++) {
            token.append(validChar.charAt(random.nextInt(validChar.length())));
        }
        return token.toString();
    }

    public Token findByToken(String token) {
        return tokens.get(token);
    }


    public Token createToken(long creatorUserId, String workspaceName) {
        Token newToken = new Token(tokenGenerator(), creatorUserId, workspaceName, System.currentTimeMillis() + TOKEN_EXPIRATION_MILLISECONDS);
        tokens.put(newToken.token(), newToken);
        return newToken;
    }

    public Token resolve(String token) {
        Token foundToken = tokens.get(token);
        if(foundToken.isExpired()) {
            tokens.remove(token);
            return null;
        }
        return foundToken;
    }
}
