package ir.sobhaneh.host;

import com.google.gson.Gson;
import ir.sobhaneh.host.models.ChatSummary;
import ir.sobhaneh.host.models.IncomingMessagePayload;
import ir.sobhaneh.host.models.Message;

import java.util.List;

public class JsonMapper {
    private final Gson gson = new Gson();

    public static void main(String... args) {
        JsonMapper mapper = new JsonMapper();

// تست پارس ورودی
        IncomingMessagePayload payload = mapper.parseIncomingMessage("{\"type\": \"text\", \"body\": \"salam\"}");
        System.out.println(payload.type() + " / " + payload.body());

// تست تبدیل پیام تکی
        Message msg = new Message(1, "ahmad", "text", "salam");
        System.out.println(mapper.messageToJson(msg));

// تست تبدیل لیست پیام‌ها
        System.out.println(mapper.messagesToJson(List.of(msg)));

// تست تبدیل لیست خلاصه‌ی چت‌ها
        ChatSummary summary = new ChatSummary("saeed", 2);
        System.out.println(mapper.chatSummariesToJson(List.of(summary)));
    }

    public IncomingMessagePayload parseIncomingMessage(String json) {
        return gson.fromJson(json, IncomingMessagePayload.class);
    }

    public String messageToJson(Message message) {
        return gson.toJson(message);
    }

    public String messagesToJson(List<Message> messages) {
        return gson.toJson(messages);
    }

    public String chatSummariesToJson(List<ChatSummary> chatSummaries) {
        return gson.toJson(chatSummaries);
    }
}
