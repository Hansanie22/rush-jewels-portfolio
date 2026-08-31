package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "discount_usages")
public class DiscountUsage implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "used_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date usedAt;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "orders_id", length = 50)
    private String ordersId;

    // ⭐ Many Usages → One DiscountCode
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_code_id", nullable = false)
    private DiscountCode discountCode;

    public DiscountUsage() {}

    public DiscountUsage(int id, Date usedAt, int userId, String ordersId, DiscountCode discountCode) {
        this.id = id;
        this.usedAt = usedAt;
        this.userId = userId;
        this.ordersId = ordersId;
        this.discountCode = discountCode;
    }

    // Getters & Setters...

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Date getUsedAt() { return usedAt; }
    public void setUsedAt(Date usedAt) { this.usedAt = usedAt; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getOrdersId() { return ordersId; }
    public void setOrdersId(String ordersId) { this.ordersId = ordersId; }

    public DiscountCode getDiscountCode() { return discountCode; }
    public void setDiscountCode(DiscountCode discountCode) { this.discountCode = discountCode; }
}
