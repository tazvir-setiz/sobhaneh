//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.common.message;

public class MessageDTO {
    private final String message;
    private final int senderId;
    private final int receiverId;

    public MessageDTO(String message, int senderId, int receiverId) {
        this.message = message;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }
}