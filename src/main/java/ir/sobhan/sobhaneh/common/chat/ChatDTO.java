package ir.sobhan.sobhaneh.common.chat;
import ir.sobhan.sobhaneh.common.message.MessageDTO;

import java.util.LinkedList;


public class ChatDTO {
    private final LinkedList<MessageDTO> messages;
    private final int senderId;
    private final int receiverId;
    ChatDTO(int senderId, int receiver) {
        this.senderId = senderId;
        this.receiverId = receiver;
        this.messages = new LinkedList<>();
    }
    public int getSenderId() {
        return senderId;
    }
    public int getReceiverId() {
        return receiverId;
    }
    public LinkedList<MessageDTO> getMessages() {
        return messages;
    }


}
