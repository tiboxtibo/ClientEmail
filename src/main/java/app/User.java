package app;

import java.io.Serializable;

/**
 * The user object.
 */
public class User implements Serializable {

    private int id;
    private String email;
    private String password;

    public User(int id, String email, String password) {
        this.id = id;
        this.email = email;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String toStringUser() {
        return id + "," + email + "," + password + ";";
    }

    public String userPathFile() {
        return id + "-" + email + ".txt";
    }

    public String userFolder() {
        return "src/main/java/mails/" + id;
    }


}
