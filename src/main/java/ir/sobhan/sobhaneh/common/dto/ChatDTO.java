//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.common.dto;

import java.util.ArrayList;

public class ChatDTO {

    private int user1Id;
    private int user2Id;
    private ArrayList<MessageDTO> messages;

    public ChatDTO() {
        messages = new ArrayList<>();
    }

    public ChatDTO(int user1Id, int user2Id) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.messages = new ArrayList<>();
    }

    public int getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(int user1Id) {
        this.user1Id = user1Id;
    }

    public int getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(int user2Id) {
        this.user2Id = user2Id;
    }

    public ArrayList<MessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<MessageDTO> messages) {
        this.messages = messages;
    }
}