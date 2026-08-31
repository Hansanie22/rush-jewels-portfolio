package lk.dio.rush_jewels.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

public class PosCartItemDTO {
    @NotBlank(message = "Item type is required")
    private String itemType; // "PRODUCT" or "COLLECTION"
    
    private Integer variantId;
    private Integer collectionId;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    private int qty;

    // Getters & Setters
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    
    public Integer getVariantId() { return variantId; }
    public void setVariantId(Integer variantId) { this.variantId = variantId; }

    public Integer getCollectionId() { return collectionId; }
    public void setCollectionId(Integer collectionId) { this.collectionId = collectionId; }
    
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
}
