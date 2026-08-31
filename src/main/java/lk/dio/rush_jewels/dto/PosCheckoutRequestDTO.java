package lk.dio.rush_jewels.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.Valid;

public class PosCheckoutRequestDTO {
    @NotBlank(message = "Customer name is required")
    private String customerName;
    
    private String customerMobile; // Optional for loyalty
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // CASH, CARD, BANK
    
    private String bankReference; // If bank transfer
    
    @NotNull(message = "Total amount is required")
    private Double totalAmount;
    
    private Double discount;        // Legacy: computed discount amount (kept for compatibility)
    private String discountType;    // 'Rs' for flat, '%' for percentage, 'none' for no discount
    private Double discountValue;   // The numeric discount value entered by cashier
    private Boolean includeWarranty;
    private Double tenderedAmount;
    private Double changeDue;
    
    @NotEmpty(message = "Cart cannot be empty")
    @Valid
    private List<PosCartItemDTO> cartItems;

    // Getters & Setters
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerMobile() { return customerMobile; }
    public void setCustomerMobile(String customerMobile) { this.customerMobile = customerMobile; }
    
    public Boolean getIncludeWarranty() { return includeWarranty; }
    public void setIncludeWarranty(Boolean includeWarranty) { this.includeWarranty = includeWarranty; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getBankReference() { return bankReference; }
    public void setBankReference(String bankReference) { this.bankReference = bankReference; }
    
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    
    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public Double getDiscountValue() { return discountValue; }
    public void setDiscountValue(Double discountValue) { this.discountValue = discountValue; }
    
    public List<PosCartItemDTO> getCartItems() { return cartItems; }
    public void setCartItems(List<PosCartItemDTO> cartItems) { this.cartItems = cartItems; }
    
    public Double getTenderedAmount() { return tenderedAmount; }
    public void setTenderedAmount(Double tenderedAmount) { this.tenderedAmount = tenderedAmount; }
    
    public Double getChangeDue() { return changeDue; }
    public void setChangeDue(Double changeDue) { this.changeDue = changeDue; }
}
