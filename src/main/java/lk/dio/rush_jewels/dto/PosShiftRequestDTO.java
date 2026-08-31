package lk.dio.rush_jewels.dto;

import java.time.OffsetDateTime;

public class PosShiftRequestDTO {
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private Double totalSales;
    private Double cashSales;
    private Double cardSales;
    private Double returnAmount;
    private Double expectedCash;
    private Double actualCash;
    private Double pettyCashUsed;
    private String differenceReason;

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
