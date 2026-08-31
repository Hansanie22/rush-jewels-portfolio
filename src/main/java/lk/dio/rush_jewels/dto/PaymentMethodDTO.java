package lk.dio.rush_jewels.dto;

import lk.dio.rush_jewels.model.PaymentMethod;

public class PaymentMethodDTO {

    private int id;
    private String method;

    public PaymentMethodDTO(PaymentMethod paymentMethod) {
        this.id = paymentMethod.getId();
        this.method = paymentMethod.getMethod();
    }

    // --- Getters ---
    public int getId() {
        return id;
    }

    public String getMethod() {
        return method;
    }
}