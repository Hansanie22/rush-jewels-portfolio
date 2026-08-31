package lk.dio.rush_jewels.dto;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class CheckoutRequestDTO implements Serializable {

    // --- Shipping Address Fields ---
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @NotBlank(message = "Contact number is required")
    private String contactNo;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    private String addressLine1; // Removed NotBlank for Store Pickup
    
    private String addressLine2;
    private String postalCode;
    
    private Integer countryId; // Removed NotNull for Store Pickup
    
    private Integer provinceId; // Removed NotNull for international
    
    private Integer cityId; // Removed NotNull for international
    private String provinceOther;
    private String cityOther;
    private Boolean saveAddress; // Frontend checkbox state

    // --- Billing Address Fields ---
    private Boolean differentBilling; // Checkbox state
    private String billingFirstName;
    private String billingLastName;
    private String billingContactNo;
    private String billingAddressLine1;
    private String billingAddressLine2;
    private String billingPostalCode;
    private Integer billingCountryId;
    private Integer billingProvinceId;
    private Integer billingCityId;
    private String billingProvinceOther;
    private String billingCityOther;

    // --- Payment & Order Fields ---
    @NotBlank(message = "Payment method is required")
    private String selectedPaymentMethod; // e.g., 'card', 'cod'
    private String selectedShippingMethodValue; // e.g., "500.00"
    private String shippingMethodName;

    // Renamed from discountCode to couponCode to match OrderService
    private String couponCode;

    private String orderNotes;
    private Boolean isGift;
    private Boolean giftWrap;
    private String giftMessage;

    // --- NEW: Bank Transfer Fields ---
    private String bankSlipUrl;

    // --- NEW: Validation & Subscription Fields ---
    private Boolean subscribed;
    private Boolean agreeTerms;

    // --- NEW: For PayHere Display ---
    private String itemsDisplay;

    // --- Financial Summary Fields ---
    private Double cartSubtotal;
    private Double shippingCost;
    private Double taxAmount;
    private Double discountAmount;
    private Double finalTotal;

    // --- Getters and Setters ---

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public Integer getCountryId() { return countryId; }
    public void setCountryId(Integer countryId) { this.countryId = countryId; }

    public Integer getProvinceId() { return provinceId; }
    public void setProvinceId(Integer provinceId) { this.provinceId = provinceId; }

    public Integer getCityId() { return cityId; }
    public void setCityId(Integer cityId) { this.cityId = cityId; }

    public String getProvinceOther() { return provinceOther; }
    public void setProvinceOther(String provinceOther) { this.provinceOther = provinceOther; }

    public String getCityOther() { return cityOther; }
    public void setCityOther(String cityOther) { this.cityOther = cityOther; }

    public Boolean getSaveAddress() { return saveAddress; }
    public void setSaveAddress(Boolean saveAddress) { this.saveAddress = saveAddress; }

    public Boolean getDifferentBilling() { return differentBilling; }
    public void setDifferentBilling(Boolean differentBilling) { this.differentBilling = differentBilling; }

    public String getBillingFirstName() { return billingFirstName; }
    public void setBillingFirstName(String billingFirstName) { this.billingFirstName = billingFirstName; }

    public String getBillingLastName() { return billingLastName; }
    public void setBillingLastName(String billingLastName) { this.billingLastName = billingLastName; }

    public String getBillingContactNo() { return billingContactNo; }
    public void setBillingContactNo(String billingContactNo) { this.billingContactNo = billingContactNo; }

    public String getBillingAddressLine1() { return billingAddressLine1; }
    public void setBillingAddressLine1(String billingAddressLine1) { this.billingAddressLine1 = billingAddressLine1; }

    public String getBillingAddressLine2() { return billingAddressLine2; }
    public void setBillingAddressLine2(String billingAddressLine2) { this.billingAddressLine2 = billingAddressLine2; }

    public String getBillingPostalCode() { return billingPostalCode; }
    public void setBillingPostalCode(String billingPostalCode) { this.billingPostalCode = billingPostalCode; }

    public Integer getBillingCountryId() { return billingCountryId; }
    public void setBillingCountryId(Integer billingCountryId) { this.billingCountryId = billingCountryId; }

    public Integer getBillingProvinceId() { return billingProvinceId; }
    public void setBillingProvinceId(Integer billingProvinceId) { this.billingProvinceId = billingProvinceId; }

    public Integer getBillingCityId() { return billingCityId; }
    public void setBillingCityId(Integer billingCityId) { this.billingCityId = billingCityId; }

    public String getBillingProvinceOther() { return billingProvinceOther; }
    public void setBillingProvinceOther(String billingProvinceOther) { this.billingProvinceOther = billingProvinceOther; }

    public String getBillingCityOther() { return billingCityOther; }
    public void setBillingCityOther(String billingCityOther) { this.billingCityOther = billingCityOther; }

    public String getSelectedPaymentMethod() { return selectedPaymentMethod; }
    public void setSelectedPaymentMethod(String selectedPaymentMethod) { this.selectedPaymentMethod = selectedPaymentMethod; }

    public String getSelectedShippingMethodValue() { return selectedShippingMethodValue; }
    public void setSelectedShippingMethodValue(String selectedShippingMethodValue) { this.selectedShippingMethodValue = selectedShippingMethodValue; }

    public String getShippingMethodName() { return shippingMethodName; }
    public void setShippingMethodName(String shippingMethodName) { this.shippingMethodName = shippingMethodName; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public String getOrderNotes() { return orderNotes; }
    public void setOrderNotes(String orderNotes) { this.orderNotes = orderNotes; }

    public Boolean getIsGift() { return isGift; }
    public void setIsGift(Boolean isGift) { this.isGift = isGift; }

    public Boolean getGiftWrap() { return giftWrap; }
    public void setGiftWrap(Boolean giftWrap) { this.giftWrap = giftWrap; }

    public String getGiftMessage() { return giftMessage; }
    public void setGiftMessage(String giftMessage) { this.giftMessage = giftMessage; }

    public Boolean getSubscribed() { return subscribed; }
    public void setSubscribed(Boolean subscribed) { this.subscribed = subscribed; }

    public String getBankSlipUrl() { return bankSlipUrl; }
    public void setBankSlipUrl(String bankSlipUrl) { this.bankSlipUrl = bankSlipUrl; }

    public Boolean getAgreeTerms() { return agreeTerms; }
    public void setAgreeTerms(Boolean agreeTerms) { this.agreeTerms = agreeTerms; }

    public String getItemsDisplay() { return itemsDisplay; }
    public void setItemsDisplay(String itemsDisplay) { this.itemsDisplay = itemsDisplay; }

    public Double getCartSubtotal() { return cartSubtotal; }
    public void setCartSubtotal(Double cartSubtotal) { this.cartSubtotal = cartSubtotal; }

    public Double getShippingCost() { return shippingCost; }
    public void setShippingCost(Double shippingCost) { this.shippingCost = shippingCost; }

    public Double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(Double taxAmount) { this.taxAmount = taxAmount; }

    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }

    public Double getFinalTotal() { return finalTotal; }
    public void setFinalTotal(Double finalTotal) { this.finalTotal = finalTotal; }
}