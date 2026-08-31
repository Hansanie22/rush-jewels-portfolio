package lk.dio.rush_jewels.model; // Updated package to match the project structure

import jakarta.persistence.*; // Changed imports from javax.persistence
import java.io.Serializable;

@Entity
@Table(name = "payment_status")
public class PaymentStatus implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "payment_status", length = 45, nullable = false)
    private String paymentStatus;

    // 🧱 Constructors
    public PaymentStatus() {
    }

    public PaymentStatus(int id, String paymentStatus) {
        this.id = id;
        this.paymentStatus = paymentStatus;
    }

    // 🧩 Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}