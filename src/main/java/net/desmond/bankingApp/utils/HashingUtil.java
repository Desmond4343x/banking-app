package net.desmond.bankingApp.utils;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class HashingUtil {

    //hashing using BCrypt
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt()); // Automatically generates a secure salt
    }

    public static boolean verifyPassword(String enteredPassword, String storedHash) {
        return BCrypt.checkpw(enteredPassword, storedHash);
    }
}
