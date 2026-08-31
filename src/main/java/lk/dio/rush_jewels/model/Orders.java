package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "orders")
public class Orders implements Serializable {

    @Id
    @Column(name = "id", length = 50, nullable = false, updatable = false)
    private String id;

    @Column(name = "ordered_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date orderedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "delivery_address_id", nullable = false)
    private DeliveryAddress deliveryAddress;

    @ManyToOne
    @JoinColumn(name = "order_status_id", nullable = false)
    private OrderStatus orderStatus;

    // This usually represents the Shipping Method (e.g., Standard vs Express)
    @ManyToOne
    @JoinColumn(name = "shipping_id", nullable = false)
    private Shipping shipping;

    @Column(name = "order_note", columnDefinition = "TEXT" ,nullable = true)
    private String orderNote;

    @Column(
            name = "is_gift",
            nullable = false,
            columnDefinition = "BIT(1) DEFAULT 0"
    )
    private boolean isGift = false;

    // ONLINE or POS
    @Column(name = "order_source", length = 20, nullable = false)
    private String orderSource = "ONLINE";

    @Column(name = "slip_url", length = 500)
    private String slipUrl;

    // This represents the physical Dispatch/Tracking history
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<Shipments> shipments;

    // 🧱 Constructors
    public Orders() {
    }

    public Orders(String id, Date orderedAt, User user, DeliveryAddress deliveryAddress, OrderStatus orderStatus) {
        this.id = id;
        this.orderedAt = orderedAt;
        this.user = user;
        this.deliveryAddress = deliveryAddress;
        this.orderStatus = orderStatus;
    }

    // 🧩 Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(Date orderedAt) {
        this.orderedAt = orderedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DeliveryAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(DeliveryAddress deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Shipping getShipping() {
        return shipping;
    }

    public void setShipping(Shipping shipping) {
        this.shipping = shipping;
    }

    public String getOrderNote() {
        return orderNote;
    }

    public void setOrderNote(String orderNote) {
        this.orderNote = orderNote;
    }

    public boolean isGift() {
        return isGift;
    }

    public void setGift(boolean gift) {
        isGift = gift;
    }

    // ✅ ADDED THESE SO THE SERVICE CAN ACCESS TRACKING INFO
    public List<Shipments> getShipments() {
        return shipments;
    }

    public void setShipments(List<Shipments> shipments) {
        this.shipments = shipments;
    }

    public String getOrderSource() {
        return orderSource;
    }

    public void setOrderSource(String orderSource) {
        this.orderSource = orderSource;
    }

    public String getSlipUrl() {
        return slipUrl;
    }

    public void setSlipUrl(String slipUrl) {
        this.slipUrl = slipUrl;
    }
}