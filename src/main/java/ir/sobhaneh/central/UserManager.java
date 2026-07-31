package ir.sobhaneh.central;

import ir.sobhaneh.central.models.User;

import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    public final Object registerLock = new Object();
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    public User findByPhone(String phone) {
        return users.getOrDefault(phone, null);
    }

    public boolean checkPhoneNumber(String phone) {
        return true;
    }

    public boolean checkPassword(String password) {
        return true;
    }

    public String register(String phoneNumber, String password) {
        synchronized (registerLock) {
            if (findByPhone(phoneNumber) != null) return "ERROR User already exists";
            if (!checkPhoneNumber(phoneNumber)) return "ERROR Invalid Phone Number";
            if (!checkPassword(password)) return "ERROR Invalid Password";
            users.put(phoneNumber, new User(phoneNumber, password));
            return "OK";
        }
    }
    public String login(String phoneNumber, String password) {
        if (!checkPhoneNumber(phoneNumber)) return "ERROR Invalid Phone Number";
        if(findByPhone(phoneNumber) == null) return "ERROR User doesn't exists";
        if (!checkPassword(password)) return "ERROR Invalid Password";
        User user = users.get(phoneNumber);
        if(!user.getPassword().equals(password)) return "ERROR Incorrect Password";
        return "OK";
    }
}
