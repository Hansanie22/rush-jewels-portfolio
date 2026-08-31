package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "cart")
public class Cart implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int qty;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_variance_id", nullable = true)
    private ProductVariance productVariance;

    @ManyToOne
    @JoinColumn(name = "collection_id", nullable = true) // Nullable collection
    private Collection collection;

    // 🧱 Constructors
    public Cart() {
    }

    public Cart(int id, int qty, User user, ProductVariance productVariance, Collection collection) {
        this.id = id;
        this.qty = qty;
        this.user = user;
        this.productVariance = productVariance;
        this.collection = collection;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
}
