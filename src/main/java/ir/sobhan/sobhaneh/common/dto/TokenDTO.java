package ir.sobhan.sobhaneh.common.dto;

import java.sql.Time;

public class TokenDTO {
    public String token;
    private int userId;
    private long expTime;
    public TokenDTO(String token, int userId) {
        this.token = token;
        this.userId = userId;
        this.expTime = System.currentTimeMillis() + 5 *  60 * 1000;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public long getExpTime() {
        return expTime;
    }

}
