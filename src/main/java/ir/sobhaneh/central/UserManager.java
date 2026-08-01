package ir.sobhaneh.central;

import ir.sobhaneh.central.models.User;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class UserManager {
    public final Object registerLock = new Object();
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    private static final Pattern PHONE_PATTERN = Pattern.compile("^09[0-9]{9}$");
    private static final int PHONE_LENGTH = 11;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 20;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,20}$"
    );
    public User findByPhone(String phone) {
        return users.getOrDefault(phone, null);
    }

    private static String validatePhoneNumber(String phone) {
        if (phone == null || phone.isBlank()) {
            return "ERROR Phone number cannot be empty";
        }
        if (phone.length() != PHONE_LENGTH) {
            return "ERROR Phone number must be exactly " + PHONE_LENGTH + " digits";
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            return "ERROR Phone number must start with 09 and contain only digits";
        }
        return null;
    }

    private static String validatePassword(String password) {
        if (password == null || password.isBlank()) {
            return "ERROR Password cannot be empty";
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "ERROR Password must be at least " + MIN_PASSWORD_LENGTH + " characters";
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return "ERROR Password must be at most " + MAX_PASSWORD_LENGTH + " characters";
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return "ERROR Password must contain at least one digit, one lowercase, one uppercase, and one special character (@#$%^&+=!)";
        }
        return null;
    }

    public String register(String phoneNumber, String password) {
         String phoneValidation = validatePhoneNumber(phoneNumber);
         if (phoneValidation != null) {
             return phoneValidation;
         }

         String passwordValidation = validatePassword(password);
         if (passwordValidation != null) {
             return passwordValidation;
         }
         synchronized (registerLock) {
            if (findByPhone(phoneNumber) != null) return "ERROR User already exists";
            users.put(phoneNumber, new User(phoneNumber, password));
            return "OK";
        }
    }
    public String login(String phoneNumber, String password) {
        String phoneValidation = validatePhoneNumber(phoneNumber);
        if (phoneValidation != null) {
            return phoneValidation;
        }

        String passwordValidation = validatePassword(password);
        if (passwordValidation != null) {
            return passwordValidation;
        }
        if(findByPhone(phoneNumber) == null) return "ERROR User doesn't exists";
        User user = users.get(phoneNumber);
        if(!user.getPassword().equals(password)) return "ERROR Incorrect Password";
        return "OK";
    }
}
