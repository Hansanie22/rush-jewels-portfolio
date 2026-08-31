package lk.dio.rush_jewels.dto;

public class WarehouseActivityDTO {
    private String type; // "Adjustment" or "Transfer"
    private String description;
    private String timeAgo;
    private String color; // "blue", "green", "red"

    public WarehouseActivityDTO(String type, String description, String timeAgo, String color) {
        this.type = type;
        this.description = description;
        this.timeAgo = timeAgo;
        this.color = color;
    }
    // Getters...
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getTimeAgo() { return timeAgo; }
    public String getColor() { return color; }
}