//in the name of ALLAH
//YA MAHDI
package ir.sobhan.sobhaneh.common.user;

public class UserDTO {

    private final String phoneNumber_;
    private final String password_;

    UserDTO(String phoneNumber, String password) {
        phoneNumber_ = phoneNumber;
        password_ = password;
    }

    String getPhoneNumber() {
        return phoneNumber_;
    }

    String getPassword() {
        return password_;
    }
}