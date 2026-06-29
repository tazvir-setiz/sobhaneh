//in the name of ALLAH
//YA MAHDI
package ir.sobhan.sobhaneh.common.user;

import java.io.Serializable;

public class UserDTO implements Serializable {

    private final String phoneNumber;
    private final long hashPassword;
    UserDTO(String phoneNumber, String password) {
        this.phoneNumber = phoneNumber;
        this.hashPassword = password.hashCode();
    }

    String getPhoneNumber() {
        return phoneNumber;
    }

    long getHashPassword() {
        return hashPassword;
    }
}