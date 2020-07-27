package pingidsdk.pingidentity.com.simpledemo.models;

/**
 * Created by Ping Identity on 11/6/18.
 */

public class User {
    private String username;
    private String firstname;
    private String lastname;
    private String status;

    public User(String username, String status){
        this.username = username;
        this.status = status;
    }

    public User(String username, String firstname, String status){
        this.username = username;
        this.firstname = firstname;
        this.status = status;
    }

    public User(String username, String firstname, String lastname, String status){
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
