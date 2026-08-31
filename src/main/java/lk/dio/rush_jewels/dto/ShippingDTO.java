package lk.dio.rush_jewels.dto;

import lk.dio.rush_jewels.model.Shipping;

public class ShippingDTO {

    private int id;
    private String shippingMethod;
    private Double value;
    private String description; // <-- ADD THIS

    public ShippingDTO(Shipping shipping) {
        this.id = shipping.getId();
        this.shippingMethod = shipping.getShippingMethod();
        this.value = shipping.getValue();
        this.description = shipping.getDescription(); 
    }

    public ShippingDTO(int id, String shippingMethod, Double value, String description) {
        this.id = id;
        this.shippingMethod = shippingMethod;
        this.value = value;
        this.description = description;
    }

    // --- Getters ---
    public int getId() {
        return id;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public Double getValue() {
        return value;
    }

    public String getDescription() { // <-- ADD THIS GETTER
        return description;
    }
}