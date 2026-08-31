package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Entity
@Table(name = "blog_post_product")
@Getter
@Setter
public class BlogPostProduct implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Link to blog post
    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private BlogPost post;

    // Optional Product Variance
    @ManyToOne
    @JoinColumn(name = "product_variance_id")
    private ProductVariance productVariance;

    // Optional Collection
    @ManyToOne
    @JoinColumn(name = "collection_id")
    private Collection collection;

    public BlogPostProduct() {}

    public BlogPostProduct(
            BlogPost post,
            ProductVariance productVariance,
            Collection collection
    ) {
        this.post = post;
        this.productVariance = productVariance;
        this.collection = collection;
    }
}
