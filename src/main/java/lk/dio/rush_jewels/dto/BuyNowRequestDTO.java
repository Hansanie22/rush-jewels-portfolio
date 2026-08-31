package lk.dio.rush_jewels.dto;

import java.io.Serializable;

public class BuyNowRequestDTO implements Serializable {
    private Integer productVariantId;
    private Integer collectionId; // Added for Collections
    private Integer quantity;

    public Integer getProductVariantId() {
        return productVariantId;
    }

    public void setProductVariantId(Integer productVariantId) {
        this.productVariantId = productVariantId;
    }

    public Integer getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Integer collectionId) {
        this.collectionId = collectionId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}