//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.common.dto;

public class ChatSummaryDTO {

    private String name;
    private int unreadCount;

    public ChatSummaryDTO() {
    }

    public ChatSummaryDTO(String name, int unreadCount) {
        this.name = name;
        this.unreadCount = unreadCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}