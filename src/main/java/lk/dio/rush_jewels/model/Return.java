package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "`return`")
public class Return implements Serializable {

    @Id
    // Image shows ID is varchar(50), so we use String (not @GeneratedValue IDENTITY)
    @Column(length = 50)
    private String id;

    @Column(name = "request_date")
    private LocalDateTime requestDate;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ReturnStatus status;

    @Column(name = "return_reason", columnDefinition = "TEXT")
    private String returnReason;

    // Relationship mapping for orders_id
    @ManyToOne
    @JoinColumn(name = "orders_id", referencedColumnName = "id", nullable = false)
    private Orders orders;

    // Relationship mapping for return_type_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_type_id", nullable = false)
    private ReturnType returnType;

    // 🧱 Constructors
    public Return() {
    }

    public Return(String id, LocalDateTime requestDate, LocalDateTime approvedDate, ReturnStatus status,
                  String returnReason, Orders orders, ReturnType returnType) {
        this.id = id;
        this.requestDate = requestDate;
        this.approvedDate = approvedDate;
        this.status = status;
        this.returnReason = returnReason;
        this.orders = orders;
        this.returnType = returnType;
    }

    // 🧩 Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDateTime getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(LocalDateTime approvedDate) {
        this.approvedDate = approvedDate;
    }

    public ReturnStatus getStatus() {
        return status;
    }

    public void setStatus(ReturnStatus status) {
        this.status = status;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public Orders getOrders() {
        return orders;
    }

    public void setOrders(Orders orders) {
        this.orders = orders;
    }

    public ReturnType getReturnType() {
        return returnType;
    }

    public void setReturnType(ReturnType returnType) {
        this.returnType = returnType;
    }

    // Automatically set the request date when the return is first created
    @PrePersist
    protected void onCreate() {
        if (this.requestDate == null) {
            this.requestDate = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = ReturnStatus.RETURN_REQUESTED; // Default status
        }
    }
}
