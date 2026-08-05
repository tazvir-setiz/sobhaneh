//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.host;

import ir.sobhaneh.host.models.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ChatStore implements Serializable {
    private static final String EMPTY_JSON_ARRAY = "[]";

    private final ConcurrentHashMap<String, Chat> chatsByKey = new ConcurrentHashMap<>();
    transient private JsonMapper jsonMapper = null;

    private void jsonMapperInit() {
        if (jsonMapper == null) {
            jsonMapper = new JsonMapper();
        }
    }

    public int addMessage(String from, String to, String type, String body, boolean recipientOnline) {
        Chat chat = findOrCreateChat(from, to);

        int seq = chat.nextSeq();
        Message message = new Message(seq, from, type, body);
        chat.addMessage(message);

        chat.incrementUnreadFor(to);
        if (recipientOnline) {
            chat.decrementUnreadFor(to);
        }

        return seq;
    }

    public String buildChatsJson(String username) {
        List<ChatSummary> summaries = new ArrayList<>();
        for (Chat chat : chatsByKey.values()) {
            String otherParty = resolveOtherParty(chat, username);
            if (otherParty == null) {
                continue;
            }
            int unreadCount = chat.getUnreadCountFor(username);
            summaries.add(new ChatSummary(otherParty, unreadCount));
        }
        jsonMapperInit();
        return jsonMapper.chatSummariesToJson(summaries);
    }

    public String buildMessagesJson(String owner, String otherParty) {
        String key = Chat.buildKey(owner, otherParty);
        Chat chat = chatsByKey.get(key);
        if (chat == null) {
            return EMPTY_JSON_ARRAY;
        }
        chat.clearUnreadFor(owner);
        jsonMapperInit();
        return jsonMapper.messagesToJson(chat.getMessages());
    }

    private Chat findOrCreateChat(String from, String to) {
        String key = Chat.buildKey(from, to);
        return chatsByKey.computeIfAbsent(key, k -> new Chat(from, to));
    }

    private String resolveOtherParty(Chat chat, String username) {
        if (username.equals(chat.getUsernameA())) {
            return chat.getUsernameB();
        }
        if (username.equals(chat.getUsernameB())) {
            return chat.getUsernameA();
        }
        return null;
    }

    public ChatStoreDto toChatStoreData() {
        List<ChatDto> chatDtos = chatsByKey.values()
                .stream()
                .map(chat -> new ChatDto(
                        chat.getUsernameA(),
                        chat.getUsernameB(),
                        chat.getMessages(),
                        chat.getUnreadCountFor(chat.getUsernameA()),
                        chat.getUnreadCountFor(chat.getUsernameB())
                ))
                .collect(Collectors.toList());
        ChatStoreDto newChatStoreDto = new ChatStoreDto(chatDtos);
        return newChatStoreDto;
    }

    public void fromChatStoreData(ChatStoreDto data) {

    }


}