package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "review_status")
public class ReviewStatus implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "review_status", nullable = false, length = 45)
    private String reviewStatus; // "Pending", "Approved", "Rejected"

    public ReviewStatus() {}
    public ReviewStatus(int id) { this.id = id; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
}