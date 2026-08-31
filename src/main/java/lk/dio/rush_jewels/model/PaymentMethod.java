package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "payments_method")
public class PaymentMethod implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 45, nullable = false)
    private String method;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true; // Default active

    public PaymentMethod() {}

    public PaymentMethod(String method, boolean isActive) {
        this.method = method;
        this.isActive = isActive;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}