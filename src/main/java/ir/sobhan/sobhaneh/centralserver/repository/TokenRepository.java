package ir.sobhan.sobhaneh.centralserver.repository;

import ir.sobhan.sobhaneh.common.dto.TokenDTO;

import java.util.ArrayList;
import java.util.HashMap;

public class TokenRepository {
    private static final HashMap<String, TokenDTO> tokens = new HashMap<>();
    private TokenRepository() {}
    public static boolean addToken(TokenDTO token) {
        if (tokens.containsKey(token.getToken())) {
            return false;
        }
        tokens.put(token.getToken(), token);
        return true;
    }
    public static TokenDTO findByToken(String token) {
        return tokens.get(token);
    }
    public static void removeToken(String token) {
        tokens.remove(token);
    }
    public static void removeExpiredTokens() {
        ArrayList<String> expTokens = new ArrayList<>();
        for (TokenDTO token : tokens.values()) {
            if(System.currentTimeMillis() > token.getExpTime()) expTokens.add(token.getToken());
        }
        for (String token : expTokens) {
            tokens.remove(token);
        }
    }


}
