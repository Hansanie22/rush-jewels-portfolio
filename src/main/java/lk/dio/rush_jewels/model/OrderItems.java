package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "order_items")
public class OrderItems implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int qty;

    @ManyToOne
    @JoinColumn(name = "orders_id", nullable = false)
    private Orders orders;

    @ManyToOne
    @JoinColumn(name = "collection_id", nullable = true) // Nullable collection
    private Collection collection;

    @ManyToOne
    @JoinColumn(name = "product_variance_id", nullable = true)
    private ProductVariance productVariance;

    // 🧱 Constructors
    public OrderItems() {
    }

    public OrderItems(int id, int qty, Orders orders, Collection collection, ProductVariance productVariance) {
        this.id = id;
        this.qty = qty;
        this.orders = orders;
        this.collection = collection;
        this.productVariance = productVariance;
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

    public Orders getOrders() {
        return orders;
    }

    public void setOrders(Orders orders) {
        this.orders = orders;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public ProductVariance getProductVariance() {
        return productVariance;
    }

    public void setProductVariance(ProductVariance productVariance) {
        this.productVariance = productVariance;
    }
}
