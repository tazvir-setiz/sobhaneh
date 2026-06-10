//in the name of ALLAH
//YA MAHDI

package ir.sobhaneh.user;

import java.util.Scanner;

public class UserService {

    public static UserDTO userCreate() {

        System.out.println("Enter user phone number:");
        String phN = getPhoneNumberFromInput();

        System.out.println("Enter " + phN + " password:");
        String password = getPasswordFromInput();

        return new UserDTO(phN, password);
    }

    public static UserDTO setPassword(UserDTO user) {

        System.out.println("Enter new password for " + user.getPhoneNumber() + ":");

        String password = getPasswordFromInput();

        return new UserDTO(user.getPhoneNumber(), password);
    }

    public static UserDTO setPhoneNumber(UserDTO user) {

        System.out.println("Enter new phone number for " + user.getPhoneNumber() + ":");

        String phoneNumber = getPhoneNumberFromInput();

        return new UserDTO(phoneNumber, user.getPassword());
    }

    private static String getPhoneNumberFromInput() {
        Scanner input = new Scanner(System.in);

        String phoneNumber = input.nextLine();

        if (!checkPhoneNumber(phoneNumber)) {
            System.out.println("try again");
            return getPhoneNumberFromInput();
        }

        return phoneNumber;
    }

    private static String getPasswordFromInput() {
        Scanner input = new Scanner(System.in);

        String password = input.nextLine();

        if (!checkPassword(password)) {
            System.out.println("try again");
            return getPasswordFromInput();
        }

        return password;
    }

    private static boolean checkPassword(String pass) {

        boolean check = true;

        if (pass == null) {
            System.out.println("Password is null");
            return false;
        }

        pass = pass.trim();

        if (pass.isEmpty()) {
            System.out.println("Password is empty");
            check = false;
        }

        if (pass.contains(" ")) {
            System.out.println("Password must not contain spaces");
            check = false;
        }

        if (pass.length() < 8) {
            System.out.println("Password is too short (min 8)");
            check = false;
        }

        if (pass.length() > 50) {
            System.out.println("Password is too long (max 50)");
            check = false;
        }

        boolean hasUpper = false;
        boolean hasSpecial = false;

        for (int i = 0; i < pass.length(); i++) {

            char c = pass.charAt(i);

            if (Character.isUpperCase(c))
                hasUpper = true;

            if (!Character.isLetterOrDigit(c))
                hasSpecial = true;
        }

        if (!hasUpper) {
            System.out.println(
                    "Password must contain at least one uppercase letter");
            check = false;
        }

        if (!hasSpecial) {
            System.out.println(
                    "Password must contain at least one special character");
            check = false;
        }

        if (check)
            System.out.println("Password is valid");

        return check;
    }

    private static boolean checkPhoneNumber(String phN) {

        boolean check = true;

        if (phN == null) {
            System.out.println("Phone number is null");
            return false;
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

        if (phN.indexOf("09") != 0) {
            System.out.println("Phone number must start with 09");
            check = false;
        }

        if (check)
            System.out.println("Phone number is valid");

        return check;
    }
}