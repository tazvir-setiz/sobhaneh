package ir.sobhaneh.central;

import java.util.concurrent.atomic.AtomicLong;

public class User {
    private static final AtomicLong counter = new AtomicLong(0);
    private final long id;
    private final String phoneNumber;
    private final String password;

    public User(String phoneNumber, String password) {
        this.id = counter.incrementAndGet();
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPassword() {
        return password;
    }
}
