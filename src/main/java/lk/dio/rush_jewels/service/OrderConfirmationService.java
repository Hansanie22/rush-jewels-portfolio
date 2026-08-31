package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.dto.OrderConfirmationDTO;
import lk.dio.rush_jewels.dto.OrderConfirmationDTO.AddressDTO;
import lk.dio.rush_jewels.dto.OrderConfirmationDTO.OrderItemDTO;
import lk.dio.rush_jewels.dto.OrderConfirmationDTO.PaymentDTO;
import lk.dio.rush_jewels.model.*;
import lk.dio.rush_jewels.model.Collection;
import lk.dio.rush_jewels.repository.*;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderConfirmationService {

    private final OrdersRepository ordersRepo;
    private final OrderItemsRepository orderItemsRepo;
    private final PaymentRepository paymentRepo;


    public OrderConfirmationService(OrdersRepository ordersRepo,
                                    OrderItemsRepository orderItemsRepo,
                                    PaymentRepository paymentRepo) {
        this.ordersRepo = ordersRepo;
        this.orderItemsRepo = orderItemsRepo;
        this.paymentRepo = paymentRepo;
    }

    public OrderConfirmationDTO getOrderConfirmation(String orderId, User user) {
        // Fetch order
        Orders order = ordersRepo.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));

        // Verify user owns this order
        if (order.getUser().getId() != user.getId()) {
            throw new SecurityException("Unauthorized access to order: " + orderId);
        }

        OrderConfirmationDTO dto = new OrderConfirmationDTO();

        // Basic Order Info
        dto.setOrderNumber(order.getId());
        dto.setOrderDate(order.getOrderedAt());
        dto.setOrderStatus(order.getOrderStatus().getOrderStatus());
        dto.setExpectedDelivery(calculateExpectedDelivery(order.getOrderedAt()));

        // Customer Info
        dto.setCustomerEmail(user.getEmail());
        dto.setCustomerPhone(order.getDeliveryAddress() != null ? order.getDeliveryAddress().getContactNo() : "");
        dto.setShippingMethod(order.getShipping() != null ? order.getShipping().getShippingMethod() : "");

        // Shipping Address
        if (order.getDeliveryAddress() != null) {
            dto.setShippingAddress(mapToAddressDTO(order.getDeliveryAddress()));
        }

        // Payment Details
        Optional<Payment> paymentOpt = paymentRepo.findByOrders(order);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            dto.setPayment(mapToPaymentDTO(payment));

            // Financial Details from Payment
            dto.setSubtotal(payment.getSubTotal());
            dto.setTotal(payment.getFinalTotal());

            // Calculate shipping and tax
            Double shippingCost = order.getShipping() != null ? order.getShipping().getValue() : 0.0;
            dto.setShippingCost(shippingCost);

            // Tax = Total - Subtotal - Shipping
            dto.setTaxAmount(payment.getTax() != null ? payment.getTax() : 0.0);
            dto.setDiscountAmount(payment.getDiscount() != null ? payment.getDiscount() : 0.0);
        }

        // Order Items
        List<OrderItems> orderItems = orderItemsRepo.findByOrders(order);
        dto.setItems(orderItems.stream()
                .map(this::mapToOrderItemDTO)
                .collect(Collectors.toList()));

        return dto;
    }

    /**
     * Get order entities for email sending
     */
    public OrderEmailData getOrderEmailData(String orderId, User user) {
        Orders order = ordersRepo.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));

        if (order.getUser().getId() != user.getId()) {
            throw new SecurityException("Unauthorized access to order: " + orderId);
        }

        List<OrderItems> orderItems = orderItemsRepo.findByOrders(order);
        Payment payment = paymentRepo.findByOrders(order)
                .orElseThrow(() -> new NoSuchElementException("Payment not found for order: " + orderId));

        return new OrderEmailData(order, orderItems, payment);
    }

    public static class OrderEmailData {
        private final Orders order;
        private final List<OrderItems> orderItems;
        private final Payment payment;

        public OrderEmailData(Orders order, List<OrderItems> orderItems, Payment payment) {
            this.order = order;
            this.orderItems = orderItems;
            this.payment = payment;
        }

        public Orders getOrder() { return order; }
        public List<OrderItems> getOrderItems() { return orderItems; }
        public Payment getPayment() { return payment; }
    }

    private AddressDTO mapToAddressDTO(DeliveryAddress address) {
        AddressDTO dto = new AddressDTO();
        dto.setFirstName(address.getFirstName());
        dto.setLastName(address.getLastName());
        dto.setPhone(address.getContactNo());
        dto.setPostalCode(address.getPostalCode());

        // 1. Address Line 1
        dto.setAddressLine1(address.getLine1());

        // 2. Address Line 2 (Only if it exists and is not empty)
        if (address.getLine2() != null && !address.getLine2().trim().isEmpty()) {
            dto.setAddressLine2(address.getLine2());
        }

        // 3. City (Prioritize Object ID, fallback to Text)
        if (address.getCity() != null) {
            dto.setCity(address.getCity().getCity());
        } else if (address.getCityText() != null && !address.getCityText().trim().isEmpty()) {
            dto.setCity(address.getCityText());
        } else {
            dto.setCity("");
        }

        // 4. Province (Prioritize Object ID, fallback to Text)
        if (address.getProvince() != null) {
            dto.setState(address.getProvince().getProvince());
        } else if (address.getStateText() != null && !address.getStateText().trim().isEmpty()) {
            dto.setState(address.getStateText());
        } else {
            dto.setState("");
        }

        // 5. Country
        if (address.getCountry() != null) {
            dto.setCountry(address.getCountry().getCountry());
        } else {
            dto.setCountry("Sri Lanka");
        }

        return dto;
    }

    private PaymentDTO mapToPaymentDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        String method = payment.getPaymentsMethod().getMethod();
        dto.setMethodDisplay(method);

        if (method.contains("Credit") || method.contains("Debit") || method.contains("Card")) dto.setMethod("card");
        else if (method.contains("Cash") || method.contains("COD")) dto.setMethod("cod");
        else if (method.contains("Bank")) dto.setMethod("bank");
        else dto.setMethod(method.toLowerCase());

        dto.setStatus(payment.getPaymentStatus().getPaymentStatus());
        dto.setTransactionId(payment.getTransactionId());
        dto.setLastFour("****");
        return dto;
    }

    private OrderItemDTO mapToOrderItemDTO(OrderItems item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setQuantity(item.getQty());

        // --- OPTION 1: Product Variance ---
        if (item.getProductVariance() != null) {
            ProductVariance variant = item.getProductVariance();
            Product product = variant.getProduct();

            dto.setVariantId(variant.getId());
            dto.setProductName(product.getName());

            double regularPrice = variant.getRegularPrice();
            double discountPercentage = variant.getDiscountPercentage() != null ? variant.getDiscountPercentage() : 0.0;
            double price = regularPrice;
            if (discountPercentage > 0) price = regularPrice * (1 - discountPercentage / 100);

            dto.setPrice(price);
            dto.setTotal(price * item.getQty());

            // Build display name
            StringBuilder displayName = new StringBuilder(product.getName());
            if (variant.getSize() != null && variant.getSize().getSize() != null) {
                displayName.append(" (Size: ").append(variant.getSize().getSize()).append(")");
                dto.setSize(variant.getSize().getSize());
            }
            if (variant.getColor() != null && variant.getColor().getColor() != null) {
                displayName.append(" - ").append(variant.getColor().getColor());
                dto.setColor(variant.getColor().getColor());
            }
            if (variant.getGemstone() != null && variant.getGemstone().getGemStone() != null) {
                displayName.append(" with ").append(variant.getGemstone().getGemStone());
                dto.setGemstone(variant.getGemstone().getGemStone());
            }

            dto.setDisplayName(displayName.toString());

            // ✅ CHANGE: Use Cloudinary URL directly
            dto.setImage(product.getImage1());
        }
        // --- OPTION 2: Collection ---
        else if (item.getCollection() != null) {
            Collection col = item.getCollection();
            dto.setVariantId(col.getId()); // Use collection ID
            dto.setProductName(col.getTitle() + " (Collection)");

            double price = col.getPrice(); // Assuming this is final price
            dto.setPrice(price);
            dto.setTotal(price * item.getQty());

            dto.setDisplayName("Collection: " + col.getTitle());

            // Set fields to generic or empty for Collections
            dto.setSize("N/A");
            dto.setColor("N/A");
            dto.setGemstone("N/A");

            // ✅ CHANGE: Use Cloudinary URL directly
            dto.setImage(col.getImage1());
        }

        return dto;
    }

    private String calculateExpectedDelivery(Date orderDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(orderDate);
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");
        return dateFormat.format(calendar.getTime());
    }
}