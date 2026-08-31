package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "shipping")
public class Shipping implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "shipping_method", length = 45, nullable = false)
    private String shippingMethod;

    @Column(name = "value", nullable = false)
    private Double value;

    @Column(name = "description", length = 45, nullable = false)
    private String description;

    // Status: 1 = Active, 0 = Deleted
    @Column(name = "status", nullable = false, columnDefinition = "INT DEFAULT 1")
    private int status = 1;

    // 🧱 Constructors
    public Shipping() {
    }

    public Shipping(int id, String shippingMethod, Double value, String description, int status) {
        this.id = id;
        this.shippingMethod = shippingMethod;
        this.value = value;
        this.description = description;
        this.status = status;
    }

    // 🧩 Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}