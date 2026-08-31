package lk.dio.rush_jewels.dto;

import java.util.Date;

public class DiscountCodeDTO {
    private int id;
    private String code;
    private String value; // Percentage value
    private Date expirationDate;
    private Integer usageLimit; // Nullable for unlimited
    private long usedCount; // Calculated from DiscountUsage table
    private boolean isActive;

    public DiscountCodeDTO(int id, String code, String value, Date expirationDate, Integer usageLimit, long usedCount, boolean isActive) {
        this.id = id;
        this.code = code;
        this.value = value;
        this.expirationDate = expirationDate;
        this.usageLimit = usageLimit;
        this.usedCount = usedCount;
        this.isActive = isActive;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public Date getExpirationDate() { return expirationDate; }
    public void setExpirationDate(Date expirationDate) { this.expirationDate = expirationDate; }
    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
    public long getUsedCount() { return usedCount; }
    public void setUsedCount(long usedCount) { this.usedCount = usedCount; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}