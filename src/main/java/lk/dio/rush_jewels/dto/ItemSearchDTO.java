package lk.dio.rush_jewels.dto;

public class ItemSearchDTO {
    private int id; // ID of ProductVariance or Collection
    private String name;
    private String image; // Full URL
    private String sku; // or Code
    private String type; // "PRODUCT" or "COLLECTION"

    public ItemSearchDTO() {
    }

    public ItemSearchDTO(int id, String name, String image, String sku, String type) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.sku = sku;
        this.type = type;
    }

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}