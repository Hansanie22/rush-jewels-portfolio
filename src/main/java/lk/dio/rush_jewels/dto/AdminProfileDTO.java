package lk.dio.rush_jewels.dto;

import java.time.LocalDateTime;

public class AdminProfileDTO {
    private int id;
    private String name;
    private String email;
    private String role; // e.g., "Super User" based on logic or DB
    private String lastLogin;

    public AdminProfileDTO(int id, String name, String email, String role, LocalDateTime lastLogin) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.lastLogin = lastLogin != null ? lastLogin.toString() : "First Login";
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getLastLogin() { return lastLogin; }
}