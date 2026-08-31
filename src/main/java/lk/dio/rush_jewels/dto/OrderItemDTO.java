package lk.dio.rush_jewels.dto;

import java.io.Serializable;

/**
 * DTO for individual order items in Order History
 */
public class OrderItemDTO implements Serializable {
    private Integer variantId; // Holds ProductVariance ID or Collection ID
    private String productName;
    private Integer quantity;
    private Double price;
    private Double subtotal;
    private String image;

    public OrderItemDTO() {}

    // Getters and Setters
    public Integer getVariantId() { return variantId; }
    public void setVariantId(Integer variantId) { this.variantId = variantId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}