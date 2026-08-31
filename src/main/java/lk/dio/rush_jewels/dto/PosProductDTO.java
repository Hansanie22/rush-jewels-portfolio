package lk.dio.rush_jewels.dto;

public class PosProductDTO {
    private String id;
    private String type; // "PRODUCT" or "COLLECTION"
    private String name;
    private String subtext; // e.g. "Size: 8, Length: 18inch"
    private double price;
    private int stockQty;
    private String barcode;
    private String imageUrl;
    private String category;

    public PosProductDTO() {}

    public PosProductDTO(String id, String type, String name, String subtext, double price, int stockQty, String barcode, String imageUrl, String category) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.subtext = subtext;
        this.price = price;
        this.stockQty = stockQty;
        this.barcode = barcode;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubtext() {
        return subtext;
    }

    public void setSubtext(String subtext) {
        this.subtext = subtext;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStockQty() {
        return stockQty;
    }

    public void setStockQty(int stockQty) {
        this.stockQty = stockQty;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
