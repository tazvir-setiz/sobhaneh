//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.common.dto;

public class MessageDTO {

    private int seq;
    private String from;
    private MessageType type;
    private String body;

    public MessageDTO() {
    }

    public MessageDTO(int seq, String from, MessageType type, String body) {
        this.seq = seq;
        this.from = from;
        this.type = type;
        this.body = body;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}