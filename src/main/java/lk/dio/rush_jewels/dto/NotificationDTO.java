package lk.dio.rush_jewels.dto;

public class NotificationDTO {
    private String type; // "ORDER" or "STOCK"
    private String message;
    private String timeAgo;
    private String iconClass; // FontAwesome class for UI
    private String link; // Where to go when clicked (e.g., #orders)

    public NotificationDTO(String type, String message, String timeAgo, String iconClass, String link) {
        this.type = type;
        this.message = message;
        this.timeAgo = timeAgo;
        this.iconClass = iconClass;
        this.link = link;
    }

    // Getters
    public String getType() { return type; }
    public String getMessage() { return message; }
    public String getTimeAgo() { return timeAgo; }
    public String getIconClass() { return iconClass; }
    public String getLink() { return link; }
}