// ProductDTO.java
package lk.dio.rush_jewels.dto;

import java.util.Date;
import java.util.List;

public class ProductDTO {
    // Variance-specific ID
    private int varianceId;

    // Product-specific attributes
    private int productId;
    private String name;
    private String title;
    private String description;
    private String category;
    private Date createdAt;

    // Pricing & Stock
    private double regularPrice;
    private double price;
    private double discountPercentage;
    private int stockLimit;
    private String stockStatus;
    private int currentStockQty; // Available Stock

    // Variance attributes
    private String size;
    private String color;
    private String gemstone;

    // Frontend display attributes
    private String image;
    private List<String> tags;

    private Double averageRating;
    private int reviewCount;

    // Getters and Setters (Omitted for brevity, but required for Gson/Jackson serialization)

    // Example Getter/Setter:
    public int getVarianceId() { return varianceId; }
    public void setVarianceId(int varianceId) { this.varianceId = varianceId; }
    // ... all other getters and setters ...

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getGemstone() {
        return gemstone;
    }

    public void setGemstone(String gemstone) {
        this.gemstone = gemstone;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getCurrentStockQty() {
        return currentStockQty;
    }

    public void setCurrentStockQty(int currentStockQty) {
        this.currentStockQty = currentStockQty;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }

    public int getStockLimit() {
        return stockLimit;
    }

    public void setStockLimit(int stockLimit) {
        this.stockLimit = stockLimit;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getRegularPrice() {
        return regularPrice;
    }

    public void setRegularPrice(double regularPrice) {
        this.regularPrice = regularPrice;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getAverageRating() {
        return averageRating != null ? averageRating : 0.0;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }
}