package lk.dio.rush_jewels.dto;

public class WarehouseStatsDTO {
    private long totalItems;
    private long lowStockCount;
    private long inStockCount;

    public WarehouseStatsDTO(long totalItems, long lowStockCount, long inStockCount) {
        this.totalItems = totalItems;
        this.lowStockCount = lowStockCount;
        this.inStockCount = inStockCount;
    }
    // Getters...
    public long getTotalItems() { return totalItems; }
    public long getLowStockCount() { return lowStockCount; }
    public long getInStockCount() { return inStockCount; }
}