//in the name of ALLAH
//YA MAHDI

package ir.sobhan.sobhaneh.common.user;

import java.util.Scanner;

public class UserService {

    public static UserDTO userCreate() {
        String phN = getPhoneNumberFromInput("Enter user phone number:");
        String password = getPasswordFromInput("Enter " + phN + " password:");
        return new UserDTO(phN, password);
    }

    private static String getPhoneNumberFromInput(String msg) {
        System.out.println(msg);
        Scanner input = new Scanner(System.in);
        String phoneNumber;
        do {
            try {
                phoneNumber = input.nextLine();
                checkPhoneNumber(phoneNumber);
                System.out.println("Your phone number is valid!");
                break;
            } catch (PhoneNumberException ex) {
                System.out.println(ex.getMessage());
                System.out.println("try again");
            }
        } while (true);
        return phoneNumber;
    }

    private static String getPasswordFromInput(String msg) {
        System.out.println(msg);
        Scanner input = new Scanner(System.in);

        String password;
        do {
            try {
                password = input.nextLine();
                checkPassword(password);
                System.out.println("Your phone number is valid!");
                break;
            } catch (PasswordException ex) {
                System.out.println(ex.getMessage());
                System.out.println("try again");
            }
        } while (true);
        return password;
    }

    private static void checkPassword(String pass) throws PasswordException {
        if (pass == null) throw new PasswordException("Password is null");
        pass = pass.trim();
        if (pass.isEmpty()) throw new PasswordException("Password is empty");
        if (pass.contains(" ")) throw new PasswordException("Password must not contain spaces");
        if (pass.length() < 8) throw new PasswordException("Password is too short (min 8)");
        if (pass.length() > 50) throw new PasswordException("Password is too long (max 50)");
        boolean hasUpper = false;
        boolean hasSpecial = false;
        for (int i = 0; i < pass.length(); i++) {
            char c = pass.charAt(i);
            if (Character.isUpperCase(c)) hasUpper = true;
            if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }
        if (!hasUpper) throw new PasswordException("Password must contain at least one uppercase letter");
        if (!hasSpecial) throw new PasswordException("Password must contain at least one special character");
    }

    private static void checkPhoneNumber(String phN) throws PhoneNumberException {

        if (phN == null) {
            throw new PhoneNumberException("Phone number is null");
        }

        phN = phN.trim();

        if (phN.isEmpty()) {
            throw new PhoneNumberException("Phone number is empty");
        }

        if (phN.contains(" ")) {
            throw new PhoneNumberException("Phone number must not contain spaces");
        }

        if (phN.length() != 11) {
            throw new PhoneNumberException("Phone number length must be 11");
        }

        boolean allDigits = true;

        for (int i = 0; i < phN.length(); i++) {

            if (!Character.isDigit(phN.charAt(i))) {
                allDigits = false;
                break;
            }
        }

        if (!allDigits) {
            throw new PhoneNumberException("Phone number must contain only digits");
        }

        if (phN.indexOf("09") != 0) {
            throw new PhoneNumberException("Phone number must start with 09");
        }
    }
}


class PhoneNumberException extends Exception {
    public PhoneNumberException(String message) {
        super(message);
    }
}

class PasswordException extends Exception {
    public PasswordException(String message) {
        super(message);
    }
}