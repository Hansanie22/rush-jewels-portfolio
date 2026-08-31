package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "stock")
public class Stock implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int qty;

    @ManyToOne
    @JoinColumn(name = "stock_status_id", nullable = false)
    private StockStatus stockStatus;

    @ManyToOne
    @JoinColumn(name = "product_variance_id", nullable = true)
    private ProductVariance productVariance;

    @ManyToOne
    @JoinColumn(name = "collection_id", nullable = true)
    private Collection collection;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = true)   // ➕ Added warehouse relation
    private Warehouse warehouse;

    // 🧱 Constructors
    public Stock() {
    }

    public Stock(int id, int qty, StockStatus stockStatus, ProductVariance productVariance,
                 Collection collection, Warehouse warehouse) {
        this.id = id;
        this.qty = qty;
        this.stockStatus = stockStatus;
        this.productVariance = productVariance;
        this.collection = collection;
        this.warehouse = warehouse;
    }

    // 🧩 Getters and Setters
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

    public StockStatus getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(StockStatus stockStatus) {
        this.stockStatus = stockStatus;
    }

    public ProductVariance getProductVariance() {
        return productVariance;
    }

    public void setProductVariance(ProductVariance productVariance) {
        this.productVariance = productVariance;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }
}
