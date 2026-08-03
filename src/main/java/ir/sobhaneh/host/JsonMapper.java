package ir.sobhaneh.host;

import com.google.gson.Gson;
import ir.sobhaneh.host.models.ChatSummary;
import ir.sobhaneh.host.models.IncomingMessagePayload;
import ir.sobhaneh.host.models.Message;

import java.util.List;

public class JsonMapper {
    private final Gson gson = new Gson();

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
