package lk.dio.rush_jewels.model; // Changed package to match the desired structure

import jakarta.persistence.*; // Updated imports from javax.persistence
import java.io.Serializable;

@Entity
@Table(name = "order_status")
public class OrderStatus implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "order_status", length = 45, nullable = false)
    private String orderStatus;

    // 🧱 Constructors
    public OrderStatus() {
    }

    public OrderStatus(int id, String orderStatus) {
        this.id = id;
        this.orderStatus = orderStatus;
    }

    // 🧩 Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}