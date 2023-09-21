package se.lth.base.server.user;

import com.google.gson.annotations.Expose;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.UUID;

/**
 * Used for authentication and user operations requiring passwords.
 *
 * @author Rasmus Ros, rasmus.ros@cs.lth.se
 */
public class Credentials {

    private final String username;
    // This annotation tells GSON that 'password' can only come from outside, it is never sent back.
    // It can't be retrieved in plain text from the database anyway. This is just an extra safe precaution.
    @Expose(serialize = false)
    private final String password;
    private final Role role;
    private final String first_name; 
    private final String last_name;
    private final String email; 
    private final String phone_number;

    /**
     * Login Constructor
     * @param username
     * @param password
     * @param role
     */
    public Credentials (String username, String password, Role role){
        this.username = username;
        this.password = password;
        this.role = role;
        this.first_name = ""; 
        this.last_name = ""; 
        this.email = ""; 
        this.phone_number = ""; 
    }

    /**
     * AddUser Constructor
     * @param username
     * @param password
     * @param role
     * @param first_name
     * @param last_name
     * @param email
     * @param phone_number
     */
    public Credentials(String username, String password, Role role, String first_name, 
                       String last_name, String email, String phone_number) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.first_name = first_name; 
        this.last_name = last_name; 
        this.email = email; 
        this.phone_number = phone_number; 
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public String getFirst_Name(){
        return first_name; 
    }

    public String getLast_Name(){
        return last_name; 
    }

    public String getPhone_Number(){
        return phone_number; 
    }

    public String getEmail(){
        return email; 
    }

    // Password hashing function parameters.
    private static final int SIZE = 256;
    private static final int ITERATION_COST = 16;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";

    public boolean validPassword() { 
        // TODO: Implement regex for password requirements
        return this.password.length() >= 8;
    }

    public boolean hasPassword() {
        return password != null;
    }

    /**
     * Hash password using hashing algorithm intended for this purpose.
     *
     * @return base64 encoded hash result.
     */
    UUID generatePasswordHash(long salt) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), ByteBuffer.allocate(8).putLong(salt).array(),
                    ITERATION_COST, SIZE);
            SecretKeyFactory f = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] blob = f.generateSecret(spec).getEncoded();
            LongBuffer lb = ByteBuffer.wrap(blob).asLongBuffer();
            return new UUID(lb.get(), lb.get());
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Missing algorithm: " + ALGORITHM, ex);
        } catch (InvalidKeySpecException ex) {
            throw new IllegalStateException("Invalid SecretKeyFactory", ex);
        }
    }

    static long generateSalt() {
        return new SecureRandom().nextLong();
    }

    public static void main(String[] args) {
        // This is left as is to show how the system was seeded. You can't login to the user admin interface and create
        // a new admin user if there are no users, so the first users are created manually and added to the database
        // schema.
        long s1 = generateSalt();
        long s2 = generateSalt();
        System.out.println(s1);
        System.out.println(new Credentials("Admin", "password", Role.ADMIN, "Admin", "Admin", "admin@admin.se", "+4600000000").generatePasswordHash(s1));

        System.out.println(s2);
        System.out.println(new Credentials("Test", "password", Role.USER, "User", "User", "user@user.se", "+4600000001").generatePasswordHash(s2));
    }
}