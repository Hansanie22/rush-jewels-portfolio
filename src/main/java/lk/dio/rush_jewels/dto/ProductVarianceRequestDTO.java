package lk.dio.rush_jewels.dto;

public class ProductVarianceRequestDTO {
    private int productId;
    private Integer sizeId;      // Nullable
    private Integer colorId;     // Nullable
    private Integer gemstoneId;  // Nullable
    private double regularPrice;
    private Double discountPercentage;
    private int stockLimit;

    // Getters and Setters
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public Integer getSizeId() { return sizeId; }
    public void setSizeId(Integer sizeId) { this.sizeId = sizeId; }
    public Integer getColorId() { return colorId; }
    public void setColorId(Integer colorId) { this.colorId = colorId; }
    public Integer getGemstoneId() { return gemstoneId; }
    public void setGemstoneId(Integer gemstoneId) { this.gemstoneId = gemstoneId; }
    public double getRegularPrice() { return regularPrice; }
    public void setRegularPrice(double regularPrice) { this.regularPrice = regularPrice; }
    public Double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Double discountPercentage) { this.discountPercentage = discountPercentage; }
    public int getStockLimit() { return stockLimit; }
    public void setStockLimit(int stockLimit) { this.stockLimit = stockLimit; }
}