package lk.dio.rush_jewels.dto;

import java.time.OffsetDateTime;

public class ReviewsDTO {
    private int id;
    private int rating;
    private String comment;
    private String reviewerName;
    private String reviewerType; // "Customer" or "Admin"
    private String profileImagePath; // URL or relative path to image
    private OffsetDateTime approvedAt;

    // Constructors (required for easy mapping/creation)
    public ReviewsDTO() {}

    public ReviewsDTO(int id, int rating, String comment, String reviewerName, String reviewerType, String profileImagePath, OffsetDateTime approvedAt) {
        this.id = id;
        this.rating = rating;
        this.comment = comment;
        this.reviewerName = reviewerName;
        this.reviewerType = reviewerType;
        this.profileImagePath = profileImagePath;
        this.approvedAt = approvedAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
    public String getReviewerType() { return reviewerType; }
    public void setReviewerType(String reviewerType) { this.reviewerType = reviewerType; }
    public String getProfileImagePath() { return profileImagePath; }
    public void setProfileImagePath(String profileImagePath) { this.profileImagePath = profileImagePath; }
    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(OffsetDateTime approvedAt) { this.approvedAt = approvedAt; }
}