package lk.dio.rush_jewels.model;

public enum ShipmentStatus {
    SHIPPED,            // Carrier has accepted the package
    IN_TRANSIT,         // Package is moving between facilities
    OUT_FOR_DELIVERY,   // Delivery driver has the package
    DELIVERED,           // Customer has received it
    RETURNED            // Customer not picked / failed
    }