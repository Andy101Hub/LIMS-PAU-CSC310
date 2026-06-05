package lims.utils;

import org.mindrot.jbcrypt.BCrypt;

public class BCryptUtil {

    // This method takes a plain password and converts it into a secure hashed password
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    // This method checks if the plain password entered by the user matches the hashed password in the database
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    // Temporary main method for testing this file directly
    public static void main(String[] args) {
        String plainPassword = "staff123";

        String hashedPassword = hashPassword(plainPassword);

        System.out.println("Plain Password: " + plainPassword);
        System.out.println("Hashed Password: " + hashedPassword);

        boolean isMatch = checkPassword("admin123", hashedPassword);

        if (isMatch) {
            System.out.println("Password matched successfully!");
        } else {
            System.out.println("Password does not match!");
        }
    }
}