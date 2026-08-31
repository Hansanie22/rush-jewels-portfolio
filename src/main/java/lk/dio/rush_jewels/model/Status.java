package lk.dio.rush_jewels.model; // Updated package to match the project structure

import jakarta.persistence.*; // Changed imports from javax.persistence
import java.io.Serializable;

@Entity
@Table(name = "status")
public class Status implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 45, nullable = false)
    private String status;

    // 🧱 Constructors
    public Status() {
    }

    public Status(int id, String status) {
        this.id = id;
        this.status = status;
    }

    // 🧩 Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}