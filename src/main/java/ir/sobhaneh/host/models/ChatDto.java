package ir.sobhaneh.host.models;

import java.util.List;

public record ChatDto(String usernameA, String usernameB,
                      List<Message> messages, int lastSeq,
                      int unreadCountA, int unreadCountB) {
}
