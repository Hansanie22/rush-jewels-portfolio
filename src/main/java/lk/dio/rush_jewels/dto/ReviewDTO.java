package lk.dio.rush_jewels.dto;

import java.time.OffsetDateTime;

public class ReviewDTO {
    private int id;
    private Integer varianceId;
    private String varianceName;
    private String customerName;
    private String customerEmail;
    private int rating;
    private String title;
    private String comment;
    private int statusId;
    private String statusName;
    private OffsetDateTime createdAt; // ✅ Changed to OffsetDateTime
    private Integer adminId;

    public ReviewDTO() {}

    public ReviewDTO(int id, Integer varianceId, String varianceName, String customerName,
                     String customerEmail, int rating, String title, String comment,
                     int statusId, String statusName, OffsetDateTime createdAt, Integer adminId) {
        this.id = id;
        this.varianceId = varianceId;
        this.varianceName = varianceName;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.rating = rating;
        this.title = title;
        this.comment = comment;
        this.statusId = statusId;
        this.statusName = statusName;
        this.createdAt = createdAt;
        this.adminId = adminId;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Integer getVarianceId() { return varianceId; }
    public void setVarianceId(Integer varianceId) { this.varianceId = varianceId; }
    public String getVarianceName() { return varianceName; }
    public void setVarianceName(String varianceName) { this.varianceName = varianceName; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public int getStatusId() { return statusId; }
    public void setStatusId(int statusId) { this.statusId = statusId; }
    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public Integer getAdminId() { return adminId; }
    public void setAdminId(Integer adminId) { this.adminId = adminId; }
}