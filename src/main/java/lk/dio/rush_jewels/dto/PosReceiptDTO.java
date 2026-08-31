package lk.dio.rush_jewels.dto;

import java.util.List;

public class PosReceiptDTO {
    private String orderId;
    private String date;
    private String cashierName;
    private String customerMobile;
    private List<PosReceiptItemDTO> items;
    private Double subTotal;
    private Double discount;
    private Double finalTotal;
    private String paymentMethod;
    private String warrantyInfo; // Specific to Jewelry

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }

    public String getCustomerMobile() { return customerMobile; }
    public void setCustomerMobile(String customerMobile) { this.customerMobile = customerMobile; }

    public List<PosReceiptItemDTO> getItems() { return items; }
    public void setItems(List<PosReceiptItemDTO> items) { this.items = items; }

    public Double getSubTotal() { return subTotal; }
    public void setSubTotal(Double subTotal) { this.subTotal = subTotal; }

    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }

    public Double getFinalTotal() { return finalTotal; }
    public void setFinalTotal(Double finalTotal) { this.finalTotal = finalTotal; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getWarrantyInfo() { return warrantyInfo; }
    public void setWarrantyInfo(String warrantyInfo) { this.warrantyInfo = warrantyInfo; }

    public static class PosReceiptItemDTO {
        private String itemName;
        private int qty;
        private Double unitPrice;
        private Double totalPrice;

        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }

        public int getQty() { return qty; }
        public void setQty(int qty) { this.qty = qty; }

        public Double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

        public Double getTotalPrice() { return totalPrice; }
        public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    }
}
