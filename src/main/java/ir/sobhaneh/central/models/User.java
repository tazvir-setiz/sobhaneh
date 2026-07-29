package ir.sobhaneh.central.models;

import java.util.concurrent.atomic.AtomicLong;

public class User {
    private static AtomicLong counter = new AtomicLong(0);
    private final long id;
    private final String phoneNumber;
    private final String password;

    public User(String phoneNumber, String password) {
        this.id = counter.incrementAndGet();
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    public static AtomicLong getCounter() {
        return counter;
    }

    public static void setCounter(AtomicLong counter) {
        User.counter = counter;
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
