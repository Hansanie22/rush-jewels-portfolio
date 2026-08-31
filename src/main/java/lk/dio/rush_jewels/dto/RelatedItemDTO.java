package lk.dio.rush_jewels.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelatedItemDTO {
    private int id; // Maps to varianceId for Products
    private String title;
    private String name; // Added for compatibility
    private String imagePath;
    private Double price;
    private Double regularPrice;
    private Double discountPercentage; // Added
    private String type; // "PRODUCT" or "COLLECTION"

    // New fields for the card design
    private Double averageRating;
    private int reviewCount;
    private String stockStatus;
    private int currentStockQty; // Reusing stockLimit logic
}