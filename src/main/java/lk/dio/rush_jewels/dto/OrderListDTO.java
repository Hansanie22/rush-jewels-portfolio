package lk.dio.rush_jewels.dto;

import java.util.Date;

public class OrderListDTO {
    private String orderId;
    private String customerName;
    private String customerEmail;
    private Date date;
    private double total;
    private String paymentStatus;
    private String deliveryStatus;
    private String paymentMethod;
    // Return Specific Fields
    private boolean isReturn;
    private String returnType;
    private String returnStatus;
    private Date returnDate;

    public OrderListDTO(String orderId, String customerName, String customerEmail, Date date, double total,
                        String paymentStatus, String deliveryStatus, boolean isReturn,
                        String returnType, String returnStatus, Date returnDate, String paymentMethod) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.date = date;
        this.total = total;
        this.paymentStatus = paymentStatus;
        this.deliveryStatus = deliveryStatus;
        this.isReturn = isReturn;
        this.returnType = returnType;
        this.returnStatus = returnStatus;
        this.returnDate = returnDate;
        this.paymentMethod = paymentMethod;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public Date getDate() { return date; }
    public double getTotal() { return total; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public boolean isReturn() { return isReturn; }
    public String getReturnType() { return returnType; }
    public String getReturnStatus() { return returnStatus; }
    public Date getReturnDate() { return returnDate; }
    public String getPaymentMethod() { return paymentMethod; }
}