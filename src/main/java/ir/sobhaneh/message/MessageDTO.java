//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.message;

import ir.sobhaneh.user.UserDTO;
public class MessageDTO {
    private String message;
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