package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "category")
public class Category implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 45, nullable = false)
    private String category;

    @Column(name = "status_id", nullable = false, columnDefinition = "INT DEFAULT 1")
    private int statusId = 1;

    public Category() {}

    public Category(int id, String category, int statusId) {
        this.id = id;
        this.category = category;
        this.statusId = statusId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }
}
