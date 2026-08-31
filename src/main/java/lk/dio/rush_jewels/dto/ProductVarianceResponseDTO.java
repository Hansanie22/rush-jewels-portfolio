package lk.dio.rush_jewels.dto;

public class ProductVarianceResponseDTO {
    private int id;
    private int productId;
    private String productName;
    private Integer sizeId;
    private String sizeName;
    private Integer colorId;
    private String colorName;
    private Integer gemstoneId;
    private String gemstoneName;
    private double price;
    private double regularPrice;
    private double discountPercentage;
    private int stockLimit;
    private int statusId;
    private String statusName;

    public ProductVarianceResponseDTO(int id, int productId, String productName,
                                      Integer sizeId, String sizeName,
                                      Integer colorId, String colorName,
                                      Integer gemstoneId, String gemstoneName,
                                      double price, double regularPrice, double discountPercentage,
                                      int stockLimit, int statusId, String statusName) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.sizeId = sizeId;
        this.sizeName = sizeName;
        this.colorId = colorId;
        this.colorName = colorName;
        this.gemstoneId = gemstoneId;
        this.gemstoneName = gemstoneName;
        this.price = price;
        this.regularPrice = regularPrice;
        this.discountPercentage = discountPercentage;
        this.stockLimit = stockLimit;
        this.statusId = statusId;
        this.statusName = statusName;
    }

    // Getters
    public int getId() { return id; }
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Integer getSizeId() { return sizeId; }
    public String getSizeName() { return sizeName; }
    public Integer getColorId() { return colorId; }
    public String getColorName() { return colorName; }
    public Integer getGemstoneId() { return gemstoneId; }
    public String getGemstoneName() { return gemstoneName; }
    public double getPrice() { return price; }
    public double getRegularPrice() { return regularPrice; }
    public double getDiscountPercentage() { return discountPercentage; }
    public int getStockLimit() { return stockLimit; }
    public int getStatusId() { return statusId; }
    public String getStatusName() { return statusName; }
}