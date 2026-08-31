package lk.dio.rush_jewels.dto;

public class StockAlertDTO {
    private String productName;
    private int qty;
    private String status; // "Low Stock" or "Out of Stock"
    private String type;   // "Variance" or "Collection"
    private String warehouse; // ✅ New Field

    public StockAlertDTO(String productName, int qty, String status, String type, String warehouse) {
        this.productName = productName;
        this.qty = qty;
        this.status = status;
        this.type = type;
        this.warehouse = warehouse;
    }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }
}