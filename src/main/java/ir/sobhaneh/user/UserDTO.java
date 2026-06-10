//in the name of ALLAH
//YA MAHDI
package ir.sobhaneh.user;

public class UserDTO {

    private final String phoneNumber_;
    private final String password_;

    UserDTO(String phoneNumber, String password) {
        phoneNumber_ = phoneNumber;
        password_ = password;
    }

    public String getPhoneNumber() {
        return phoneNumber_;
    }

    public String getPassword() {
        return password_;
    }
}