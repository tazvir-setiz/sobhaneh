//in the name of ALLAH
//YA MAHDI


package ir.sobhaneh.user;

import java.util.Scanner;

public class User{
    //this class is Immutable
    private String phoneNumber_;
    private String password_;

    private static boolean checkPassword(String pass) {
        boolean chek = true;
        if (pass == null) {
            System.out.println("Password is null");
            chek = false;
        }

        pass = pass.trim();

        if (pass.isEmpty()) {
            System.out.println("Password is empty");
            chek = false;
        }

        if (pass.contains(" ")) {
            System.out.println("Password must not contain spaces");
            chek = false;
        }

        if (pass.length() < 8) {
            System.out.println("Password is too short (min 8)");
            chek = false;
        }

        if (pass.length() > 50) {
            System.out.println("Password is too long (max 50)");
            chek = false;
        }

        boolean hasUpper = false;
        boolean hasSpecial = false;

        for (int i = 0; i < pass.length(); i++) {
            char c = pass.charAt(i);
            if (Character.isUpperCase(c)) hasUpper = true;

            if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }

        if (!hasUpper) {
            System.out.println("Password must contain at least one uppercase letter");
            chek = false;
        }

        if (!hasSpecial) {
            System.out.println("Password must contain at least one special character");
            chek = false;
        }
        if(chek) System.out.println("Password is valid");
        return chek;
    }

    private static boolean checkPhoneNumber(String phN) {

        boolean check = true;

        if (phN == null) {
            System.out.println("Phone number is null");
            check = false;
        }

        phN = phN.trim();

        if (phN.isEmpty()) {
            System.out.println("Phone number is empty");
            check = false;
        }

        if (phN.contains(" ")) {
            System.out.println("Phone number must not contain spaces");
            check = false;
        }

        if (phN.length() != 11) {
            System.out.println("Phone number length must be 11");
            check = false;
        }

        boolean allDigits = true;

        for (int i = 0; i < phN.length(); i++) {
            if (!Character.isDigit(phN.charAt(i))) {
                allDigits = false;
                break;
            }
        }

        if (!allDigits) {
            System.out.println("Phone number must contain only digits");
            check = false;
        }

        if(phN.indexOf("09") != 0){
            System.out.println("Phone number must start with 09");
            check = false;
        }
        if(check) System.out.println("Phone number is valid");
        return check;
    }


    private static String getPhoneNumberFromInput(){
        Scanner input = new Scanner(System.in);
        String phoneNumber = input.nextLine();
        if (!checkPhoneNumber(phoneNumber)) {
            System.out.println("try again");
            phoneNumber = getPhoneNumberFromInput();
        }
        return phoneNumber;
    }



    private static String getPasswordFromInput(){
        Scanner input = new Scanner(System.in);
        String password = input.nextLine();
        /*if(password.equals("0")){
            System.out.println("operation canceled");
            return new String("");
        }*/
        if(!checkPassword(password)){
            System.out.println("try again");
            password = getPasswordFromInput();
        }
        return password;

    }

    private User(String phoneNumber, String password){
        this.phoneNumber_ = phoneNumber;
        this.password_ = password;
    }

    public static User userCreate(){
        System.out.println("Enter user phone number:");
        String phN = getPhoneNumberFromInput();
        System.out.println("Enter " + phN + " password:");
        String password = getPasswordFromInput();
        return new User(phN, password);
    }



    public String getPassword() {
        return password_;
    }

    public User setPassword() {
        System.out.println("Enter new password for " + phoneNumber_ + ":");
        String password = getPasswordFromInput();

        return this.setPassword(password);
    }

    private User setPassword(String password) {
        return new User(phoneNumber_, password);
    }



    public String getPhoneNumber() {
        return phoneNumber_;
    }

    public User setPhoneNumber(){
        System.out.print("Enter new user phone number for " + phoneNumber_ + ":");
        String phN = getPhoneNumberFromInput();
        return this.setPhoneNumber(phN);
    }

    private User setPhoneNumber(String phoneNumber) {
        return new User(phoneNumber, password_);
    }

}