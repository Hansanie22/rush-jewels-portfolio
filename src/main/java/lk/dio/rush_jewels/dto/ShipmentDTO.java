package lk.dio.rush_jewels.dto;

import java.util.Date;

public class ShipmentDTO {
    private int id;
    private String trackingNumber;
    private String orderId; // String ID from Orders entity
    private Date shippedDate;
    private String status; // Enum String
    private String courierName; // For display
    private String destinationCity; // From Order Address

    // Constructors
    public ShipmentDTO() {}
    public ShipmentDTO(int id, String trackingNumber, String orderId, Date shippedDate, String status, String courierName, String destinationCity) {
        this.id = id;
        this.trackingNumber = trackingNumber;
        this.orderId = orderId;
        this.shippedDate = shippedDate;
        this.status = status;
        this.courierName = courierName;
        this.destinationCity = destinationCity;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public Date getShippedDate() { return shippedDate; }
    public void setShippedDate(Date shippedDate) { this.shippedDate = shippedDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCourierName() { return courierName; }
    public void setCourierName(String courierName) { this.courierName = courierName; }
    public String getDestinationCity() { return destinationCity; }
    public void setDestinationCity(String destinationCity) { this.destinationCity = destinationCity; }
}