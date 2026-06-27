//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.common.message;

import ir.sobhan.sobhaneh.common.user.UserDTO;

public class MessageDTO {
    private final String message;
    UserDTO messageSender;
    UserDTO messageReceiver;

    public MessageDTO(String message, UserDTO messageSender, UserDTO messageReceiver) {
        this.message = message;
        this.messageSender = messageSender;
        this.messageReceiver = messageReceiver;
    }

    public String getMessage() {
        return message;
    }

    public UserDTO getMessageSender() {
        return messageSender;
    }

    public UserDTO getMessageReceiver() {
        return messageReceiver;
    }
}