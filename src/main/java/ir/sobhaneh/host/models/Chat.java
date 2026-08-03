package ir.sobhaneh.host.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Getter
public class Chat {
    private final String usernameA;
    private final String usernameB;
    private final List<Message> messages = new CopyOnWriteArrayList<>();
    private final AtomicInteger lastSeq = new AtomicInteger(0);

    public static String buildKey(String usernameA, String usernameB) {
        if (usernameA.compareTo(usernameB) > 0) {
            return usernameA + "-" + usernameB;
        } else {
            return usernameB + "-" + usernameA;
        }
    }

    public int nextSeq() {
        return lastSeq.incrementAndGet();
    }

    public void addMessage(Message message) {
        messages.add(message);
    }
}