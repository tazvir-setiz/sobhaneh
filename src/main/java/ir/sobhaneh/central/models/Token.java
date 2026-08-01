package ir.sobhaneh.central.models;

public record Token(String token, long creatorUserId, String workspaceName, long expiresAtMillis) {
    public boolean isExpired() {
        return expiresAtMillis < System.currentTimeMillis();
    }
}
