/*
 * EE422C Final Project submission by
 * Desheng Liu
 * dl36526
 * 17150
 * Spring 2023
 * Slip Days Used: 1
 */

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;


public class User implements Serializable {

    private String username;
    private String hashedPassword;
    private String password;
    private String salt;
    private static int count = 0; // not thread safe
    private int id;

    public User(String username, String salt, String hashedPassword, int count){
        this.username = username;
        this.salt = salt;
        this.hashedPassword = hashedPassword;
        this.id = count;
    }
    public User(String username, String password, String makeaccountmethod){
        this.username = username;
        this.salt = generateSalt();
        this.hashedPassword = hashPassword(password, this.salt);
        this.id = updateCount();
        this.password = password;
    }
    public User(String username, String password){
        this.username = username;
        this.password = password;
    }
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(salt.getBytes());
            byte[] hashedBytes = messageDigest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {e.printStackTrace(); return null;}
    }
    public boolean checkPassword(String password) { // Method to check if a password matches the stored hashed password
        String hashedInputPassword = hashPassword(password, this.salt);
        return hashedInputPassword.equals(this.hashedPassword);
    }
    public String getUsername(){
        return username;
    }
    public String getHashedPassword(){
        return hashedPassword;
    }
    public String getSalt(){
        return salt;
    }
    public String getPassword(){
        return this.password;
    }
    public static int getId_Count(){
        return count;
    }
    private static synchronized int updateCount(){
        return count++;
    }
}
