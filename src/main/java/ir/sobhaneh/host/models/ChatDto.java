package ir.sobhaneh.host.models;

import java.util.List;

public record ChatDto(String usernameA, String usernameB,
                      List<Message> messages,
                      int unreadCountA, int unreadCountB) {
}
