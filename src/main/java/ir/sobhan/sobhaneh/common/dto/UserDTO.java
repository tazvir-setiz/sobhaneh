//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.common.dto;

public class UserDTO {

    private int id;
    private String phoneNumber;
    private String password;
    private String username;

    public UserDTO() {
    }

    public UserDTO(int id, String phoneNumber, String password, String username) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}