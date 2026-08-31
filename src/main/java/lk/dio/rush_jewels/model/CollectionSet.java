package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "collection_set")
public class CollectionSet implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int qty;

    @ManyToOne
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection; // Assumes Collection is an entity

    @ManyToOne
    @JoinColumn(name = "product_variance_id", nullable = false)
    private ProductVariance productVariance; // Assumes ProductVariance is an entity

    // 🧱 Constructors
    public CollectionSet() {
    }

    public CollectionSet(int id, int qty, Collection collection, ProductVariance productVariance) {
        this.id = id;
        this.qty = qty;
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
