//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.centralserver.repository;

import ir.sobhan.sobhaneh.common.dto.UserDTO;

import java.util.HashMap;

public class UserRepository {
    private static final HashMap<Integer, UserDTO> users = new HashMap<>();

    private UserRepository() {}

    public static boolean addUser(UserDTO user) {
        if (users.containsKey(user.getId())) {
            return false;
        }
        users.put(user.getId(), user);
        return true;
    }
    public static boolean removeUser(UserDTO user) {
        if (users.containsKey(user.getId())) {
            users.remove(user.getId());
            return true;
        }
        return false;
    }
    public static UserDTO findById(int id) {
        if (users.containsKey(id)) {
            return users.get(id);
        }
        return null;
    }
    public static UserDTO findByPhone(String phone) {
        for (UserDTO user : users.values()) {
            if (user.getPhoneNumber().equals(phone)) {
                return user;
            }
        }
        return null;
    }

}
