package lk.dio.rush_jewels.dto;

public class DiscountDTO {

    private String type; // "PERCENT" or "FIXED"
    private double value;
    private String message;

    public DiscountDTO(String type, double value, String message) {
        this.type = type;
        this.value = value;
        this.message = message;
    }

    // Getters
    public String getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }
}