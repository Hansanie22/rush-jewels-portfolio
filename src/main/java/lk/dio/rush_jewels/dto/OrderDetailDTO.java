package lk.dio.rush_jewels.dto;

import java.util.Date;
import java.util.List;

public class OrderDetailDTO {
    // Order Info
    private String orderId;
    private Date date;

    // Financial Breakdown
    private double subTotal;
    private double tax;
    private double shipping;
    private double discount;
    private double total;

    private String paymentMethod;
    private String paymentStatus;
    private String deliveryStatus;
    private String trackingNumber;
    private String notes;
    private String slipUrl;

    private boolean isGift;

    // Customer Info
    private String customerName;
    private String email;
    private String phone;

    // Address
    private String address;

    // Items
    private List<OrderItemDTO> items;

    // Return Info
    private boolean isReturn;
    private String returnType;
    private String returnReason;
    private String returnStatus;
    private String returnDate;

    public static class OrderItemDTO {
        public String name;
        public String sku;
        public int qty;
        public double price;
        public double subtotal;
        public String subtext;
        private boolean returned;

        public OrderItemDTO(String name, String sku, int qty, double price, String subtext) {
            this.name = name;
            this.sku = sku;
            this.qty = qty;
            this.price = price;
            this.subtotal = price * qty;
            this.subtext = subtext;
        }

        // ✅ Getters and Setters for returned field
        public boolean isReturned() { return returned; }
        public void setReturned(boolean returned) { this.returned = returned; }
    }

    public OrderDetailDTO() {}

    // Getters & Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public double getSubTotal() { return subTotal; }
    public void setSubTotal(double subTotal) { this.subTotal = subTotal; }
    public double getTax() { return tax; }
    public void setTax(double tax) { this.tax = tax; }
    public double getShipping() { return shipping; }
    public void setShipping(double shipping) { this.shipping = shipping; }
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getSlipUrl() { return slipUrl; }
    public void setSlipUrl(String slipUrl) { this.slipUrl = slipUrl; }
    public boolean isGift() { return isGift; }
    public void setGift(boolean gift) { isGift = gift; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
    public boolean isReturn() { return isReturn; }
    public void setReturn(boolean aReturn) { isReturn = aReturn; }
    public String getReturnType() { return returnType; }
    public void setReturnType(String returnType) { this.returnType = returnType; }
    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }
    public String getReturnStatus() { return returnStatus; }
    public void setReturnStatus(String returnStatus) { this.returnStatus = returnStatus; }
    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }
}