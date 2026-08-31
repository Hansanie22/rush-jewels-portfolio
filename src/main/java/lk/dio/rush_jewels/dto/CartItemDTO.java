package lk.dio.rush_jewels.dto;

import java.io.Serializable;

// Represents a single item in the cart response
public class CartItemDTO implements Serializable {
    private String cartId; // String for client-side consistency
    private Integer varianceId;
    private Integer collectionId; // Added field for Collections
    private String name;
    private Double regularPrice;
    private Double discountPercentage;
    private Double finalPrice;
    private Integer availableStock; // Stock sum for validation
    private Integer quantity;
    private String image;

    // Getters and Setters

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public Integer getVarianceId() {
        return varianceId;
    }

    public void setVarianceId(Integer varianceId) {
        this.varianceId = varianceId;
    }

    public Integer getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Integer collectionId) {
        this.collectionId = collectionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getRegularPrice() {
        return regularPrice;
    }

    public void setRegularPrice(Double regularPrice) {
        this.regularPrice = regularPrice;
    }

    public Double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}