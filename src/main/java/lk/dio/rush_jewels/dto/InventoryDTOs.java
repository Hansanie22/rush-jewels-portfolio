package lk.dio.rush_jewels.dto;

public class InventoryDTOs {

    // 1. For displaying in the table
    public static class InventoryItemDTO {
        private int stockId;
        private String type;
        private int itemId;
        private String itemName;
        private String sku;
        private String category;
        private int warehouseId; // ✅ Added
        private String warehouseName;
        private int currentStock;
        private int minStockLimit;
        private String status;
        private double unitPrice;
        private double totalValue;

        public InventoryItemDTO(int stockId, String type, int itemId, String itemName, String sku,
                                String category, int warehouseId, String warehouseName, int currentStock, int minStockLimit,
                                String status, double unitPrice) {
            this.stockId = stockId;
            this.type = type;
            this.itemId = itemId;
            this.itemName = itemName;
            this.sku = sku;
            this.category = category;
            this.warehouseId = warehouseId; // ✅ Set
            this.warehouseName = warehouseName;
            this.currentStock = currentStock;
            this.minStockLimit = minStockLimit;
            this.status = status;
            this.unitPrice = unitPrice;
            this.totalValue = currentStock * unitPrice;
        }

        // Getters
        public int getStockId() { return stockId; }
        public String getType() { return type; }
        public int getItemId() { return itemId; }
        public String getItemName() { return itemName; }
        public String getSku() { return sku; }
        public String getCategory() { return category; }
        public int getWarehouseId() { return warehouseId; } // ✅ Getter
        public String getWarehouseName() { return warehouseName; }
        public int getCurrentStock() { return currentStock; }
        public int getMinStockLimit() { return minStockLimit; }
        public String getStatus() { return status; }
        public double getUnitPrice() { return unitPrice; }
        public double getTotalValue() { return totalValue; }
    }

    // 2. Adjustment Request DTO
    public static class StockAdjustmentRequestDTO {
        private int stockId;
        private String adjustmentType;
        private int quantity;
        private String reason;

        public int getStockId() { return stockId; }
        public void setStockId(int stockId) { this.stockId = stockId; }
        public String getAdjustmentType() { return adjustmentType; }
        public void setAdjustmentType(String adjustmentType) { this.adjustmentType = adjustmentType; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}