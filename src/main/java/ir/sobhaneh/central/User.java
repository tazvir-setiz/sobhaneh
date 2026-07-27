//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.central;

public class User {
    private final long id;
    private final String phoneNumber;
    private final String password;

    public User(int id, String phoneNumber, String password) {
        this.id = id;
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
