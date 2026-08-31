package lk.dio.rush_jewels.model;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "color")
public class Color implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 45, nullable = false, unique = true)
    private String color;

    // --- Constructors ---
    public Color() {}

    public Color(Integer id, String color) {
        this.id = id;
        this.color = color;
    }

    // --- Getters and Setters ---
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
