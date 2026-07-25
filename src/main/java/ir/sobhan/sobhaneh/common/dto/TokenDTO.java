package ir.sobhan.sobhaneh.common.dto;


public class TokenDTO {
    private final String token;
    private final int userId;
    private final long expTime;

    public TokenDTO(String token, int userId) {
        this.token = token;
        this.userId = userId;
        this.expTime = System.currentTimeMillis() + 5 * 60 * 1000;
    }

    public String getToken() {
        return token;
    }

    public int getUserId() {
        return userId;
    }

    public long getExpTime() {
        return expTime;
    }

}
