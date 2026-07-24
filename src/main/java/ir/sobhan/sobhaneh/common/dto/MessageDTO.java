//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.common.dto;

public class MessageDTO {

    private int seq;
    private int senderId;
    private int receiverId;
    private String type;
    private String body;

    public MessageDTO() {
    }

    public MessageDTO(int seq, int senderId, int receiverId, String type, String body) {
        this.seq = seq;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.type = type;
        this.body = body;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}