package lk.dio.rush_jewels.dto;

public class SeasonalSaleBannerDTO {
    private String description;
    private String imageUrl;
    private boolean hasActiveSale;

    public SeasonalSaleBannerDTO(String description, String imageUrl, boolean hasActiveSale) {
        this.description = description;
        this.imageUrl = imageUrl;
        this.hasActiveSale = hasActiveSale;
    }

    // Getters and Setters
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isHasActiveSale() { return hasActiveSale; }
    public void setHasActiveSale(boolean hasActiveSale) { this.hasActiveSale = hasActiveSale; }
}