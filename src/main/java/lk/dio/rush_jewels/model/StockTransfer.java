package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "stock_transfer")
public class StockTransfer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int qty;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "transferred_at", nullable = false)
    private Date transferredAt;

    // ---------------------------
    //   RELATIONSHIPS
    // ---------------------------

    @ManyToOne
    @JoinColumn(name = "from_warehouse_id", nullable = false)
    private Warehouse fromWarehouse;

    @ManyToOne
    @JoinColumn(name = "to_warehouse_id", nullable = false)
    private Warehouse toWarehouse;

    @ManyToOne
    @JoinColumn(name = "product_variance_id", nullable = false)
    private ProductVariance productVariance;

    // ---------------------------
    //   CONSTRUCTORS
    // ---------------------------

    public StockTransfer() {}

    public StockTransfer(int id, int qty, Date transferredAt,
                         Warehouse fromWarehouse, Warehouse toWarehouse,
                         ProductVariance productVariance) {
        this.id = id;
        this.qty = qty;
        this.transferredAt = transferredAt;
        this.fromWarehouse = fromWarehouse;
        this.toWarehouse = toWarehouse;
        this.productVariance = productVariance;
    }

    // ---------------------------
    //   GETTERS & SETTERS
    // ---------------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public Date getTransferredAt() {
        return transferredAt;
    }

    public void setTransferredAt(Date transferredAt) {
        this.transferredAt = transferredAt;
    }

    public Warehouse getFromWarehouse() {
        return fromWarehouse;
    }

    public void setFromWarehouse(Warehouse fromWarehouse) {
        this.fromWarehouse = fromWarehouse;
    }

    public Warehouse getToWarehouse() {
        return toWarehouse;
    }

    public void setToWarehouse(Warehouse toWarehouse) {
        this.toWarehouse = toWarehouse;
    }

    public ProductVariance getProductVariance() {
        return productVariance;
    }

    public void setProductVariance(ProductVariance productVariance) {
        this.productVariance = productVariance;
    }
}
