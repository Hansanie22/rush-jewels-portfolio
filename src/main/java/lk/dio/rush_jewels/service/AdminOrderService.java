package lk.dio.rush_jewels.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.dto.OrderDetailDTO;
import lk.dio.rush_jewels.dto.OrderListDTO;
import lk.dio.rush_jewels.dto.ReturnListDTO;
import lk.dio.rush_jewels.model.*;
import lk.dio.rush_jewels.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminOrderService {

    private final OrdersRepository ordersRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final PaymentRepository paymentRepository;
    private final ReturnRepository returnRepository;
    private final ReturnTypeRepository returnTypeRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final StockRepository stockRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockStatusRepository stockStatusRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final ReturnItemsRepository returnItemsRepository;
    private final OrderEmailService orderEmailService;

    private static final int WAREHOUSE_MAIN_ID = 1;
    private static final int STOCK_STATUS_IN_STOCK_ID = 1;
    private static final int STOCK_STATUS_OUT_OF_STOCK_ID = 2;

    public AdminOrderService(OrdersRepository ordersRepository,
                             OrderItemsRepository orderItemsRepository,
                             PaymentRepository paymentRepository,
                             ReturnRepository returnRepository,
                             ReturnTypeRepository returnTypeRepository,
                             OrderStatusRepository orderStatusRepository,
                             AdminAuditLogRepository auditLogRepository,
                             ObjectMapper objectMapper,
                             StockRepository stockRepository,
                             WarehouseRepository warehouseRepository,
                             StockStatusRepository stockStatusRepository,
                             PaymentStatusRepository paymentStatusRepository,
                             ReturnItemsRepository returnItemsRepository,
                             OrderEmailService orderEmailService) {
        this.ordersRepository = ordersRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.paymentRepository = paymentRepository;
        this.returnRepository = returnRepository;
        this.returnTypeRepository = returnTypeRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
        this.stockRepository = stockRepository;
        this.warehouseRepository = warehouseRepository;
        this.stockStatusRepository = stockStatusRepository;
        this.paymentStatusRepository = paymentStatusRepository;
        this.returnItemsRepository = returnItemsRepository;
        this.orderEmailService = orderEmailService;
    }

    public List<OrderListDTO> getActiveOrders() {
        seedReturnTypes();
        return ordersRepository.findAllByOrderByOrderedAtDesc().stream()
                .filter(o -> !returnRepository.existsByOrders_Id(o.getId()))
                .map(o -> {
                    double total = calculateOrderTotal(o.getId());
                    String payStatus = getPaymentStatus(o.getId());
                    return new OrderListDTO(
                            o.getId(),
                            o.getUser().getFname() + " " + o.getUser().getLname(),
                            o.getUser().getEmail(),
                            o.getOrderedAt(),
                            total,
                            payStatus,
                            o.getOrderStatus().getOrderStatus(),
                            false, null, null, null,
                            getPaymentMethod(o.getId())
                    );
                }).collect(Collectors.toList());
    }

    public List<ReturnListDTO> getAllReturns() {
        return returnRepository.findAll().stream()
                .map(r -> new ReturnListDTO(
                        r.getId(),
                        r.getOrders().getId(),
                        r.getOrders().getUser().getFname() + " " + r.getOrders().getUser().getLname(),
                        r.getReturnType().getReturnType(),
                        r.getReturnReason(),
                        mapReturnStatus(r.getStatus()),
                        r.getRequestDate().toString()
                ))
                .collect(Collectors.toList());
    }

    public OrderDetailDTO getOrderDetails(String id) {
        // 1. මුලින්ම Order එක සොයන්න. නැත්නම් Return ID එකක් හරහා Order එක සොයන්න.
        Orders o = ordersRepository.findById(id).orElse(null);
        if (o == null) {
            Optional<Return> r = returnRepository.findById(id);
            if (r.isPresent()) o = r.get().getOrders();
            else throw new RuntimeException("Order/Return not found");
        }

        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setOrderId(o.getId());
        dto.setDate(o.getOrderedAt());
        dto.setNotes(o.getOrderNote());
        dto.setGift(o.isGift());
        dto.setCustomerName(o.getUser().getFname() + " " + o.getUser().getLname());
        dto.setEmail(o.getUser().getEmail());

        // 2. ලිපිනය සැකසීම (Address Construction)
        DeliveryAddress da = o.getDeliveryAddress();
        List<String> addressParts = new ArrayList<>();
        if (da.getLine1() != null && !da.getLine1().trim().isEmpty()) addressParts.add(da.getLine1().trim());
        if (da.getLine2() != null && !da.getLine2().trim().isEmpty()) addressParts.add(da.getLine2().trim());
        String city = (da.getCity() != null) ? da.getCity().getCity() : da.getCityText();
        if (city != null) addressParts.add(city.trim());
        String province = (da.getProvince() != null) ? da.getProvince().getProvince() : da.getStateText();
        if (province != null) addressParts.add(province.trim());
        if (da.getCountry() != null) addressParts.add(da.getCountry().getCountry());

        dto.setAddress(String.join(", ", addressParts));
        dto.setPhone(da.getContactNo());
        dto.setDeliveryStatus(o.getOrderStatus().getOrderStatus());
        dto.setSlipUrl(o.getSlipUrl());

        // 3. ගෙවීම් විස්තර (Payment Summary)
        Optional<Payment> paymentOpt = paymentRepository.findByOrders_Id(o.getId());
        double shippingCost = (o.getShipping() != null) ? o.getShipping().getValue() : 0.0;
        dto.setShipping(shippingCost);

        if (paymentOpt.isPresent()) {
            Payment p = paymentOpt.get();
            dto.setSubTotal(p.getSubTotal());
            dto.setTax(p.getTax() != null ? p.getTax() : 0.0);
            dto.setDiscount(p.getDiscount() != null ? p.getDiscount() : 0.0);
            dto.setTotal(p.getFinalTotal());
            dto.setPaymentMethod(p.getPaymentsMethod().getMethod());
            dto.setPaymentStatus(p.getPaymentStatus().getPaymentStatus());
        }

        // 4. Return කර ඇති භාණ්ඩ හඳුනා ගැනීම (Identifying Returned Items) ✅
        List<Integer> returnedOrderItemIds = new ArrayList<>();
        returnRepository.findByOrders_Id(o.getId()).ifPresent(r -> {
            // පාරිභෝගිකයා Return එක සඳහා තේරූ සියලුම Items වල IDs ලබා ගනී
            returnedOrderItemIds.addAll(
                    returnItemsRepository.findByReturns_Id(r.getId()).stream()
                            .map(ri -> ri.getOrderItems().getId())
                            .collect(Collectors.toList())
            );
        });

        // 5. භාණ්ඩ ලැයිස්තුව සැකසීම (Item List Mapping)
        List<OrderItems> items = orderItemsRepository.findByOrders_Id(o.getId());
        dto.setItems(items.stream().map(i -> {
            String name = "Unknown Product";
            double price = 0.0;
            String subtext = null;
            if (i.getProductVariance() != null) {
                name = i.getProductVariance().getProduct().getName();
                price = i.getProductVariance().getPrice() != null ? i.getProductVariance().getPrice() : i.getProductVariance().getRegularPrice();
                
                List<String> varianceDetails = new ArrayList<>();
                if (i.getProductVariance().getSize() != null && i.getProductVariance().getSize().getSize() != null) varianceDetails.add("Size: " + i.getProductVariance().getSize().getSize());
                if (i.getProductVariance().getColor() != null && i.getProductVariance().getColor().getColor() != null) varianceDetails.add("Color: " + i.getProductVariance().getColor().getColor());
                if (i.getProductVariance().getGemstone() != null && i.getProductVariance().getGemstone().getGemStone() != null) varianceDetails.add("Gem: " + i.getProductVariance().getGemstone().getGemStone());
                
                if (!varianceDetails.isEmpty()) {
                    subtext = String.join(" | ", varianceDetails);
                }
            } else if (i.getCollection() != null) {
                name = "Collection: " + i.getCollection().getTitle();
                price = i.getCollection().getPrice();
            }

            OrderDetailDTO.OrderItemDTO itemDto = new OrderDetailDTO.OrderItemDTO(name, "SKU-" + i.getId(), i.getQty(), price, subtext);

            // ✅ අදාළ Item එක Return කර ඇත්නම් එය Marked කරයි
            itemDto.setReturned(returnedOrderItemIds.contains(i.getId()));

            return itemDto;
        }).collect(Collectors.toList()));

        // 6. Return Header විස්තර ඇතුළත් කිරීම
        returnRepository.findByOrders_Id(o.getId()).ifPresent(r -> {
            dto.setReturn(true);
            dto.setReturnType(r.getReturnType().getReturnType());
            dto.setReturnReason(r.getReturnReason());
            dto.setReturnStatus(mapReturnStatus(r.getStatus()));
            dto.setReturnDate(r.getRequestDate().toString());
        });

        return dto;
    }


    public void updateOrderStatus(String orderId, String newStatusStr) {
        Orders order = ordersRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        final String formattedStatus = java.util.Arrays.stream(newStatusStr.toLowerCase().replace("_", " ").trim().split(" "))
                .map(word -> word.substring(0,1).toUpperCase() + word.substring(1))
                .collect(java.util.stream.Collectors.joining(" "));

        OrderStatus targetStatus = orderStatusRepository.findAll().stream()
                .filter(s -> s.getOrderStatus().equalsIgnoreCase(formattedStatus))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Invalid status: " + formattedStatus));

        // Removed strict ID-based status transition restrictions to allow admin full control over order flows (POS, Delivery, Pickup).

        order.setOrderStatus(targetStatus);
        ordersRepository.save(order);
        
        // Update payment if delivered
        if (formattedStatus.equalsIgnoreCase("Delivered") || formattedStatus.equalsIgnoreCase("Completed")) {
            paymentRepository.findByOrders_Id(orderId).ifPresent(p -> {
                if (p.getCompletedAt() == null) {
                    p.setCompletedAt(new Date());
                    
                    // Also mark PaymentStatus as Completed/Paid if it's not already
                    if (!p.getPaymentStatus().getPaymentStatus().equalsIgnoreCase("COMPLETED") 
                        && !p.getPaymentStatus().getPaymentStatus().equalsIgnoreCase("PAID")) {
                        paymentStatusRepository.findAll().stream()
                            .filter(ps -> ps.getPaymentStatus().equalsIgnoreCase("COMPLETED") || ps.getPaymentStatus().equalsIgnoreCase("PAID"))
                            .findFirst().ifPresent(p::setPaymentStatus);
                    }
                    paymentRepository.save(p);
                }
            });
        }
        
        String msg = "Your order status has been updated to: " + formattedStatus + ".";
        if (formattedStatus.equalsIgnoreCase("Delivered") || formattedStatus.equalsIgnoreCase("Completed")) {
            msg = "Great news! Your order has been delivered successfully. Thank you for shopping with Velora Fine Jewellery.";
        }
        orderEmailService.sendGenericNotificationEmail(order, "Order Update: " + formattedStatus, "Order Status Updated", "Order Update", msg);
    }

    public void handoverOnlinePickup(String orderId) {
        Orders order = ordersRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        updateOrderStatusInternal(orderId, "Completed");
        
        String msg = "Great news! Your online pickup order has been handed over successfully. Thank you for shopping with Velora Fine Jewellery.";
        orderEmailService.sendGenericNotificationEmail(order, "Order Picked Up", "Order Picked Up", "Success", msg);
    }

    public void handleAdminReturnAction(String orderId, String action) {
        Return returnEntity = returnRepository.findByOrders_Id(orderId)
                .orElseThrow(() -> new RuntimeException("Return record not found for Order " + orderId));

        // Capture snapshot before changes
        Object oldSnapshot = sanitizeForAudit(returnEntity);

        switch (action.toUpperCase()) {
            case "APPROVE":
                returnEntity.setStatus(ReturnStatus.APPROVED);
                returnEntity.setApprovedDate(LocalDateTime.now());
                updateOrderStatusInternal(orderId, "RETURN_APPROVED");
                break;
            case "REJECT":
                returnEntity.setStatus(ReturnStatus.REJECTED);
                break;
            case "COMPLETE":
                returnEntity.setStatus(ReturnStatus.COMPLETED);
                updateOrderStatusInternal(orderId, "RETURNED");
                restoreOrderStock(returnEntity.getOrders());
                break;
            default:
                throw new IllegalArgumentException("Invalid action");
        }

        Return savedReturn = returnRepository.save(returnEntity);
        logAction("RETURN_" + action.toUpperCase(), "return", String.valueOf(savedReturn.getId()), oldSnapshot, savedReturn);
        
        String actionStr = action.toUpperCase();
        if (actionStr.equals("APPROVE")) {
            orderEmailService.sendGenericNotificationEmail(returnEntity.getOrders(), "Return Request Approved", "Return Approved", "Update", "Your return request for order " + orderId + " has been approved.");
        } else if (actionStr.equals("REJECT")) {
            orderEmailService.sendGenericNotificationEmail(returnEntity.getOrders(), "Return Request Rejected", "Return Rejected", "Update", "Unfortunately, your return request for order " + orderId + " was rejected.");
        } else if (actionStr.equals("COMPLETE")) {
            orderEmailService.sendGenericNotificationEmail(returnEntity.getOrders(), "Return Completed", "Return Completed", "Success", "Your return for order " + orderId + " has been fully processed and completed.");
        }
    }

    public void processPickupPayment(String orderId) {
        Orders order = ordersRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        Payment payment = paymentRepository.findByOrders_Id(orderId).orElseThrow(() -> new RuntimeException("Payment record not found"));

        PaymentStatus completedStatus = paymentStatusRepository.findAll().stream()
                .filter(ps -> ps.getPaymentStatus().equalsIgnoreCase("COMPLETED") || ps.getPaymentStatus().equalsIgnoreCase("PAID"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Status 'COMPLETED' or 'PAID' not found."));

        OrderStatus deliveredStatus = orderStatusRepository.findAll().stream()
                .filter(os -> os.getOrderStatus().equalsIgnoreCase("COMPLETED") || os.getOrderStatus().equalsIgnoreCase("DELIVERED") || os.getOrderStatus().equalsIgnoreCase("HANDED OVER"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Completed/Delivered status not found."));

        payment.setPaymentStatus(completedStatus);
        payment.setCompletedAt(new Date());
        paymentRepository.save(payment);

        order.setOrderStatus(deliveredStatus);
        ordersRepository.save(order);
        
        orderEmailService.sendGenericNotificationEmail(order, "Order Handed Over", "Order Completed", "Thank You!", "Your store pickup order has been paid and handed over successfully. Thank you for shopping with Velora Fine Jewellery!");
    }

    public void updatePaymentToCompleted(String orderId) {
        Orders order = ordersRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        Payment payment = paymentRepository.findByOrders_Id(orderId).orElseThrow(() -> new RuntimeException("Payment record not found"));

        String method = payment.getPaymentsMethod().getMethod().toUpperCase();
        String delStatus = order.getOrderStatus().getOrderStatus().toUpperCase();

        boolean isCod = method.contains("CASH ON DELIVERY") || method.equals("COD");
        boolean isBank = method.contains("BANK");

        if (!isCod && !isBank) {
            throw new IllegalStateException("Only COD or Bank Transfer orders can be marked manually.");
        }
        
        if (isCod && !delStatus.equals("DELIVERED")) {
            throw new IllegalStateException("COD Order must be Delivered first before marking as Paid.");
        }

        PaymentStatus completedStatus = paymentStatusRepository.findAll().stream()
                .filter(ps -> ps.getPaymentStatus().equalsIgnoreCase("COMPLETED") || ps.getPaymentStatus().equalsIgnoreCase("PAID"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Status 'COMPLETED' not found."));

        // Snapshot of old status for audit
        Object oldSnapshot = sanitizeForAudit(payment);

        payment.setPaymentStatus(completedStatus);
        if (payment.getCompletedAt() == null) {
            payment.setCompletedAt(new Date());
        }
        paymentRepository.save(payment);

        logAction("PAYMENT_MARKED_COMPLETED", "payment", String.valueOf(payment.getId()), oldSnapshot, payment);

        if (isBank) {
            orderEmailService.sendGenericNotificationEmail(order, "Bank Transfer Verified", "Payment Verified", "Great News!", "We have successfully verified your bank transfer for Order " + orderId + ". Your order is now being processed.");
        } else if (isCod) {
            orderEmailService.sendGenericNotificationEmail(order, "Payment Received", "Payment Complete", "Thank You!", "We have received the cash payment for your delivered order " + orderId + ". Thank you for shopping with us.");
        }
    }

    public void processPosReturn(String orderId, lk.dio.rush_jewels.dto.ReturnRequestDTO requestDTO, String action) {
        Orders order = ordersRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

        Return returnReq = new Return();
        returnReq.setId("POS-" + action + "-" + System.currentTimeMillis());
        returnReq.setOrders(order);
        returnReq.setReturnReason(requestDTO.getReason() != null ? requestDTO.getReason() : action);
        
        ReturnType type = returnTypeRepository.findByReturnType(action).orElseGet(() -> {
            ReturnType t = new ReturnType();
            t.setReturnType(action);
            return returnTypeRepository.save(t);
        });
        returnReq.setReturnType(type);
        
        returnReq.setStatus(ReturnStatus.COMPLETED);
        returnReq.setApprovedDate(LocalDateTime.now());
        Return savedReturn = returnRepository.save(returnReq);

        List<OrderItems> orderItems = orderItemsRepository.findByOrders_Id(order.getId());
        if (requestDTO.getSelectedItemNames() != null && !requestDTO.getSelectedItemNames().isEmpty()) {
            for (String selectedName : requestDTO.getSelectedItemNames()) {
                orderItems.stream().filter(oi -> {
                    String name = (oi.getProductVariance() != null)
                            ? oi.getProductVariance().getProduct().getName()
                            : (oi.getCollection() != null ? "Collection: " + oi.getCollection().getTitle() : "");
                    return name.equals(selectedName);
                }).findFirst().ifPresent(match -> {
                    ReturnItems ri = new ReturnItems();
                    ri.setReturns(savedReturn);
                    ri.setOrderItems(match);
                    ri.setQty(match.getQty());
                    returnItemsRepository.save(ri);
                    
                    if (action.equalsIgnoreCase("RETURN") || action.equalsIgnoreCase("EXCHANGE")) {
                        restoreItemStock(match, warehouseRepository.findById(WAREHOUSE_MAIN_ID).orElseThrow());
                    }
                });
            }
        }
        
        if (action.equalsIgnoreCase("RETURN") || action.equalsIgnoreCase("EXCHANGE")) {
            updateOrderStatusInternal(orderId, "RETURNED");
            orderEmailService.sendGenericNotificationEmail(order, "Return Processed", "Return Processed", "Success", "Your return/exchange for order " + orderId + " has been processed in-store.");
        } else if (action.equalsIgnoreCase("WARRANTY")) {
            orderEmailService.sendGenericNotificationEmail(order, "Warranty Claimed", "Warranty Claim Initiated", "Warranty", "Your warranty claim for item(s) in order " + orderId + " has been initiated in-store.");
        }
    }

    private void restoreOrderStock(Orders order) {
        Warehouse warehouse = warehouseRepository.findById(WAREHOUSE_MAIN_ID).orElseThrow();
        orderItemsRepository.findByOrders_Id(order.getId()).forEach(item -> restoreItemStock(item, warehouse));
    }

    private void restoreItemStock(OrderItems item, Warehouse warehouse) {
        Stock stock = (item.getProductVariance() != null)
                ? stockRepository.findByProductVarianceAndWarehouse(item.getProductVariance(), warehouse).orElse(new Stock())
                : stockRepository.findByCollectionAndWarehouse(item.getCollection(), warehouse).orElse(new Stock());

        if (stock.getId() == 0) {
            if (item.getProductVariance() != null) stock.setProductVariance(item.getProductVariance());
            else stock.setCollection(item.getCollection());
            stock.setWarehouse(warehouse);
            stock.setQty(0);
            stock.setStockStatus(stockStatusRepository.findById(STOCK_STATUS_OUT_OF_STOCK_ID).orElseThrow());
        }

        int newQty = stock.getQty() + item.getQty();
        stock.setQty(newQty);
        if (newQty > 0) stock.setStockStatus(stockStatusRepository.findById(STOCK_STATUS_IN_STOCK_ID).orElseThrow());
        stockRepository.save(stock);
    }

    private String mapReturnStatus(ReturnStatus status) {
        return (status == ReturnStatus.RETURN_REQUESTED) ? "RETURN_REQUESTED" : status.toString();
    }

    private void updateOrderStatusInternal(String orderId, String statusStr) {
        String normalized = statusStr.replace("_", " ").trim();
        orderStatusRepository.findAll().stream()
                .filter(s -> s.getOrderStatus().equalsIgnoreCase(normalized))
                .findFirst()
                .ifPresent(status -> {
                    Orders order = ordersRepository.findById(orderId).orElseThrow();
                    order.setOrderStatus(status);
                    ordersRepository.save(order);
                });
    }

    private double calculateOrderTotal(String orderId) {
        return paymentRepository.findByOrders_Id(orderId).map(Payment::getFinalTotal)
                .orElseGet(() -> orderItemsRepository.findByOrders_Id(orderId).stream().mapToDouble(i -> {
                    double p = (i.getProductVariance() != null) ? (i.getProductVariance().getPrice() != null ? i.getProductVariance().getPrice() : i.getProductVariance().getRegularPrice()) : i.getCollection().getPrice();
                    return p * i.getQty();
                }).sum());
    }

    private String getPaymentStatus(String orderId) {
        return paymentRepository.findByOrders_Id(orderId).map(p -> p.getPaymentStatus().getPaymentStatus()).orElse("Pending");
    }

    private String getPaymentMethod(String orderId) {
        return paymentRepository.findByOrders_Id(orderId).map(p -> p.getPaymentsMethod().getMethod()).orElse("Unknown");
    }

    private void seedReturnTypes() {
        if (returnTypeRepository.count() == 0) {
            returnTypeRepository.save(new ReturnType(0, "Customer not picked"));
            returnTypeRepository.save(new ReturnType(0, "Exchange"));
            returnTypeRepository.save(new ReturnType(0, "Adjustment"));
        }
    }

    /**
     * Updated logAction to handle Objects for both old and new values,
     * ensuring proper JSON serialization for the DB.
     */
    private void logAction(String action, String table, String recordId, Object oldValueObj, Object newValueObj) {
        try {
            String oldValJson = convertToJson(oldValueObj);
            String newValJson = convertToJson(newValueObj);
            AdminAuditLog log = new AdminAuditLog(action, table, recordId, oldValJson, newValJson, LocalDateTime.now());
            auditLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Audit Log Failed: " + e.getMessage());
        }
    }

    private String convertToJson(Object object) {
        if (object == null) return "{}";
        // If it's already a String, we must still ensure it's valid JSON (wrapped in quotes)
        if (object instanceof String) {
            try { return objectMapper.writeValueAsString(object); } catch (Exception e) { return "\"{}\""; }
        }
        try {
            return objectMapper.writeValueAsString(sanitizeForAudit(object));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    /**
     * Sanitizes JPA entities into Maps to prevent circular references
     * and "LazyInitializationException" during JSON serialization.
     */
    private Object sanitizeForAudit(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Return) {
            Return r = (Return) obj;
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("status", r.getStatus());
            map.put("orderId", r.getOrders() != null ? r.getOrders().getId() : null);
            return map;
        }
        if (obj instanceof Payment) {
            Payment p = (Payment) obj;
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("status", p.getPaymentStatus() != null ? p.getPaymentStatus().getPaymentStatus() : null);
            map.put("total", p.getFinalTotal());
            return map;
        }
        return obj;
    }
}