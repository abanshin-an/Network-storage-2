package ru.gb.storage.commons.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;


public class PasswordUtils {
    private static final Logger log = LogManager.getLogger(PasswordUtils.class);

    // Define the BCrypt workload to use when generating password hashes. 10-31 is a valid value.
    private static final int WORKLOAD = 12;

    /**
     * This method can be used to generate a string representing an account password
     * suitable for storing in a database. It will be an OpenBSD-style crypt(3) formatted
     * hash string of length=60
     * The bcrypt workload is specified in the above static variable, a value from 10 to 31.
     * A workload of 12 is a very reasonable safe default as of 2013.
     * This automatically handles secure 128-bit salt generation and storage within the hash.
     *
     * @param passwordPlaintext The account's plaintext password as provided during account creation,
     *                          or when changing an account's password.
     * @return String - a string of length 60 that is the bcrypt hashed password in crypt(3) format.
     */
    public static String hashPassword(String passwordPlaintext) {
        String salt = BCrypt.gensalt(WORKLOAD);
        return BCrypt.hashpw(passwordPlaintext, salt);
    }

    /**
     * This method can be used to verify a computed hash from a plaintext (e.g. during a login
     * request) with that of a stored hash from a database. The password hash from the database
     * must be passed as the second variable.
     *
     * @param passwordPlaintext The account's plaintext password, as provided during a login request
     * @param storedHash        The account's stored password hash, retrieved from the authorization database
     * @return boolean - true if the password matches the password of the stored hash, false otherwise
     */
    public static boolean checkPassword(String passwordPlaintext, String storedHash) {
        if (null == storedHash || !storedHash.startsWith("$2a$"))
            throw new java.lang.IllegalArgumentException("Invalid hash provided for comparison");
        return BCrypt.checkpw(passwordPlaintext, storedHash);
    }

    /**
     * A simple test case for the main method, verify that a pre-generated test hash verifies successfully
     * for the password it represents, and also generate a new hash and ensure that the new hash verifies
     * just the same.
     */
    public static void main(String[] args) {
        String testPasswd = "abcdefghijklmnopqrstuvwxyz";
        String testHash = "$2a$06$.rCVZVOThsIa97pEDOxvGuRRgzG64bvtJ0938xuqzv18d3ZpQhstC";
        log.info("Testing BCrypt Password hashing and verification");
        log.info("Test password: {}", testPasswd);
        log.info("Test stored hash: {}", testHash);
        log.info("Hashing test password...");
        String computedHash = hashPassword(testPasswd);
        log.info("Test computed hash: {}", computedHash);
        log.info("Verifying that hash and stored hash both match for the test password...");
        String compareTest = checkPassword(testPasswd, testHash)
                ? "Passwords Match" : "Passwords do not match";
        String compareComputed = checkPassword(testPasswd, computedHash)
                ? "Passwords Match" : "Passwords do not match";
        log.info("Verify against stored hash:  {} ", compareTest);
        log.info("Verify against computed hash: {} ", compareComputed);
    }
}

