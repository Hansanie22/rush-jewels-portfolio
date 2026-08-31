package lk.dio.rush_jewels.dto;

public class CollectionRequestDTO {
    private String name;
    private String title;
    private String description;
    private String specifications;
    private String warranty;
    private double regularPrice;
    private Double discountPercentage;
    private int stockLimit;
    private String material;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
    public String getWarranty() { return warranty; }
    public void setWarranty(String warranty) { this.warranty = warranty; }
    public double getRegularPrice() { return regularPrice; }
    public void setRegularPrice(double regularPrice) { this.regularPrice = regularPrice; }
    public Double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Double discountPercentage) { this.discountPercentage = discountPercentage; }
    public int getStockLimit() { return stockLimit; }
    public void setStockLimit(int stockLimit) { this.stockLimit = stockLimit; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
}