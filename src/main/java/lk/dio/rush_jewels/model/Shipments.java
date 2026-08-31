package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "shipments")
public class Shipments implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "tracking_number", length = 100, nullable = false)
    private String trackingNumber;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "shipped_date")
    private Date shippedDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "estimated_delivery")
    private Date estimatedDelivery;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private ShipmentStatus status;

    @ManyToOne
    @JoinColumn(name = "orders_id", nullable = false)
    private Orders order;

    public Shipments() {}

    public Shipments(String trackingNumber, Date shippedDate, Date estimatedDelivery,
                     ShipmentStatus status, Orders order) {
        this.trackingNumber = trackingNumber;
        this.shippedDate = shippedDate;
        this.estimatedDelivery = estimatedDelivery;
        this.status = status;
        this.order = order;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public Date getShippedDate() { return shippedDate; }
    public void setShippedDate(Date shippedDate) { this.shippedDate = shippedDate; }

    public Date getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(Date estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }

    public ShipmentStatus getStatus() { return status; }
    public void setStatus(ShipmentStatus status) { this.status = status; }

    public Orders getOrder() { return order; }
    public void setOrder(Orders order) { this.order = order; }
}
