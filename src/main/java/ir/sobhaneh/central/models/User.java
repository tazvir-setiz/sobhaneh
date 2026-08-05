package ir.sobhaneh.central.models;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Setter
public class User implements Serializable {
    private static AtomicLong counter = new AtomicLong(0);
    private final long id;
    private final String phoneNumber;
    private final String password;

    public User(String phoneNumber, String password) {
        this.id = counter.incrementAndGet();
        this.phoneNumber = phoneNumber;
        this.password = password;
    }
}
