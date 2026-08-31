package lk.dio.rush_jewels.dto;

import java.util.Date;

public class ShipmentDetailDTO {
    // Shipment Info
    private String trackingNumber;
    private String status;
    private Date shippedDate;
    private Date estimatedDate;

    // Order Info
    private String orderId;
    private Date orderDate;
    private double orderTotal;

    // Customer Info
    private String customerName;
    private String customerEmail;

    // Shipping Address
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String contactNo;

    public ShipmentDetailDTO(String trackingNumber, String status, Date shippedDate, Date estimatedDate, String orderId, Date orderDate, double orderTotal, String customerName, String customerEmail, String addressLine1, String addressLine2, String city, String contactNo) {
        this.trackingNumber = trackingNumber;
        this.status = status;
        this.shippedDate = shippedDate;
        this.estimatedDate = estimatedDate;
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.orderTotal = orderTotal;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.contactNo = contactNo;
    }

    // Getters...
    public String getTrackingNumber() { return trackingNumber; }
    public String getStatus() { return status; }
    public Date getShippedDate() { return shippedDate; }
    public Date getEstimatedDate() { return estimatedDate; }
    public String getOrderId() { return orderId; }
    public Date getOrderDate() { return orderDate; }
    public double getOrderTotal() { return orderTotal; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getAddressLine1() { return addressLine1; }
    public String getAddressLine2() { return addressLine2; }
    public String getCity() { return city; }
    public String getContactNo() { return contactNo; }
}