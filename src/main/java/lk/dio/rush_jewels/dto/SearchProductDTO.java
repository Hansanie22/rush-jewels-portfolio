package lk.dio.rush_jewels.dto;

import java.io.Serializable;

public class SearchProductDTO implements Serializable {
    private Integer varianceId; // Used for Product Variance ID
    private Integer productId;  // Used for Parent Product ID or Collection ID
    private String name;
    private String title;
    private String description;
    private Double price;
    private Double regularPrice;
    private Double discountPercentage;
    private Integer stockQty;
    private Integer stockLimit;

    // Meta fields
    private String size;
    private String color;
    private String gemstone;
    private String category;

    private String image;

    // NEW FIELD
    private String type; // "PRODUCT" or "COLLECTION"

    // Getters and Setters
    public Integer getVarianceId() { return varianceId; }
    public void setVarianceId(Integer varianceId) { this.varianceId = varianceId; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Double getRegularPrice() { return regularPrice; }
    public void setRegularPrice(Double regularPrice) { this.regularPrice = regularPrice; }
    public Double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Double discountPercentage) { this.discountPercentage = discountPercentage; }
    public Integer getStockQty() { return stockQty; }
    public void setStockQty(Integer stockQty) { this.stockQty = stockQty; }
    public Integer getStockLimit() { return stockLimit; }
    public void setStockLimit(Integer stockLimit) { this.stockLimit = stockLimit; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getGemstone() { return gemstone; }
    public void setGemstone(String gemstone) { this.gemstone = gemstone; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}