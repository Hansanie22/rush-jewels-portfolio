package lk.dio.rush_jewels.dto;

public class ReturnListDTO {
    private String returnId;
    private String orderId;
    private String customerName;
    private String type;     // "Exchange", "Customer not picked"
    private String reason;
    private String status;   // "PENDING", "APPROVED", "COMPLETED"
    private String date;     // Request Date

    public ReturnListDTO(String returnId, String orderId, String customerName, String type, String reason, String status, String date) {
        this.returnId = returnId;
        this.orderId = orderId;
        this.customerName = customerName;
        this.type = type;
        this.reason = reason;
        this.status = status;
        this.date = date;
    }

    // Getters
    public String getReturnId() { return returnId; }
    public String getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getType() { return type; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public String getDate() { return date; }
}