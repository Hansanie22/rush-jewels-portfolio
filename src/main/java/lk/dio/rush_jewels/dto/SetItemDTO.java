package lk.dio.rush_jewels.dto;

public class SetItemDTO {
    private int varianceId;
    private String varianceName;
    private int qty;

    // No-Args Constructor
    public SetItemDTO() {}

    // Parameterized Constructor
    public SetItemDTO(int varianceId, String varianceName, int qty) {
        this.varianceId = varianceId;
        this.varianceName = varianceName;
        this.qty = qty;
    }

    // Getters and Setters
    public int getVarianceId() { return varianceId; }
    public void setVarianceId(int varianceId) { this.varianceId = varianceId; }
    public String getVarianceName() { return varianceName; }
    public void setVarianceName(String varianceName) { this.varianceName = varianceName; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
}