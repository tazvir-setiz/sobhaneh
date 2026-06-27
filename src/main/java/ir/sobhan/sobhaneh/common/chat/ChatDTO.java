package ir.sobhan.sobhaneh.common.chat;
import ir.sobhan.sobhaneh.common.message.MessageDTO;
import ir.sobhan.sobhaneh.common.user.UserDTO;

import java.util.LinkedList;


public class ChatDTO {
    private LinkedList<MessageDTO> messages;
    private final UserDTO sender;
    private final UserDTO receiver;
    ChatDTO(UserDTO sender, UserDTO receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }
    public UserDTO getSender() {
        return sender;
    }
    public UserDTO getReceiver() {
        return receiver;
    }
    public void addMessage(MessageDTO message) {
        messages.addLast(message);
    }


}
