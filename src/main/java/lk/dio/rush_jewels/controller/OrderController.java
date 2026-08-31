package lk.dio.rush_jewels.controller;

import jakarta.servlet.http.HttpSession;
import lk.dio.rush_jewels.dto.CheckoutRequestDTO;
import lk.dio.rush_jewels.dto.PayHereRequestDTO;
import lk.dio.rush_jewels.model.*;
import lk.dio.rush_jewels.repository.CollectionRepository;
import lk.dio.rush_jewels.repository.ProductVarianceRepository;
import lk.dio.rush_jewels.repository.SystemSettingRepository;
import lk.dio.rush_jewels.service.CheckoutAddressService;
import lk.dio.rush_jewels.service.OrderService;
import lk.dio.rush_jewels.service.PaymentService;
import lk.dio.rush_jewels.service.UserService;
import lk.dio.rush_jewels.util.CustomPayloadEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final UserService userService;
    private final CheckoutAddressService addressService;
    private final ProductVarianceRepository productVarianceRepo;
    private final CollectionRepository collectionRepo;
    private final SystemSettingRepository systemSettingRepo;

    @Value("${payhere.return.url}")
    private String payHereReturnUrl;
    @Value("${payhere.cancel.url}")
    private String payHereCancelUrl;
    @Value("${payhere.notify.url}")
    private String payHereNotifyUrl;
    @Value("${payhere.sandbox:true}")
    private boolean payHereSandbox;

    // ✅ NOTE: Local Image Base Paths removed.
    // We now use Cloudinary URLs directly from the database.

    private static final String USER_SESSION_KEY = "user";

    public OrderController(OrderService orderService,
                           PaymentService paymentService,
                           UserService userService,
                           CheckoutAddressService addressService,
                           ProductVarianceRepository productVarianceRepo,
                           CollectionRepository collectionRepo,
                           SystemSettingRepository systemSettingRepo) {
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.userService = userService;
        this.addressService = addressService;
        this.productVarianceRepo = productVarianceRepo;
        this.collectionRepo = collectionRepo;
        this.systemSettingRepo = systemSettingRepo;
    }

    private double getTaxRate() {
        try {
            return systemSettingRepo.findByKey("TAX_RATE")
                    .map(setting -> Double.parseDouble(setting.getValue()) / 100.0)
                    .orElse(0.08);
        } catch (Exception e) {
            return 0.08;
        }
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(HttpSession session, @Valid @RequestBody CheckoutRequestDTO requestDTO) {
        User user = (User) session.getAttribute(USER_SESSION_KEY);
        if (user == null) return ResponseEntity.status(401).body(Map.of("status", false, "message", "User not authenticated."));

        try {
            Integer buyNowVariantId = (Integer) session.getAttribute("buyNowVariantId");
            Integer buyNowCollectionId = (Integer) session.getAttribute("buyNowCollectionId");
            Integer buyNowQuantity = (Integer) session.getAttribute("buyNowQuantity");

            if ((buyNowVariantId != null || buyNowCollectionId != null) && buyNowQuantity != null) {
                return processBuyNowOrder(session, user, requestDTO, buyNowVariantId, buyNowCollectionId, buyNowQuantity);
            }

            Object result = orderService.processOrder(user, requestDTO);
            return handleOrderResult(result, requestDTO.getSelectedPaymentMethod());

        } catch (IllegalArgumentException | NoSuchElementException e) {
            return ResponseEntity.status(400).body(Map.of("status", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", false, "message", "Internal Error: " + e.getMessage()));
        }
    }

    private ResponseEntity<?> processBuyNowOrder(HttpSession session, User user, CheckoutRequestDTO requestDTO,
                                                 Integer variantId, Integer collectionId, Integer quantity) {
        try {
            String selectedPayment = Optional.ofNullable(requestDTO.getSelectedPaymentMethod()).orElse("").toLowerCase();
            Object result = orderService.processBuyNowOrder(user, requestDTO, variantId, collectionId, quantity, session);
            return handleOrderResult(result, selectedPayment);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("status", false, "message", "Error processing Buy Now: " + e.getMessage()));
        }
    }

    private ResponseEntity<?> handleOrderResult(Object result, String paymentMethod) {
        if (result instanceof PayHereRequestDTO payHereRequest) {
            return buildPayHereResponse(payHereRequest, paymentMethod != null ? paymentMethod.toUpperCase() : "");
        } else if (result instanceof Orders order) {
            return ResponseEntity.ok(Map.of(
                    "status", true,
                    "paymentType", "COD",
                    "orderId", order.getId(),
                    "message", "Order placed successfully.",
                    "redirectUrl", "/order-confirmation.html?order=" + order.getId()
            ));
        }
        throw new IllegalStateException("Unknown result type");
    }

    @PostMapping("/notify")
    public ResponseEntity<String> handlePayHereNotification(@RequestParam Map<String, String> notificationData, HttpSession session) {
        String orderId = notificationData.get("order_id");
        String statusCode = notificationData.get("status_code");
        String transactionId = notificationData.get("payment_id");
        String internalPayloadEncoded = notificationData.get("custom_1");

        if (orderId == null || statusCode == null || internalPayloadEncoded == null) {
            return ResponseEntity.badRequest().body("Missing data");
        }

        try {
            if (!paymentService.verifyPayHereNotification(notificationData)) {
                return ResponseEntity.ok("Verification failed.");
            }

            if ("2".equals(statusCode)) {
                var decodedOpt = CustomPayloadEncoder.decode(internalPayloadEncoded);
                if (decodedOpt.isEmpty()) throw new IllegalArgumentException("Invalid payload.");

                String[] parts = decodedOpt.get();
                if (parts.length < 13) throw new IllegalArgumentException("Invalid payload length: " + parts.length);

                Integer userId = Integer.parseInt(parts[0].trim());
                User user = userService.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));

                CheckoutRequestDTO tempDto = new CheckoutRequestDTO();
                tempDto.setSelectedPaymentMethod(parts[1].trim());

                double cartSubtotal = parseDoubleSafe(parts[2]);
                double shippingCost = parseDoubleSafe(parts[3]);
                double taxAmount = parseDoubleSafe(parts[4]);
                double discountAmount = parseDoubleSafe(parts[5]);

                tempDto.setCartSubtotal(cartSubtotal);
                tempDto.setShippingCost(shippingCost);
                tempDto.setTaxAmount(taxAmount);
                tempDto.setDiscountAmount(discountAmount);
                tempDto.setFinalTotal(Math.max(cartSubtotal + shippingCost + taxAmount - discountAmount, 0.0));

                tempDto.setSelectedShippingMethodValue(parts[6]);

                if (isNumeric(parts[7])) addressToDtoPopulate(Integer.parseInt(parts[7]), tempDto);
                if (isNumeric(parts[8])) {
                    tempDto.setDifferentBilling(true);
                    addressToDtoPopulateBilling(Integer.parseInt(parts[8]), tempDto);
                }
                tempDto.setOrderNotes(parts[9]);

                String buyNowVariantStr = normalizeId(parts[10]);
                String buyNowQuantityStr = normalizeId(parts[11]);
                String buyNowCollectionStr = normalizeId(parts[12]);

                if (parts.length > 13) tempDto.setCouponCode(normalizeId(parts[13]));

                if (parts.length > 14) {
                    tempDto.setIsGift("1".equals(parts[14]));
                }

                Integer buyNowQuantity = (buyNowQuantityStr != null) ? Integer.parseInt(buyNowQuantityStr) : null;

                tempDto.setAgreeTerms(true);

                if (buyNowVariantStr != null && buyNowQuantity != null) {
                    orderService.processSuccessfulBuyNowPaymentOrder(user, tempDto, orderId, transactionId, Integer.parseInt(buyNowVariantStr), null, buyNowQuantity, session);
                } else if (buyNowCollectionStr != null && buyNowQuantity != null) {
                    orderService.processSuccessfulBuyNowPaymentOrder(user, tempDto, orderId, transactionId, null, Integer.parseInt(buyNowCollectionStr), buyNowQuantity, session);
                } else {
                    orderService.processSuccessfulPaymentOrder(user, tempDto, orderId, transactionId);
                }
            }
            return ResponseEntity.ok("Notification processed.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }

    @PostMapping("/buy-now-session")
    public ResponseEntity<?> setBuyNowInSession(HttpSession session, @RequestBody Map<String, Object> payload) {
        if (session.getAttribute(USER_SESSION_KEY) == null) return ResponseEntity.status(401).body(Map.of("status", false));
        try {
            Integer variantId = (Integer) payload.get("productVariantId");
            Integer collectionId = (Integer) payload.get("collectionId");
            Integer quantity = (Integer) payload.get("quantity");
            if (quantity == null || quantity < 1 || (variantId == null && collectionId == null)) return ResponseEntity.badRequest().body(Map.of("status", false));
            session.removeAttribute("buyNowVariantId"); session.removeAttribute("buyNowCollectionId"); session.removeAttribute("buyNowQuantity");
            if (variantId != null) session.setAttribute("buyNowVariantId", variantId);
            if (collectionId != null) session.setAttribute("buyNowCollectionId", collectionId);
            session.setAttribute("buyNowQuantity", quantity);
            return ResponseEntity.ok(Map.of("status", true));
        } catch (Exception e) { return ResponseEntity.status(500).body(Map.of("status", false)); }
    }

    @GetMapping("/buy-now")
    public ResponseEntity<?> getBuyNow(HttpSession session) {
        Integer variantId = (Integer) session.getAttribute("buyNowVariantId");
        Integer collectionId = (Integer) session.getAttribute("buyNowCollectionId");
        Integer quantity = (Integer) session.getAttribute("buyNowQuantity");
        if ((variantId == null && collectionId == null) || quantity == null) return ResponseEntity.ok(Map.of("status", false));

        Map<String, Object> data = new HashMap<>();
        double price, regularPrice, discount;
        String name, image;

        if (variantId != null) {
            ProductVariance pv = productVarianceRepo.findById(variantId).orElse(null);
            if (pv == null) return ResponseEntity.ok(Map.of("status", false));
            String baseName = pv.getProduct().getName();
            java.util.List<String> attrs = new java.util.ArrayList<>();
            if (pv.getSize() != null && pv.getSize().getSize() != null && !pv.getSize().getSize().isEmpty()) attrs.add("Size: " + pv.getSize().getSize());
            if (pv.getColor() != null && pv.getColor().getColor() != null && !pv.getColor().getColor().isEmpty()) attrs.add("Color: " + pv.getColor().getColor());
            if (pv.getGemstone() != null && pv.getGemstone().getGemStone() != null && !pv.getGemstone().getGemStone().isEmpty()) attrs.add("Gem: " + pv.getGemstone().getGemStone());
            name = attrs.isEmpty() ? baseName : baseName + " (" + String.join(", ", attrs) + ")";
            regularPrice = pv.getRegularPrice();
            discount = pv.getDiscountPercentage() != null ? pv.getDiscountPercentage() : 0.0;

            // ✅ CHANGE: Use Cloudinary URL directly
            image = pv.getProduct().getImage1();

            data.put("variantId", variantId);
        } else {
            Collection col = collectionRepo.findById(collectionId).orElse(null);
            if (col == null) return ResponseEntity.ok(Map.of("status", false));
            name = "Collection: " + col.getTitle();
            regularPrice = col.getRegularPrice();
            discount = col.getDiscountPercentage();

            // ✅ CHANGE: Use Cloudinary URL directly
            image = col.getImage1();

            data.put("collectionId", collectionId);
        }

        price = discount > 0 ? regularPrice * (1 - discount / 100) : regularPrice;
        double subtotal = price * quantity;
        double tax = subtotal * getTaxRate();
        double total = subtotal + tax;

        data.put("name", name); data.put("image", image); data.put("price", price); data.put("quantity", quantity);
        data.put("subtotal", subtotal); data.put("tax", tax); data.put("total", total);

        return ResponseEntity.ok(Map.of("status", true, "data", data));
    }

    private ResponseEntity<?> buildPayHereResponse(PayHereRequestDTO payHereRequest, String selectedPayment) {
        Map<String, Object> payhereData = new HashMap<>();
        payhereData.put("sandbox", payHereSandbox);
        payhereData.put("merchant_id", payHereRequest.getMerchantId());
        payhereData.put("return_url", payHereReturnUrl);
        payhereData.put("cancel_url", payHereCancelUrl);
        payhereData.put("notify_url", payHereNotifyUrl);
        payhereData.put("order_id", payHereRequest.getOrderId());
        payhereData.put("items", payHereRequest.getItems());
        payhereData.put("currency", payHereRequest.getCurrency());
        payhereData.put("amount", String.format("%.2f", payHereRequest.getAmount()));
        payhereData.put("first_name", payHereRequest.getFirstName());
        payhereData.put("last_name", payHereRequest.getLastName());
        payhereData.put("email", payHereRequest.getEmail());
        payhereData.put("phone", payHereRequest.getPhone());
        payhereData.put("address", payHereRequest.getAddress());
        payhereData.put("city", payHereRequest.getCity());
        payhereData.put("country", payHereRequest.getCountry());
        payhereData.put("hash", payHereRequest.getHash());
        payhereData.put("custom_1", payHereRequest.getCustom1());
        payhereData.put("payhere_post_url", payHereSandbox ? "https://sandbox.payhere.lk/pay/checkout" : "https://www.payhere.lk/pay/checkout");
        return ResponseEntity.ok(Map.of("status", true, "paymentType", selectedPayment, "payhereData", payhereData));
    }

    private double parseDoubleSafe(String s) { try { return Double.parseDouble(Optional.ofNullable(s).orElse("").trim()); } catch (Exception e) { return 0.0; } }
    private String normalizeId(String raw) { if (raw == null) return null; String t = raw.trim(); return (t.isEmpty() || "null".equalsIgnoreCase(t)) ? null : t; }
    private boolean isNumeric(String s) { return s != null && s.matches("\\d+"); }
    private void addressToDtoPopulate(Integer id, CheckoutRequestDTO dto) { DeliveryAddress a = addressService.getAddressById(id); dto.setFirstName(a.getFirstName()); dto.setLastName(a.getLastName()); dto.setContactNo(a.getContactNo()); dto.setAddressLine1(a.getLine1()); dto.setAddressLine2(a.getLine2()); dto.setPostalCode(a.getPostalCode()); if(a.getCity()!=null) dto.setCityId(a.getCity().getId()); if(a.getCountry()!=null) dto.setCountryId(a.getCountry().getId()); }
    private void addressToDtoPopulateBilling(Integer id, CheckoutRequestDTO dto) { DeliveryAddress a = addressService.getAddressById(id); dto.setBillingFirstName(a.getFirstName()); dto.setBillingLastName(a.getLastName()); dto.setBillingContactNo(a.getContactNo()); dto.setBillingAddressLine1(a.getLine1()); dto.setBillingAddressLine2(a.getLine2()); dto.setBillingPostalCode(a.getPostalCode()); if(a.getCity()!=null) dto.setBillingCityId(a.getCity().getId()); if(a.getCountry()!=null) dto.setBillingCountryId(a.getCountry().getId()); }
}