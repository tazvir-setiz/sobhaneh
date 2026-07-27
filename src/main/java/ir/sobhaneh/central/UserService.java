//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.central;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class UserService {
    private AtomicLong  counter = new AtomicLong(0);
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    public UserService() {}
    public RegisterResult register(String phoneNumber, String password) {
        if(!checkPhoneNumber(phoneNumber)) return RegisterResult.INVALID_PHONE_FORMAT;
        if(users.containsKey(phoneNumber)) return RegisterResult.PHONE_ALREADY_EXISTS;
        if(!checkPassword(password)) return RegisterResult.INVALID_PASSWORD_FORMAT;
        return RegisterResult.OK;
    }

    private static boolean checkPhoneNumber(String phoneNumber) {
        return true;
    }
    private static boolean checkPassword(String password) {
        return true;
    }

    public LoginResult login(String phoneNumber, String password) {
        if(!users.containsKey(phoneNumber)) return LoginResult.USER_NOT_FOUND;
        User user = users.get(phoneNumber);
        if(!user.getPassword().equals(password)) return LoginResult.WRONG_PASSWORD;
        return LoginResult.OK;
    }

}
