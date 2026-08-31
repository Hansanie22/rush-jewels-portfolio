package lk.dio.rush_jewels.dto;

public class AnalyticsDTOs {

    // 1. Sales Report Row (Updated)
    public static class SalesReportDTO {
        private String date;
        private long totalOrders;
        private double totalRevenue; // Gross Revenue
        private double averageOrderValue;

        // New Fields for Returns
        private long totalReturns;
        private double totalRefunded;
        private double netRevenue;

        public SalesReportDTO(String date, long totalOrders, double totalRevenue) {
            this.date = date;
            this.totalOrders = totalOrders;
            this.totalRevenue = totalRevenue;
            this.averageOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;

            // Defaults
            this.totalReturns = 0;
            this.totalRefunded = 0.0;
            this.netRevenue = totalRevenue;
        }

        // Setters for merging logic
        public void setReturnStats(long totalReturns, double totalRefunded) {
            this.totalReturns = totalReturns;
            this.totalRefunded = totalRefunded;
            this.netRevenue = this.totalRevenue - totalRefunded;
        }

        // Getters
        public String getDate() { return date; }
        public long getTotalOrders() { return totalOrders; }
        public double getTotalRevenue() { return totalRevenue; }
        public double getAverageOrderValue() { return averageOrderValue; }
        public long getTotalReturns() { return totalReturns; }
        public double getTotalRefunded() { return totalRefunded; }
        public double getNetRevenue() { return netRevenue; }
    }

    // 2. Product Performance Row (Updated)
    public static class ProductPerformanceDTO {
        private String productName;
        private String category;
        private long unitsSold; // Gross Sold
        private double revenueGenerated; // Gross Revenue
        private int currentStock;

        // New Fields
        private long unitsReturned;
        private long netUnitsSold;
        private double netRevenue;

        public ProductPerformanceDTO(String productName, String category, long unitsSold, double revenueGenerated, int currentStock, long unitsReturned) {
            this.productName = productName;
            this.category = category;
            this.unitsSold = unitsSold;
            this.revenueGenerated = revenueGenerated;
            this.currentStock = currentStock;
            this.unitsReturned = unitsReturned;

            // Calculate Net Stats
            this.netUnitsSold = unitsSold - unitsReturned;
            // Approximate Net Revenue (Revenue per unit * Net Units)
            double pricePerUnit = unitsSold > 0 ? revenueGenerated / unitsSold : 0;
            this.netRevenue = this.netUnitsSold * pricePerUnit;
        }

        // Getters
        public String getProductName() { return productName; }
        public String getCategory() { return category; }
        public long getUnitsSold() { return unitsSold; }
        public double getRevenueGenerated() { return revenueGenerated; }
        public int getCurrentStock() { return currentStock; }
        public long getUnitsReturned() { return unitsReturned; }
        public long getNetUnitsSold() { return netUnitsSold; }
        public double getNetRevenue() { return netRevenue; }
    }

    // 3. Finance Report Row (Unchanged)
    public static class FinanceReportDTO {
        private String paymentMethod;
        private long transactionCount;
        private double totalAmount;
        private double taxCollected;
        private double discountGiven;

        // ✅ FIXED CONSTRUCTOR: Accepts 5 arguments now
        public FinanceReportDTO(String paymentMethod, long transactionCount, double totalAmount, double taxCollected, double discountGiven) {
            this.paymentMethod = paymentMethod;
            this.transactionCount = transactionCount;
            this.totalAmount = totalAmount;
            this.taxCollected = taxCollected; // Set directly from DB value
            this.discountGiven = discountGiven;
        }

        // Getters
        public String getPaymentMethod() { return paymentMethod; }
        public long getTransactionCount() { return transactionCount; }
        public double getTotalAmount() { return totalAmount; }
        public double getTaxCollected() { return taxCollected; }
        public void setTaxCollected(double taxCollected) { this.taxCollected = taxCollected; }
        public double getDiscountGiven() { return discountGiven; }
    }

    // 4. Top Customers Report Row
    public static class TopCustomerDTO {
        private String customerName;
        private long totalOrders;
        private double totalSpent;

        public TopCustomerDTO(String customerName, long totalOrders, double totalSpent) {
            this.customerName = customerName;
            this.totalOrders = totalOrders;
            this.totalSpent = totalSpent;
        }

        public String getCustomerName() { return customerName; }
        public long getTotalOrders() { return totalOrders; }
        public double getTotalSpent() { return totalSpent; }
    }

    // 5. Order Status Breakdown Row
    public static class OrderStatusDTO {
        private String status;
        private long count;

        public OrderStatusDTO(String status, long count) {
            this.status = status;
            this.count = count;
        }

        public String getStatus() { return status; }
        public long getCount() { return count; }
    }

    // 6. Transaction History Row
    public static class TransactionHistoryDTO {
        private String dateTime;       // e.g. "2025-07-29 14:35:00"
        private String orderId;
        private String transactionId;
        private String customerName;
        private String channel;        // "WEB" or "POS"
        private String paymentMethod;
        private String paymentStatus;
        private double subTotal;
        private double discount;
        private double finalTotal;
        private double tenderedAmount;
        private double changeDue;

        public TransactionHistoryDTO(String dateTime, String orderId, String transactionId,
                                     String customerName, String channel,
                                     String paymentMethod, String paymentStatus,
                                     double subTotal, double discount, double finalTotal,
                                     double tenderedAmount, double changeDue) {
            this.dateTime      = dateTime;
            this.orderId       = orderId;
            this.transactionId = transactionId;
            this.customerName  = customerName;
            this.channel       = channel;
            this.paymentMethod = paymentMethod;
            this.paymentStatus = paymentStatus;
            this.subTotal      = subTotal;
            this.discount      = discount;
            this.finalTotal    = finalTotal;
            this.tenderedAmount = tenderedAmount;
            this.changeDue     = changeDue;
        }

        public String getDateTime()      { return dateTime; }
        public String getOrderId()       { return orderId; }
        public String getTransactionId() { return transactionId; }
        public String getCustomerName()  { return customerName; }
        public String getChannel()       { return channel; }
        public String getPaymentMethod() { return paymentMethod; }
        public String getPaymentStatus() { return paymentStatus; }
        public double getSubTotal()      { return subTotal; }
        public double getDiscount()      { return discount; }
        public double getFinalTotal()    { return finalTotal; }
        public double getTenderedAmount() { return tenderedAmount; }
        public double getChangeDue()     { return changeDue; }
    }
}