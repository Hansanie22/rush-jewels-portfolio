package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "discount_code")
public class DiscountCode implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 45, nullable = false)
    private String code;

    @Column(length = 45, nullable = false)
    private String value;

    @Column(name = "expiration_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationDate;

    @Column(name = "usage_limit")
    private int usageLimit;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    // ⭐ One DiscountCode → Many DiscountUsages
    @OneToMany(mappedBy = "discountCode", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DiscountUsage> usages;

    public DiscountCode() {}

    public DiscountCode(int id, String code, String value, Date expirationDate, int usageLimit, boolean isActive, Date createdAt) {
        this.id = id;
        this.code = code;
        this.value = value;
        this.expirationDate = expirationDate;
        this.usageLimit = usageLimit;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // Getters & Setters...

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public Date getExpirationDate() { return expirationDate; }
    public void setExpirationDate(Date expirationDate) { this.expirationDate = expirationDate; }

    public int getUsageLimit() { return usageLimit; }
    public void setUsageLimit(int usageLimit) { this.usageLimit = usageLimit; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public List<DiscountUsage> getUsages() { return usages; }
    public void setUsages(List<DiscountUsage> usages) { this.usages = usages; }
}
