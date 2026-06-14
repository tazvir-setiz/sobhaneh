package ir.sobhaneh;


import ir.sobhaneh.user.UserDTO;

import static ir.sobhaneh.user.UserService.userCreate;

public class test {
    public static void main(String[] args) {
        UserDTO user = userCreate();
    }
}
