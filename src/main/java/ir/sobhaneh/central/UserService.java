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
        User newUser = new User(counter.incrementAndGet(), phoneNumber, password);
        users.put(phoneNumber, newUser);
        return RegisterResult.OK;
    }

    private static boolean checkPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        if(!phoneNumber.startsWith("09")) return false;
        if(!(phoneNumber.length() == 11)) return false;
        for(int i = 0; i < phoneNumber.length(); i++) {
            if(!Character.isDigit(phoneNumber.charAt(i))) return false;
        }
        return true;
    }

    private static boolean checkPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (hasLetter && hasDigit) {
                return true;
            }
        }
        return false;
    }

    public LoginResult login(String phoneNumber, String password) {
        if(!users.containsKey(phoneNumber)) return LoginResult.USER_NOT_FOUND;
        User user = users.get(phoneNumber);
        if(!user.getPassword().equals(password)) return LoginResult.WRONG_PASSWORD;
        return LoginResult.OK;
    }

}