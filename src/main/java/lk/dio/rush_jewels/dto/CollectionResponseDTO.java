package lk.dio.rush_jewels.dto;

public class CollectionResponseDTO {
    private int id;
    private String name;
    private String title;
    private String description;
    private String specifications;
    private String warranty;
    private String material; // ✅ Added field
    private double price;
    private double regularPrice;
    private double discountPercentage;
    private int stockLimit;
    private int statusId;
    private String statusName;
    private String image;

    // ✅ Updated Constructor
    public CollectionResponseDTO(int id, String name, String title, String description,
                                 String specifications, String warranty, String material,
                                 double price, double regularPrice, double discountPercentage,
                                 int stockLimit, int statusId, String statusName, String image) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.description = description;
        this.specifications = specifications;
        this.warranty = warranty;
        this.material = material;
        this.price = price;
        this.regularPrice = regularPrice;
        this.discountPercentage = discountPercentage;
        this.stockLimit = stockLimit;
        this.statusId = statusId;
        this.statusName = statusName;
        this.image = image;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getSpecifications() { return specifications; }
    public String getWarranty() { return warranty; }
    public String getMaterial() { return material; } // ✅ Added getter
    public double getPrice() { return price; }
    public double getRegularPrice() { return regularPrice; }
    public double getDiscountPercentage() { return discountPercentage; }
    public int getStockLimit() { return stockLimit; }
    public int getStatusId() { return statusId; }
    public String getStatusName() { return statusName; }
    public String getImage() { return image; }
}