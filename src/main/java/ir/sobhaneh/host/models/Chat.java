//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.host.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Getter
public class Chat implements Serializable {
    private final String usernameA;
    private final String usernameB;
    private final List<Message> messages = new CopyOnWriteArrayList<>();
    private final AtomicInteger lastSeq = new AtomicInteger(0);
    private final AtomicInteger unreadCountForA = new AtomicInteger(0);
    private final AtomicInteger unreadCountForB = new AtomicInteger(0);

    public Chat(String usernameA, String usernameB, List<Message> messages, int lastSeq, int unreadCountA, int unreadCountB) {
        this.usernameA = usernameA;
        this.usernameB = usernameB;
        this.lastSeq.set(lastSeq);
        this.unreadCountForA.set(unreadCountA);
        this.unreadCountForB.set(unreadCountB);
        messages.stream().forEach(this::addMessage);
    }

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

    public void setLastSeq(int seq) {
        lastSeq.set(seq);
    }

    public void addMessage(Message message) {
        messages.add(message);
    }


    public void incrementUnreadFor(String username) {
        if (isUserA(username)) {
            unreadCountForA.incrementAndGet();
        } else {
            unreadCountForB.incrementAndGet();
        }
    }

    public void setUnreadFor(String username, int unreadCount) {
        if (isUserA(username)) {
            unreadCountForA.set(unreadCount);
        } else {
            unreadCountForB.set(unreadCount);
        }
    }
    public void decrementUnreadFor(String username) {
        if (isUserA(username)) {
            unreadCountForA.decrementAndGet();
        } else {
            unreadCountForB.decrementAndGet();
        }
    }

    public int getUnreadCountFor(String username) {
        if (isUserA(username)) {
            return unreadCountForA.get();
        } else {
            return unreadCountForB.get();
        }
    }

    public void clearUnreadFor(String username) {
        if (isUserA(username)) {
            unreadCountForA.set(0);
        } else {
            unreadCountForB.set(0);
        }
    }

    private boolean isUserA(String username) {
        return usernameA.equals(username);
    }
}