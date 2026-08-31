package lk.dio.rush_jewels.model; // Updated package to match the project structure

import jakarta.persistence.*; // Changed imports from javax.persistence
import java.io.Serializable;

@Entity
@Table(name = "product_variance")
public class ProductVariance implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // Assumes Product is an entity

    @ManyToOne
    @JoinColumn(name = "size_id", nullable = true)
    private Size size; // Assumes Size is an entity

    @ManyToOne
    @JoinColumn(name = "color_id", nullable = true)
    private Color color; // Assumes Color is an entity

    @ManyToOne
    @JoinColumn(name = "gemstone_id", nullable = true)
    private Gemstone gemstone; // Assumes Gemstone is an entity

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    @Column(nullable = true)
    private Double price;

    @Column(name = "discount_percentage", nullable = true)
    private Double discountPercentage;

    @Column(name = "regular_price", nullable = false)
    private double regularPrice;

    @Column(name = "stock_limit", nullable = false)
    private int stockLimit;

    // 🧱 Constructors
    public ProductVariance() {
    }

    public ProductVariance(int id, Product product, Size size, Color color, Gemstone gemstone,
                           Double price, Double discountPercentage, double regularPrice, int stockLimit) {
        this.id = id;
        this.product = product;
        this.size = size;
        this.color = color;
        this.gemstone = gemstone;
        this.price = price;
        this.discountPercentage = discountPercentage;
        this.regularPrice = regularPrice;
        this.stockLimit = stockLimit;
    }

    // 🧩 Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Gemstone getGemstone() {
        return gemstone;
    }

    public void setGemstone(Gemstone gemstone) {
        this.gemstone = gemstone;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public double getRegularPrice() {
        return regularPrice;
    }

    public void setRegularPrice(double regularPrice) {
        this.regularPrice = regularPrice;
    }

    public int getStockLimit() {
        return stockLimit;
    }

    public void setStockLimit(int stockLimit) {
        this.stockLimit = stockLimit;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}