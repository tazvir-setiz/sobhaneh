package ir.sobhan.sobhaneh.common.chat;

import ir.sobhan.sobhaneh.common.user.UserDTO;
import ir.sobhan.sobhaneh.common.user.UserService;

public class ChatService {
    public static ChatDTO createChatDTO(UserDTO sender, String reciverName) {
        UserDTO reciver = UserService.find(reciverName);
        return new ChatDTO(sender, reciver);
    }
}
