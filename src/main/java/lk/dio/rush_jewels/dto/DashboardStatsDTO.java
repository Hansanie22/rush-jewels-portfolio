package lk.dio.rush_jewels.dto;

public class DashboardStatsDTO {
    private double totalRevenue;
    private long totalOrders;
    private long pendingOrders;
    private long totalCustomers;
    private long newCustomersThisMonth;
    private double averageOrderValue;

    // Constructor
    public DashboardStatsDTO(double totalRevenue, long totalOrders, long pendingOrders, long totalCustomers, long newCustomersThisMonth, double averageOrderValue) {
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.pendingOrders = pendingOrders;
        this.totalCustomers = totalCustomers;
        this.newCustomersThisMonth = newCustomersThisMonth;
        this.averageOrderValue = averageOrderValue;
    }

    // Getters
    public double getTotalRevenue() { return totalRevenue; }
    public long getTotalOrders() { return totalOrders; }
    public long getPendingOrders() { return pendingOrders; }
    public long getTotalCustomers() { return totalCustomers; }
    public long getNewCustomersThisMonth() { return newCustomersThisMonth; }
    public double getAverageOrderValue() { return averageOrderValue; }
}