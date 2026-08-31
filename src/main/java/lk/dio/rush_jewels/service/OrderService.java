package lk.dio.rush_jewels.service;

import jakarta.servlet.http.HttpSession;
import lk.dio.rush_jewels.dto.CheckoutAddressDTO;
import lk.dio.rush_jewels.dto.CheckoutRequestDTO;
import lk.dio.rush_jewels.dto.PayHereRequestDTO;
import lk.dio.rush_jewels.model.*;
import lk.dio.rush_jewels.model.Collection;
import lk.dio.rush_jewels.repository.*;
import lk.dio.rush_jewels.util.CustomPayloadEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Value("${payhere.merchant.id}")
    private String merchantId;
    @Value("${payhere.currency}")
    private String payHereCurrency;

    private static final DecimalFormat AMOUNT_FORMAT = new DecimalFormat("0.00");

    private final CheckoutAddressService checkoutAddressService;
    private final OrdersRepository ordersRepo;
    private final CartRepository cartRepo;
    private final OrderItemsRepository orderItemsRepo;
    private final PaymentMethodRepository paymentMethodRepo;
    private final ShippingRepository shippingRepo;
    private final PaymentService paymentService;
    private final ProductVarianceRepository productVarianceRepo;
    private final CollectionRepository collectionRepo;
    private final StockRepository stockRepo;
    private final PaymentStatusRepository paymentStatusRepo;
    private final PaymentRepository paymentRepo;
    private final OrderStatusRepository orderStatusRepo;
    private final StockStatusRepository stockStatusRepo;
    private final DiscountService discountService;
    private final UserService userService;
    private final SystemSettingRepository systemSettingRepo;

    public OrderService(CheckoutAddressService checkoutAddressService, OrdersRepository ordersRepo, CartRepository cartRepo, OrderItemsRepository orderItemsRepo, PaymentMethodRepository paymentMethodRepo, ShippingRepository shippingRepo, PaymentService paymentService, CountryRepository countryRepo, CityRepository cityRepo, ProductVarianceRepository productVarianceRepo, CollectionRepository collectionRepo, StockRepository stockRepo, PaymentStatusRepository paymentStatusRepo, PaymentRepository paymentRepo, OrderStatusRepository orderStatusRepo, StockStatusRepository stockStatusRepo, DiscountService discountService, UserService userService, SystemSettingRepository systemSettingRepo) {
        this.checkoutAddressService = checkoutAddressService;
        this.ordersRepo = ordersRepo;
        this.cartRepo = cartRepo;
        this.orderItemsRepo = orderItemsRepo;
        this.paymentMethodRepo = paymentMethodRepo;
        this.shippingRepo = shippingRepo;
        this.paymentService = paymentService;
        this.productVarianceRepo = productVarianceRepo;
        this.collectionRepo = collectionRepo;
        this.stockRepo = stockRepo;
        this.paymentStatusRepo = paymentStatusRepo;
        this.paymentRepo = paymentRepo;
        this.orderStatusRepo = orderStatusRepo;
        this.stockStatusRepo = stockStatusRepo;
        this.discountService = discountService;
        this.userService = userService;
        this.systemSettingRepo = systemSettingRepo;
    }

    // ====================================================================
    // PROCESS ORDER (CART)
    // ====================================================================
    @Transactional
    public Object processOrder(User user, CheckoutRequestDTO requestDTO) {
        validateRequest(user, requestDTO);
        calculateFinalTotal(user, requestDTO, null, null, null);

        if (Boolean.TRUE.equals(requestDTO.getSubscribed()) && !user.isSubscribed()) {
            userService.updateSubscriptionStatus(user.getEmail(), true);
        }

        List<Cart> cartItems = cartRepo.findByUser(user);
        if (cartItems.isEmpty()) throw new IllegalStateException("Cart is empty.");

        String itemsDisplay = cartItems.stream()
                .map(c -> (c.getProductVariance() != null ? c.getProductVariance().getProduct().getName() : "Collection: " + c.getCollection().getTitle()) + " (" + c.getQty() + ")")
                .collect(Collectors.joining(", "));
        requestDTO.setItemsDisplay(itemsDisplay);

        DeliveryAddress shippingAddress = checkoutAddressService.saveShippingAddressDuringCheckout(user, toCheckoutAddressDTOFromRequest(requestDTO));
        DeliveryAddress billingAddress = handleBillingAddress(user, requestDTO, shippingAddress);

        if ("cod".equalsIgnoreCase(requestDTO.getSelectedPaymentMethod()) || "bank".equalsIgnoreCase(requestDTO.getSelectedPaymentMethod())) {
            Orders order = createAndFinalizeOrder(user, requestDTO, shippingAddress, "Order Placed");
            completeOrderPostActions(order, user, requestDTO, "PENDING_PAYMENT", null);
            if ("bank".equalsIgnoreCase(requestDTO.getSelectedPaymentMethod()) && requestDTO.getBankSlipUrl() != null) {
                order.setSlipUrl(requestDTO.getBankSlipUrl());
                ordersRepo.save(order);
            }
            return order;
        }

        return buildPayHereRequest(requestDTO, user, shippingAddress, billingAddress, generateCustomOrderId(), null, null, null);
    }

    @Transactional
    public void processSuccessfulPaymentOrder(User user, CheckoutRequestDTO requestDTO, String payHereOrderId, String transactionId) {
        calculateFinalTotal(user, requestDTO, null, null, null);
        DeliveryAddress shippingAddress = checkoutAddressService.saveShippingAddressDuringCheckout(user, toCheckoutAddressDTOFromRequest(requestDTO));
        handleBillingAddress(user, requestDTO, shippingAddress);

        if (Boolean.TRUE.equals(requestDTO.getSubscribed()) && !user.isSubscribed()) {
            userService.updateSubscriptionStatus(user.getEmail(), true);
        }

        Orders order = createAndFinalizeOrder(user, requestDTO, shippingAddress, "Order Placed", payHereOrderId);
        completeOrderPostActions(order, user, requestDTO, "COMPLETED", transactionId);
    }

    @Transactional
    public Object processBuyNowOrder(User user, CheckoutRequestDTO requestDTO, Integer variantId, Integer collectionId, Integer quantity, HttpSession session) {
        validateRequest(user, requestDTO);
        calculateFinalTotal(user, requestDTO, variantId, collectionId, quantity);

        if (Boolean.TRUE.equals(requestDTO.getSubscribed()) && !user.isSubscribed()) {
            userService.updateSubscriptionStatus(user.getEmail(), true);
        }

        String itemsDisplay = variantId != null ?
                productVarianceRepo.findById(variantId).get().getProduct().getName() + " (" + quantity + ")" :
                "Collection: " + collectionRepo.findById(collectionId).get().getTitle() + " (" + quantity + ")";
        requestDTO.setItemsDisplay(itemsDisplay);

        DeliveryAddress shippingAddress = checkoutAddressService.saveShippingAddressDuringCheckout(user, toCheckoutAddressDTOFromRequest(requestDTO));
        DeliveryAddress billingAddress = handleBillingAddress(user, requestDTO, shippingAddress);

        if ("cod".equalsIgnoreCase(requestDTO.getSelectedPaymentMethod()) || "bank".equalsIgnoreCase(requestDTO.getSelectedPaymentMethod())) {
            Orders order = createAndFinalizeOrder(user, requestDTO, shippingAddress, "Order Placed");
            completeBuyNowOrderPostActions(order, user, requestDTO, "PENDING_PAYMENT", null, variantId, collectionId, quantity, session);
            if ("bank".equalsIgnoreCase(requestDTO.getSelectedPaymentMethod()) && requestDTO.getBankSlipUrl() != null) {
                order.setSlipUrl(requestDTO.getBankSlipUrl());
                ordersRepo.save(order);
            }
            return order;
        }

        return buildPayHereRequest(requestDTO, user, shippingAddress, billingAddress, generateCustomOrderId(), variantId, collectionId, quantity);
    }

    @Transactional
    public void processSuccessfulBuyNowPaymentOrder(User user, CheckoutRequestDTO requestDTO, String payHereOrderId,
                                                    String transactionId, Integer variantId, Integer collectionId, Integer quantity, HttpSession session) {
        calculateFinalTotal(user, requestDTO, variantId, collectionId, quantity);
        DeliveryAddress shippingAddress = checkoutAddressService.saveShippingAddressDuringCheckout(user, toCheckoutAddressDTOFromRequest(requestDTO));
        handleBillingAddress(user, requestDTO, shippingAddress);

        if (Boolean.TRUE.equals(requestDTO.getSubscribed()) && !user.isSubscribed()) {
            userService.updateSubscriptionStatus(user.getEmail(), true);
        }

        Orders order = createAndFinalizeOrder(user, requestDTO, shippingAddress, "Order Placed", payHereOrderId);
        completeBuyNowOrderPostActions(order, user, requestDTO, "COMPLETED", transactionId, variantId, collectionId, quantity, session);
    }

    // ====================================================================
    // COMPLETION LOGIC
    // ====================================================================

    private void completeOrderPostActions(Orders order, User user, CheckoutRequestDTO requestDTO, String paymentStatus, String transactionId) {
        List<Cart> cartItems = cartRepo.findByUser(user);
        if (cartItems.isEmpty()) throw new IllegalStateException("Cart empty.");

        for (Cart item : cartItems) {
            OrderItems orderItem = new OrderItems();
            orderItem.setOrders(order);
            orderItem.setQty(item.getQty());

            if (item.getProductVariance() != null) {
                ProductVariance pv = item.getProductVariance();
                processStockDeduction(pv, item.getQty());
                orderItem.setProductVariance(pv);
            } else if (item.getCollection() != null) {
                Collection col = collectionRepo.findLockedById(item.getCollection().getId())
                        .orElseThrow(() -> new IllegalStateException("Collection not found"));

                processCollectionStockDeduction(col, item.getQty());
                orderItem.setCollection(col);
            }
            orderItemsRepo.save(orderItem);
        }
        cartRepo.deleteByUser(user);

        if (requestDTO.getCouponCode() != null && !requestDTO.getCouponCode().isEmpty()) {
            discountService.recordUsage(requestDTO.getCouponCode(), user.getId(), order.getId());
        }

        savePaymentRecord(order, user, requestDTO, paymentStatus, transactionId);
    }

    private void completeBuyNowOrderPostActions(Orders order, User user, CheckoutRequestDTO requestDTO,
                                                String paymentStatus, String transactionId,
                                                Integer variantId, Integer collectionId, Integer quantity, HttpSession session) {
        OrderItems orderItem = new OrderItems();
        orderItem.setOrders(order);
        orderItem.setQty(quantity);

        if (variantId != null) {
            ProductVariance pv = productVarianceRepo.findById(variantId).orElseThrow();
            processStockDeduction(pv, quantity);
            orderItem.setProductVariance(pv);
        } else if (collectionId != null) {
            Collection col = collectionRepo.findLockedById(collectionId)
                    .orElseThrow(() -> new IllegalStateException("Collection not found: " + collectionId));

            processCollectionStockDeduction(col, quantity);
            orderItem.setCollection(col);
        }

        orderItemsRepo.save(orderItem);

        if (session != null) {
            session.removeAttribute("buyNowVariantId");
            session.removeAttribute("buyNowCollectionId");
            session.removeAttribute("buyNowQuantity");
        }

        if (requestDTO.getCouponCode() != null && !requestDTO.getCouponCode().isEmpty()) {
            discountService.recordUsage(requestDTO.getCouponCode(), user.getId(), order.getId());
        }

        savePaymentRecord(order, user, requestDTO, paymentStatus, transactionId);
    }

    // ====================================================================
    // STOCK LOGIC (WAREHOUSE 1)
    // ====================================================================

    private void processStockDeduction(ProductVariance pv, int quantityRequired) {
        List<Stock> stocks = stockRepo.findLockedByProductVarianceAndWarehouse1(pv);
        deductFromStockList(stocks, quantityRequired, pv.getStockLimit(), "product variant: " + pv.getId());
    }

    private void processCollectionStockDeduction(Collection col, int quantityRequired) {
        List<Stock> stocks = stockRepo.findLockedByCollectionAndWarehouse1(col);
        deductFromStockList(stocks, quantityRequired, col.getStockLimit(), "collection: " + col.getId());
        col.setStockLimit(col.getStockLimit() - quantityRequired);
        collectionRepo.save(col);
    }

    private void deductFromStockList(List<Stock> stocks, int quantityRequired, int stockLimit, String entityDesc) {
        if (stocks.isEmpty()) {
            throw new IllegalStateException("Stock not found in Warehouse 1 for " + entityDesc);
        }

        int totalAvailable = stocks.stream().mapToInt(Stock::getQty).sum();
        if (totalAvailable < quantityRequired) {
            throw new IllegalStateException("Insufficient stock for " + entityDesc + ". Available: " + totalAvailable + ", Required: " + quantityRequired);
        }

        int remainingToDeduct = quantityRequired;
        for (Stock stock : stocks) {
            if (remainingToDeduct <= 0) break;

            int currentQty = stock.getQty();
            if (currentQty >= remainingToDeduct) {
                stock.setQty(currentQty - remainingToDeduct);
                remainingToDeduct = 0;
            } else {
                stock.setQty(0);
                remainingToDeduct -= currentQty;
            }
            updateStockStatus(stock, stockLimit);
        }
    }

    private void updateStockStatus(Stock stock, int limit) {
        int qty = stock.getQty();
        int status = qty == 0 ? 3 : (qty > limit ? 2 : 1);
        StockStatus newStatus = stockStatusRepo.findById(status).orElseThrow();
        if(stock.getStockStatus().getId() != status) {
            stock.setStockStatus(newStatus);
            stockRepo.save(stock);
        }
    }

    // ... (Helpers) ...
    private double calculateSecureSubtotal(User user, Integer variantId, Integer collectionId, Integer qty) {
        double subtotal = 0.0;
        if (variantId != null && qty != null) {
            ProductVariance pv = productVarianceRepo.findById(variantId).orElse(null);
            if(pv != null) {
                double discount = pv.getDiscountPercentage() != null ? pv.getDiscountPercentage() : 0;
                subtotal = (pv.getRegularPrice() * (1 - discount / 100)) * qty;
            }
        } else if (collectionId != null && qty != null) {
            Collection col = collectionRepo.findById(collectionId).orElse(null);
            if(col != null) subtotal = col.getPrice() * qty;
        } else {
            List<Cart> cartItems = cartRepo.findByUser(user);
            for (Cart c : cartItems) {
                if (c.getProductVariance() != null) {
                    double discount = c.getProductVariance().getDiscountPercentage() != null ? c.getProductVariance().getDiscountPercentage() : 0;
                    subtotal += (c.getProductVariance().getRegularPrice() * (1 - discount / 100)) * c.getQty();
                } else if (c.getCollection() != null) {
                    subtotal += c.getCollection().getPrice() * c.getQty();
                }
            }
        }
        return subtotal;
    }

    private double getTaxRate() {
        return 0.0;
    }

    private double calculateFinalTotal(User user, CheckoutRequestDTO dto, Integer variantId, Integer collectionId, Integer qty) { 
        double subtotal = calculateSecureSubtotal(user, variantId, collectionId, qty);
        dto.setCartSubtotal(subtotal); // Force correct DB price to DTO

        // Securely calculate tax
        double tax = subtotal * getTaxRate();
        dto.setTaxAmount(tax); // Override malicious payload

        // Securely calculate shipping
        double shipping = 0.0;
        if (dto.getShippingMethodName() != null && !dto.getShippingMethodName().isEmpty()) {
            Shipping dbShip = shippingRepo.findByShippingMethod(dto.getShippingMethodName()).orElse(null);
            if (dbShip != null) {
                shipping = dbShip.getValue();
            } else if (dto.getShippingMethodName().contains("BOPIS")) {
                shipping = 0.0; // BOPIS is free
            } else if (dto.getShippingMethodName().contains("Flat Rate")) {
                shipping = 400.0; // Hardcoded flat rate
            }
        }
        dto.setShippingCost(shipping); // Override malicious payload
        dto.setSelectedShippingMethodValue(String.valueOf(shipping));

        double discount = 0.0; 
        if(dto.getCouponCode() != null && !dto.getCouponCode().isEmpty()) { 
            try { discount = discountService.calculateDiscount(dto.getCouponCode(), subtotal); } catch(Exception e) {} 
        } 
        dto.setDiscountAmount(discount); 
        
        double finalPrice = Math.max(0, subtotal + shipping + tax - discount);
        dto.setFinalTotal(finalPrice); // Override malicious payload
        return finalPrice; 
    }

    private PayHereRequestDTO buildPayHereRequest(CheckoutRequestDTO requestDTO, User user, DeliveryAddress shipping, DeliveryAddress billing, String orderId, Integer variantId, Integer collectionId, Integer qty) {
        PayHereRequestDTO req = new PayHereRequestDTO();
        req.setMerchantId(merchantId);
        req.setOrderId(orderId);
        req.setAmount(requestDTO.getFinalTotal());
        req.setCurrency(payHereCurrency);
        req.setFirstName(Optional.ofNullable(requestDTO.getFirstName()).orElse(user.getFname()));
        req.setLastName(Optional.ofNullable(requestDTO.getLastName()).orElse(user.getLname()));
        req.setEmail(Optional.ofNullable(requestDTO.getEmail()).orElse(user.getEmail()));
        req.setPhone(Optional.ofNullable(requestDTO.getContactNo()).orElse(""));
        req.setAddress(Optional.ofNullable(requestDTO.getAddressLine1()).orElse(""));
        req.setCity(requestDTO.getCityOther());
        req.setCountry("Sri Lanka");

        String items = Optional.ofNullable(requestDTO.getItemsDisplay()).orElse("Jewelry Items");
        if (items.length() > 250) items = items.substring(0, 247) + "...";
        req.setItems(items);

        if (billing != null) {
            req.setBillingFirstName(billing.getFirstName());
            req.setBillingLastName(billing.getLastName());
            req.setBillingAddress(Optional.ofNullable(billing.getLine1()).orElse(""));
            req.setBillingCity(Optional.ofNullable(billing.getCityText()).orElse(""));
            req.setBillingCountry(Optional.ofNullable(billing.getCountry()).map(Country::getCountry).orElse(""));
        } else {
            req.setBillingFirstName(req.getFirstName());
            req.setBillingLastName(req.getLastName());
            req.setBillingAddress(req.getAddress());
            req.setBillingCity(req.getCity());
            req.setBillingCountry(req.getCountry());
        }

        String vId = variantId!=null?String.valueOf(variantId):"";
        String cId=collectionId!=null?String.valueOf(collectionId):"";
        String q=qty!=null?String.valueOf(qty):"";

        // ✅ ADDED: isGift flag to payload (Index 14)
        String isGift = Boolean.TRUE.equals(requestDTO.getIsGift()) ? "1" : "0";

        String internalPayload = CustomPayloadEncoder.encode(
                String.valueOf(user.getId()),
                requestDTO.getSelectedPaymentMethod(),
                AMOUNT_FORMAT.format(requestDTO.getCartSubtotal()),
                AMOUNT_FORMAT.format(requestDTO.getShippingCost()),
                AMOUNT_FORMAT.format(requestDTO.getTaxAmount()),
                AMOUNT_FORMAT.format(requestDTO.getDiscountAmount()),
                requestDTO.getSelectedShippingMethodValue(),
                shipping!=null?String.valueOf(shipping.getId()):"",
                billing!=null?String.valueOf(billing.getId()):"",
                requestDTO.getOrderNotes(),
                vId, q, cId,
                Optional.ofNullable(requestDTO.getCouponCode()).orElse(""),
                isGift // New param
        );

        req.setCustom1(internalPayload);
        req.setHash(paymentService.calculatePayHereHash(req));
        return req;
    }

    private DeliveryAddress handleBillingAddress(User user, CheckoutRequestDTO dto, DeliveryAddress shipping) { if (Boolean.TRUE.equals(dto.getDifferentBilling())) return checkoutAddressService.saveBillingAddressIfNeeded(user, toCheckoutAddressDTOFromBilling(dto), shipping, true); return checkoutAddressService.saveBillingAddressIfNeeded(user, null, shipping, false); }

    private void validateRequest(User user, CheckoutRequestDTO dto) {
        if (user == null) throw new IllegalArgumentException("User null");
        if (!Boolean.TRUE.equals(dto.getAgreeTerms())) throw new IllegalArgumentException("You must agree to the Terms & Conditions.");
        if (dto.getSelectedPaymentMethod() == null) throw new IllegalArgumentException("Payment required");
        boolean isStorePickup = dto.getShippingMethodName() != null && dto.getShippingMethodName().contains("BOPIS");
        if (!isStorePickup) {
            if (dto.getAddressLine1() == null || dto.getAddressLine1().trim().isEmpty()) throw new IllegalArgumentException("Shipping Address is required.");
            if (dto.getCityId() == null && (dto.getCityOther() == null || dto.getCityOther().trim().isEmpty())) throw new IllegalArgumentException("City is required.");
            if (dto.getCountryId() == null) throw new IllegalArgumentException("Country is required.");
        }
        if (dto.getContactNo() == null || dto.getContactNo().trim().isEmpty()) throw new IllegalArgumentException("Phone number is required.");
    }

    private Orders createAndFinalizeOrder(User user, CheckoutRequestDTO dto, DeliveryAddress shipping, String status) { 
        return createAndFinalizeOrder(user, dto, shipping, status, generateCustomOrderId()); 
    }

    private Orders createAndFinalizeOrder(User user, CheckoutRequestDTO dto, DeliveryAddress shipping, String status, String id) {
        String finalStatus = (dto.getShippingMethodName() != null && dto.getShippingMethodName().contains("BOPIS")) ? "PENDING_PICKUP" : status;
        Orders order = new Orders();
        order.setId(id);
        order.setUser(user);
        order.setOrderedAt(new Date());
        order.setDeliveryAddress(shipping);
        order.setOrderNote(dto.getOrderNotes());
        order.setGift(Boolean.TRUE.equals(dto.getIsGift())); // Sets gift status
        order.setOrderStatus(orderStatusRepo.findByOrderStatus(finalStatus).orElseGet(() -> {
            OrderStatus newStatus = new OrderStatus();
            newStatus.setOrderStatus(finalStatus);
            return orderStatusRepo.save(newStatus);
        }));
        
        // Securely link shipping (values are forced safe by calculateFinalTotal)
        Double shipVal = dto.getSelectedShippingMethodValue() != null ? Double.parseDouble(dto.getSelectedShippingMethodValue()) : 0.0;
        String shipName = dto.getShippingMethodName() != null && !dto.getShippingMethodName().isEmpty() ? dto.getShippingMethodName() : "Standard";
        
        order.setShipping(shippingRepo.findByShippingMethod(shipName).orElseGet(() -> {
            return shippingRepo.findByValue(shipVal).orElseGet(() -> {
                Shipping newShip = new Shipping();
                newShip.setShippingMethod(shipName);
                newShip.setValue(shipVal);
                newShip.setDescription(shipName + " Delivery");
                newShip.setStatus(1);
                return shippingRepo.save(newShip);
            });
        }));
        return ordersRepo.save(order);
    }

    private void savePaymentRecord(Orders order, User user, CheckoutRequestDTO dto, String status, String txnId) {
        Payment p = new Payment();
        p.setCreatedAt(new Date());
        p.setOrders(order);
        p.setTransactionId(txnId != null ? txnId : order.getId());
        p.setUser(user);
        p.setPaymentStatus(paymentStatusRepo.findByPaymentStatus(status).orElseThrow());
        String methodKey = dto.getSelectedPaymentMethod().toLowerCase();
        String dbMethod = methodKey.contains("cod") ? "Cash on Delivery" : (methodKey.contains("bank") ? "Bank Transfer" : "Credit / Debit Card");
        p.setPaymentsMethod(paymentMethodRepo.findByMethod(dbMethod).orElseThrow());
        p.setSubTotal(dto.getCartSubtotal());
        p.setTax(dto.getTaxAmount() != null ? dto.getTaxAmount() : 0.0);
        p.setDiscount(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : 0.0);
        double finalTotal = dto.getFinalTotal() != null ? dto.getFinalTotal() : 0.0;
        p.setFinalTotal(finalTotal);
        paymentRepo.save(p);
    }

    private String generateCustomOrderId() { String prefix = "RJ-" + YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM")); Optional<String> lastId = ordersRepo.findLastOrderIdByPrefix(prefix + "-%"); long seq = 1; if(lastId.isPresent()) { String[] parts = lastId.get().split("-"); if(parts.length == 4) seq = Long.parseLong(parts[3]) + 1; } return prefix + "-" + String.format("%05d", seq); }

    // ✅ FIX: Added setStateText/setCityText properly mapping from 'Other' fields to ensure address duplication check works
    private CheckoutAddressDTO toCheckoutAddressDTOFromRequest(CheckoutRequestDTO req) {
        CheckoutAddressDTO d = new CheckoutAddressDTO();
        d.setFirstName(req.getFirstName());
        d.setLastName(req.getLastName());
        d.setContactNo(req.getContactNo());
        d.setAddressLine1(req.getAddressLine1());
        d.setAddressLine2(req.getAddressLine2());
        d.setPostalCode(req.getPostalCode());
        d.setCityId(req.getCityId());
        d.setProvinceId(req.getProvinceId());
        d.setCountryId(req.getCountryId());
        d.setCityText(req.getCityOther());
        d.setStateText(req.getProvinceOther()); // Added this line
        d.setDefaultAddress(Optional.ofNullable(req.getSaveAddress()).orElse(false));
        return d;
    }

    private CheckoutAddressDTO toCheckoutAddressDTOFromBilling(CheckoutRequestDTO req) {
        CheckoutAddressDTO d = new CheckoutAddressDTO();
        d.setFirstName(req.getBillingFirstName());
        d.setLastName(req.getBillingLastName());
        d.setContactNo(req.getBillingContactNo());
        d.setAddressLine1(req.getBillingAddressLine1());
        d.setAddressLine2(req.getBillingAddressLine2());
        d.setPostalCode(req.getBillingPostalCode());
        d.setCityId(req.getBillingCityId());
        d.setProvinceId(req.getBillingProvinceId());
        d.setCountryId(req.getBillingCountryId());
        d.setCityText(req.getBillingCityOther());
        d.setStateText(req.getBillingProvinceOther()); // Added this line
        return d;
    }
}