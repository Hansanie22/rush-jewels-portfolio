package lk.dio.rush_jewels.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CollectionDTO {

    private int id;
    private String name;
    private String description;
    private double price;
    private double discountPercentage;
    private double regularPrice;
    private int stockLimit;
    private String title;
    private String specifications;
    private String warranty;
    private LocalDateTime createdAt;
    private String status;
    private String image; // Main image
    private List<String> images; // All gallery images
    private String material;
    private List<CollectionItemDTO> collectionItems;

    private Double averageRating;
    private int reviewCount;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }
    public double getRegularPrice() { return regularPrice; }
    public void setRegularPrice(double regularPrice) { this.regularPrice = regularPrice; }
    public int getStockLimit() { return stockLimit; }
    public void setStockLimit(int stockLimit) { this.stockLimit = stockLimit; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
    public String getWarranty() { return warranty; }
    public void setWarranty(String warranty) { this.warranty = warranty; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    public List<CollectionItemDTO> getCollectionItems() { return collectionItems; }
    public void setCollectionItems(List<CollectionItemDTO> collectionItems) { this.collectionItems = collectionItems; }

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


    public static class CollectionItemDTO {
        private String productTitle;
        private String variantName;
        private String productSubtext;
        private int qty;
        private String image;

        public CollectionItemDTO(String productTitle, String variantName, String productSubtext, int qty, String image) {
            this.productTitle = productTitle;
            this.variantName = variantName;
            this.productSubtext = productSubtext;
            this.qty = qty;
            this.image = image;
        }

        public String getProductTitle() { return productTitle; }
        public void setProductTitle(String productTitle) { this.productTitle = productTitle; }
        public String getVariantName() { return variantName; }
        public void setVariantName(String variantName) { this.variantName = variantName; }
        public String getProductSubtext() { return productSubtext; }
        public void setProductSubtext(String productSubtext) { this.productSubtext = productSubtext; }
        public int getQty() { return qty; }
        public void setQty(int qty) { this.qty = qty; }
        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
    }
}