//in the name of ALLAH
//YA MAHDI
package ir.sobhaneh.user;


import java.util.Scanner;

public class User{
    private String name_;
    private String password_;


    private User(String name, String password){
        this.name_ = name;
        this.password_ = password;
    }

    public User userCreate(){
        Scanner input = new Scanner(System.in);
        System.out.print("Enter user name: ");
        String name = input.nextLine();
        System.out.print("Enter user password: ");
        String password = input.nextLine();
        return new User(name, password);
    }



    public String getPassword() {
        return password_;
    }
    public User setPassword() {
        System.out.print("Enter new password for " + name_ + ": ");
        Scanner input = new Scanner(System.in);
        String password = input.nextLine();

        return this.setPassword(password);
    }
    public User setPassword(String password) {
        return new User(name_, password);
    }




    public String getName() {
        return name_;
    }

    public User setName(){
        System.out.print("Enter new user name for " + name_ + ": ");
        Scanner input = new Scanner(System.in);
        String name = input.nextLine();
        return this.setName(name);
    }

    public User setName(String name) {
        return new User(name, password_);
    }


}