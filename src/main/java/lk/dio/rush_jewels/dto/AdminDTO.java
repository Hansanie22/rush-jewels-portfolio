package lk.dio.rush_jewels.dto;

import java.time.LocalDateTime;

public class AdminDTO {
    private int id;
    private String name;
    private String email;
    private String status;
    private int statusId;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private String role;
    private String imagePath;
    // Password is used only for creation, not fetching
    private String password;

    public AdminDTO(int id, String name, String email, String status, int statusId, LocalDateTime lastLogin, LocalDateTime createdAt, String role, String imagePath) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.status = status;
        this.statusId = statusId;
        this.lastLogin = lastLogin;
        this.createdAt = createdAt;
        this.role = role;
        this.imagePath = imagePath;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getStatusId() { return statusId; }
    public void setStatusId(int statusId) { this.statusId = statusId; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}