package lk.dio.rush_jewels.dto;

public class PayHereRequestDTO {

    public PayHereRequestDTO() {}

    // Financial and Transaction Details
    private String merchantId;
    private String orderId;
    private Double amount;
    private String currency;
    private String hash; // Calculated hash for security

    // Shipping Contact Details
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address; // Shipping Line 1
    private String city;
    private String country; // Shipping Country name
    private String items;

    // Custom Fields for Internal Data Transmission
    // NEW: Used to send the internal order payload (user ID, amounts, etc.)
    // to PayHere and retrieve it via the IPN.
    private String custom1;
    private String custom2;

    // Billing Address Fields
    private String billingFirstName;
    private String billingLastName;
    private String billingAddress; // Billing Line 1
    private String billingCity;
    private String billingCountry; // Billing Country name

    private String returnUrl;
    private String cancelUrl;
    private String notifyUrl;

    // --- Getters and Setters (Updated) ---

    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getItems() { return items; }
    public void setItems(String items) { this.items = items; }

    // *** NEW: Custom Fields Getters and Setters ***
    public String getCustom1() { return custom1; }
    public void setCustom1(String custom1) { this.custom1 = custom1; }
    public String getCustom2() { return custom2; }
    public void setCustom2(String custom2) { this.custom2 = custom2; }

    // *** Billing Getters and Setters ***
    public String getBillingFirstName() { return billingFirstName; }
    public void setBillingFirstName(String billingFirstName) { this.billingFirstName = billingFirstName; }
    public String getBillingLastName() { return billingLastName; }
    public void setBillingLastName(String billingLastName) { this.billingLastName = billingLastName; }
    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }
    public String getBillingCity() { return billingCity; }
    public void setBillingCity(String billingCity) { this.billingCity = billingCity; }
    public String getBillingCountry() { return billingCountry; }
    public void setBillingCountry(String billingCountry) { this.billingCountry = billingCountry; }

    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
    public String getCancelUrl() { return cancelUrl; }
    public void setCancelUrl(String cancelUrl) { this.cancelUrl = cancelUrl; }
    public String getNotifyUrl() { return notifyUrl; }
    public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
}