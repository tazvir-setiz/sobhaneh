package ir.sobhan.sobhaneh.common.chat;

import ir.sobhan.sobhaneh.common.message.MessageDTO;
import ir.sobhan.sobhaneh.common.user.UserService;

import java.util.LinkedList;

public class ChatService {
    public static ChatDTO createChatDTO(int senderId, String reciverName) {
        return new ChatDTO(senderId, UserService.find(reciverName));
    }

    public void addMessage(LinkedList<MessageDTO> messages, MessageDTO message) {
        messages.add(message);
    }
}
