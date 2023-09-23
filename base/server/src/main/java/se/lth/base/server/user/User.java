package se.lth.base.server.user;

import java.security.Principal;

public class User implements Principal {

    public static User NONE = new User(0, Role.NONE, "-", "-", "-", "-", "-");

    private final int id; // User has ID instead of password - difference between this and Credentials
    private final Role role;
    private final String username;
    private final String first_name;
    private final String last_name;
    private final String email;
    private final String phone_number;

    public User(int id, Role role, String username, String first_name, String last_name, String email,
            String phone_number) {
        this.id = id;
        this.role = role;
        this.username = username;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.phone_number = phone_number;
    }

    public Role getRole() {
        return role;
    }

    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return username;
    }

    public String getFirst_Name() {
        return first_name;
    }

    public String getLast_Name() {
        return last_name;
    }

    public String getPhone_Number() {
        return phone_number;
    }

    public String getEmail() {
        return email;
    }
}
