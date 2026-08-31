package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_adjustment")
public class StockAdjustment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "adjustment_type", length = 20, nullable = false)
    private String adjustmentType; // "ADD", "REMOVE", "SET"

    @Column(nullable = false)
    private int quantity_change; // The amount changed

    @Column(name = "previous_qty")
    private int previousQty;

    @Column(name = "new_qty")
    private int newQty;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "adjusted_at")
    private LocalDateTime adjustedAt;

    @PrePersist
    protected void onCreate() {
        this.adjustedAt = LocalDateTime.now();
    }

    // Constructors
    public StockAdjustment() {}

    public StockAdjustment(Stock stock, String adjustmentType, int quantity_change, int previousQty, int newQty, String reason) {
        this.stock = stock;
        this.adjustmentType = adjustmentType;
        this.quantity_change = quantity_change;
        this.previousQty = previousQty;
        this.newQty = newQty;
        this.reason = reason;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Stock getStock() { return stock; }
    public void setStock(Stock stock) { this.stock = stock; }
    public String getAdjustmentType() { return adjustmentType; }
    public void setAdjustmentType(String adjustmentType) { this.adjustmentType = adjustmentType; }
    public int getQuantity_change() { return quantity_change; }
    public void setQuantity_change(int quantity_change) { this.quantity_change = quantity_change; }
    public int getPreviousQty() { return previousQty; }
    public void setPreviousQty(int previousQty) { this.previousQty = previousQty; }
    public int getNewQty() { return newQty; }
    public void setNewQty(int newQty) { this.newQty = newQty; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getAdjustedAt() { return adjustedAt; }
    public void setAdjustedAt(LocalDateTime adjustedAt) { this.adjustedAt = adjustedAt; }
}