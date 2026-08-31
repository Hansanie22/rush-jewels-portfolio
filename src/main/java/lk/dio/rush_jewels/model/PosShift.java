package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pos_shift")
public class PosShift implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "cashier_id")
    private User cashier;

    @Column(name = "start_time")
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Column(name = "total_sales")
    private Double totalSales;

    @Column(name = "cash_sales")
    private Double cashSales;

    @Column(name = "card_sales")
    private Double cardSales;

    @Column(name = "return_amount")
    private Double returnAmount;

    @Column(name = "expected_cash")
    private Double expectedCash;

    @Column(name = "actual_cash")
    private Double actualCash;

    @Column(name = "petty_cash_used")
    private Double pettyCashUsed;

    @Column(name = "difference_reason", columnDefinition = "TEXT")
    private String differenceReason;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getCashier() { return cashier; }
    public void setCashier(User cashier) { this.cashier = cashier; }

    public OffsetDateTime getStartTime() { return startTime; }
    public void setStartTime(OffsetDateTime startTime) { this.startTime = startTime; }

    public OffsetDateTime getEndTime() { return endTime; }
    public void setEndTime(OffsetDateTime endTime) { this.endTime = endTime; }

    public Double getTotalSales() { return totalSales; }
    public void setTotalSales(Double totalSales) { this.totalSales = totalSales; }

    public Double getCashSales() { return cashSales; }
    public void setCashSales(Double cashSales) { this.cashSales = cashSales; }

    public Double getCardSales() { return cardSales; }
    public void setCardSales(Double cardSales) { this.cardSales = cardSales; }

    public Double getReturnAmount() { return returnAmount; }
    public void setReturnAmount(Double returnAmount) { this.returnAmount = returnAmount; }

    public Double getExpectedCash() { return expectedCash; }
    public void setExpectedCash(Double expectedCash) { this.expectedCash = expectedCash; }

    public Double getActualCash() { return actualCash; }
    public void setActualCash(Double actualCash) { this.actualCash = actualCash; }

    public Double getPettyCashUsed() { return pettyCashUsed; }
    public void setPettyCashUsed(Double pettyCashUsed) { this.pettyCashUsed = pettyCashUsed; }

    public String getDifferenceReason() { return differenceReason; }
    public void setDifferenceReason(String differenceReason) { this.differenceReason = differenceReason; }
}
