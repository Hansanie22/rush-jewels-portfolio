package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "collection")
public class Collection implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 45)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "price",nullable = true)
    private double price;

    @Column(name = "discount_percentage",nullable = true)
    private double discountPercentage;

    @Column(name = "regular_price")
    private double regularPrice;

    @Column(name = "stock_limit")
    private int stockLimit;

    @Column(length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String specifications;

    @Column(length = 45)
    private String warranty;

    @Column(length = 45, nullable = true)
    private String material;

    @Column(name = "image1", length = 500) // ලින්ක් එක දිග නිසා 500 දැම්මා
    private String image1;

    @Column(name = "image2", length = 500)
    private String image2;

    @Column(name = "image3", length = 500)
    private String image3;

    @Column(name = "image4", length = 500)
    private String image4;

    public String getImage1() {
        return image1;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public String getImage2() {
        return image2;
    }

    public void setImage2(String image2) {
        this.image2 = image2;
    }

    public String getImage3() {
        return image3;
    }

    public void setImage3(String image3) {
        this.image3 = image3;
    }

    public String getImage4() {
        return image4;
    }

    public void setImage4(String image4) {
        this.image4 = image4;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Column(length = 45)
    private String type;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private Status status;  // Assuming Status is an entity


    // 🧱 Constructors
    public Collection() {
    }

    public Collection(int id, String name, String description, double price,
                      double discountPercentage, double regularPrice, int stockLimit,
                      String title, String specifications, String warranty,
                      LocalDateTime createdAt, Status status, String material) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.discountPercentage = discountPercentage;
        this.regularPrice = regularPrice;
        this.stockLimit = stockLimit;
        this.title = title;
        this.specifications = specifications;
        this.warranty = warranty;
        this.createdAt = createdAt;
        this.status = status;
        this.material = material;
    }


    // 🧩 Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public String getWarranty() {
        return warranty;
    }

    public void setWarranty(String warranty) {
        this.warranty = warranty;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

}
