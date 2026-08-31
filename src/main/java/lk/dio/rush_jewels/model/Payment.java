package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "payment")
public class Payment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @ManyToOne
    @JoinColumn(name = "orders_id", nullable = false)
    private Orders orders;

    @Column(name = "transaction_id", length = 60, nullable = false)
    private String transactionId;

    @ManyToOne
    @JoinColumn(name = "payments_method_id", nullable = false)
    private PaymentMethod paymentsMethod;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "payment_status_id", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "sub_total", nullable = false)
    private Double subTotal;

    @Column(name = "final_total", nullable = false)
    private Double finalTotal;

    @Column(name = "tax", nullable = true) // Allows null
    private Double tax;

    @Column(name = "discount", nullable = true) // Allows null
    private Double discount;

    @Column(name = "tendered_amount", nullable = true)
    private Double tenderedAmount;

    @Column(name = "change_due", nullable = true)
    private Double changeDue;

    @Column(name = "completed_at", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date completedAt;

    // 🧱 Constructors
    public Payment() {
    }

    // Updated Constructor with Tax, Discount, Tendered, and Change
    public Payment(int id, Date createdAt, Orders orders, String transactionId,
                   PaymentMethod paymentsMethod, User user, PaymentStatus paymentStatus,
                   Double subTotal, Double finalTotal, Double tax, Double discount,
                   Double tenderedAmount, Double changeDue) {
        this.id = id;
        this.createdAt = createdAt;
        this.orders = orders;
        this.transactionId = transactionId;
        this.paymentsMethod = paymentsMethod;
        this.user = user;
        this.paymentStatus = paymentStatus;
        this.subTotal = subTotal;
        this.finalTotal = finalTotal;
        this.tax = tax;
        this.discount = discount;
        this.tenderedAmount = tenderedAmount;
        this.changeDue = changeDue;
    }

    // 🧩 Getters and Setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Orders getOrders() { return orders; }
    public void setOrders(Orders orders) { this.orders = orders; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public PaymentMethod getPaymentsMethod() { return paymentsMethod; }
    public void setPaymentsMethod(PaymentMethod paymentsMethod) { this.paymentsMethod = paymentsMethod; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public Double getSubTotal() { return subTotal; }
    public void setSubTotal(Double subTotal) { this.subTotal = subTotal; }

    public Double getFinalTotal() { return finalTotal; }
    public void setFinalTotal(Double finalTotal) { this.finalTotal = finalTotal; }

    // --- NEW GETTERS & SETTERS ---

    public Double getTax() {
        return tax;
    }

    public void setTax(Double tax) {
        this.tax = tax;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getTenderedAmount() {
        return tenderedAmount;
    }

    public void setTenderedAmount(Double tenderedAmount) {
        this.tenderedAmount = tenderedAmount;
    }

    public Double getChangeDue() {
        return changeDue;
    }

    public void setChangeDue(Double changeDue) {
        this.changeDue = changeDue;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }
}