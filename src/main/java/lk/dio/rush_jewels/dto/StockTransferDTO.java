package lk.dio.rush_jewels.dto;

public class StockTransferDTO {
    private int varianceId; // Only variances based on your entity
    private int fromWarehouseId;
    private int toWarehouseId;
    private int quantity;

    // Getters and Setters
    public int getVarianceId() { return varianceId; }
    public void setVarianceId(int varianceId) { this.varianceId = varianceId; }
    public int getFromWarehouseId() { return fromWarehouseId; }
    public void setFromWarehouseId(int fromWarehouseId) { this.fromWarehouseId = fromWarehouseId; }
    public int getToWarehouseId() { return toWarehouseId; }
    public void setToWarehouseId(int toWarehouseId) { this.toWarehouseId = toWarehouseId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}