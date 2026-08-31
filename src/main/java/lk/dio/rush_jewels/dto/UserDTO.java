package lk.dio.rush_jewels.dto;

import lk.dio.rush_jewels.model.User;
import java.io.Serializable;
import java.util.Date;

/**
 * Data Transfer Object for User entity
 * Prevents lazy loading issues when serializing to JSON
 */
public class UserDTO implements Serializable {

    private int id;
    private String fname;
    private String lname;
    private String email;
    private Date createdAt;
    private String loginProvider;
    private boolean subscribed;
    private String statusName;
    private String type;

    public UserDTO() {}

    // Constructor from User entity
    public UserDTO(User user) {
        this.id = user.getId();
        this.fname = user.getFname();
        this.lname = user.getLname();
        this.email = user.getEmail();
        this.createdAt = user.getCreatedAt();
        this.loginProvider = user.getLoginProvider();
        this.subscribed = user.isSubscribed();
        this.statusName = user.getStatus() != null ? user.getStatus().getStatus() : null;
        this.type = user.getType();
    }

    // Static factory method for easy conversion
    public static UserDTO from(User user) {
        return new UserDTO(user);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getLoginProvider() {
        return loginProvider;
    }

    public void setLoginProvider(String loginProvider) {
        this.loginProvider = loginProvider;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}