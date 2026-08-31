package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "size")
public class Size implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 45, nullable = false)
    private String size;

    // 🧱 Add category relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // 🧱 Constructors
    public Size() {
    }

    public Size(int id, String size, Category category) {
        this.id = id;
        this.size = size;
        this.category = category;
    }

    // 🧩 Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
