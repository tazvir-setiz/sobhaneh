package ir.sobhaneh.host.models;

import java.util.List;
import java.util.Map;

public record ChatStoreDto(List<ChatDto> chats, Map<Long, String> usernameByUserId) {
}