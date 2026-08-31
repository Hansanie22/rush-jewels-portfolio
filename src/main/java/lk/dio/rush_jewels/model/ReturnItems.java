package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "return_items")
public class ReturnItems implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private Integer qty;

    // ---------------------------
    // Relationship to Return Entity
    // ---------------------------
    @ManyToOne
    // "return" is a keyword, so we name the variable 'returns'
    @JoinColumn(name = "return_id", nullable = false)
    private Return returns;

    // ---------------------------
    // Relationship to OrderItems Entity
    // ---------------------------
    @ManyToOne
    @JoinColumn(name = "order_items_id", nullable = false)
    private OrderItems orderItems;

    // 🧱 Constructors
    public ReturnItems() {
    }

    public ReturnItems(int id, Integer qty, Return returns, OrderItems orderItems) {
        this.id = id;
        this.qty = qty;
        this.returns = returns;
        this.orderItems = orderItems;
    }

    // 🧩 Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public Return getReturns() {
        return returns;
    }

    public void setReturns(Return returns) {
        this.returns = returns;
    }

    public OrderItems getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(OrderItems orderItems) {
        this.orderItems = orderItems;
    }
}