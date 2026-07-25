package ir.sobhan.sobhaneh.common.dto;


public class TokenDTO {
    private final String token;
    private final int userId;
    private final String workspaceName;
    private final long expTime;

    public TokenDTO(String token, int userId,  String workspaceName) {
        this.token = token;
        this.userId = userId;
        this.workspaceName = workspaceName;
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
