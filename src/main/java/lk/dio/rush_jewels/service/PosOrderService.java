package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.dto.PosCartItemDTO;
import lk.dio.rush_jewels.dto.PosCheckoutRequestDTO;
import lk.dio.rush_jewels.model.*;
import lk.dio.rush_jewels.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

@Service
public class PosOrderService {

    private final OrdersRepository ordersRepo;
    private final OrderItemsRepository orderItemsRepo;
    private final ProductVarianceRepository productVarianceRepo;
    private final StockRepository stockRepo;
    private final OrderStatusRepository orderStatusRepo;
    private final PaymentRepository paymentRepo;
    private final PaymentStatusRepository paymentStatusRepo;
    private final PaymentMethodRepository paymentMethodRepo;
    private final UserRepository userRepo;
    private final DeliveryAddressRepository deliveryAddressRepo;
    private final ShippingRepository shippingRepo;
    private final StockStatusRepository stockStatusRepo;
    private final StatusRepository statusRepo;
    private final CollectionRepository collectionRepo;
    private final CollectionSetRepository collectionSetRepo;
    private final CountryRepository countryRepo;

    public PosOrderService(OrdersRepository ordersRepo, OrderItemsRepository orderItemsRepo, ProductVarianceRepository productVarianceRepo, StockRepository stockRepo, OrderStatusRepository orderStatusRepo, PaymentRepository paymentRepo, PaymentStatusRepository paymentStatusRepo, PaymentMethodRepository paymentMethodRepo, UserRepository userRepo, DeliveryAddressRepository deliveryAddressRepo, ShippingRepository shippingRepo, StockStatusRepository stockStatusRepo, StatusRepository statusRepo, CollectionRepository collectionRepo, CollectionSetRepository collectionSetRepo, CountryRepository countryRepo) {
        this.ordersRepo = ordersRepo;
        this.orderItemsRepo = orderItemsRepo;
        this.productVarianceRepo = productVarianceRepo;
        this.stockRepo = stockRepo;
        this.orderStatusRepo = orderStatusRepo;
        this.paymentRepo = paymentRepo;
        this.paymentStatusRepo = paymentStatusRepo;
        this.paymentMethodRepo = paymentMethodRepo;
        this.userRepo = userRepo;
        this.deliveryAddressRepo = deliveryAddressRepo;
        this.shippingRepo = shippingRepo;
        this.stockStatusRepo = stockStatusRepo;
        this.statusRepo = statusRepo;
        this.collectionRepo = collectionRepo;
        this.collectionSetRepo = collectionSetRepo;
        this.countryRepo = countryRepo;
    }

    @Transactional
    public Object processPosOrder(PosCheckoutRequestDTO request) {
        // 1. Get or Create Customer
        User orderUser;
        if (request.getCustomerMobile() != null && !request.getCustomerMobile().trim().isEmpty()) {
            String mobile = request.getCustomerMobile().trim();
            String name = request.getCustomerName() != null && !request.getCustomerName().trim().isEmpty() ? request.getCustomerName().trim() : "POS Customer";
            
            // Try to find existing user by mobile first, then fall back to dynamic email
            orderUser = userRepo.findFirstByMobile(mobile).orElseGet(() -> {
                String dynamicEmail = mobile + "@pos.velorajewellery.lk";
                return userRepo.findByEmail(dynamicEmail).orElseGet(() -> {
                    User u = new User();
                    String[] names = name.split(" ");
                    u.setFname(names[0]);
                    u.setLname(names.length > 1 ? name.substring(names[0].length()).trim() : "");
                    u.setEmail(dynamicEmail);
                    u.setMobile(mobile);
                    u.setPassword("pos_placeholder"); // Required by User entity
                    u.setLoginProvider("LOCAL");      // Required: prevents null constraint errors
                    u.setType("USER");
                    u.setSubscribed(false);
                    u.setCreatedAt(new Date());
                    u.setStatus(statusRepo.findById(1).orElseThrow());
                    return userRepo.save(u);
                });
            });
        } else {
            orderUser = userRepo.findByEmail("walkin@velorajewellery.lk").orElseGet(() -> {
                User u = new User();
                u.setFname("Walk-in");
                u.setLname("Customer");
                u.setEmail("walkin@velorajewellery.lk");
                u.setMobile("0000000000");
                u.setPassword("pos_placeholder"); // Required by User entity
                u.setLoginProvider("LOCAL");      // Required: prevents null constraint errors
                u.setType("USER");
                u.setSubscribed(false);
                u.setCreatedAt(new Date());
                u.setStatus(statusRepo.findById(1).orElseThrow());
                return userRepo.save(u);
            });
        }

        // 2. Get Dummy Delivery Address (Since it's in-store pickup)
        DeliveryAddress dummyAddress = deliveryAddressRepo.findAll().stream().findFirst().orElseGet(() -> {
            DeliveryAddress da = new DeliveryAddress();
            da.setLine1("In-Store Pickup");
            da.setAddressType(AddressType.SHIPPING);
            Country dummyCountry = countryRepo.findById(1).orElseGet(() -> {
                Country c = new Country();
                c.setCountry("Sri Lanka");
                return countryRepo.save(c);
            });
            da.setCountry(dummyCountry);
            da.setUser(orderUser);
            da.setFirstName("POS");
            da.setLastName("Customer");
            da.setContactNo(orderUser.getMobile() != null ? orderUser.getMobile() : "0000000000");
            return deliveryAddressRepo.save(da);
        });

        // 3. Get Store Pickup Shipping method
        Shipping storePickup = shippingRepo.findByValue(0.0).orElseGet(() -> {
            Shipping s = new Shipping();
            s.setShippingMethod("Store Pickup");
            s.setValue(0.0);
            s.setDescription("In-store POS");
            s.setStatus(1);
            return shippingRepo.save(s);
        });

        // 4. Create Order
        Orders order = new Orders();
        order.setId(generateCustomOrderId());
        order.setUser(orderUser);
        order.setOrderedAt(new Date());
        order.setDeliveryAddress(dummyAddress);
        order.setShipping(storePickup);
        order.setOrderSource("POS");
        
        // For POS, Bank Transfers are verified instantly by the cashier.
        if ("BANK".equalsIgnoreCase(request.getPaymentMethod()) && request.getBankReference() != null) {
            order.setSlipUrl(request.getBankReference()); // Store bank reference text
        }
        
        order.setOrderStatus(orderStatusRepo.findByOrderStatus("Completed").orElseGet(() -> {
            OrderStatus os = new OrderStatus(); os.setOrderStatus("Completed"); return orderStatusRepo.save(os);
        }));
        
        ordersRepo.save(order);

        // 5. Add Items & Deduct Stock
        double calculatedSubtotal = 0.0;
        StockStatus outOfStock = stockStatusRepo.findByStockStatus("Out Of Stock").orElseGet(() -> {
            StockStatus ss = new StockStatus(); ss.setStockStatus("Out Of Stock"); return stockStatusRepo.save(ss);
        });
        StockStatus inStock = stockStatusRepo.findByStockStatus("In Stock").orElseGet(() -> {
            StockStatus ss = new StockStatus(); ss.setStockStatus("In Stock"); return stockStatusRepo.save(ss);
        });

        for (PosCartItemDTO item : request.getCartItems()) {
            if ("COLLECTION".equalsIgnoreCase(item.getItemType())) {
                lk.dio.rush_jewels.model.Collection col = collectionRepo.findById(item.getCollectionId()).orElseThrow(() -> new RuntimeException("Collection " + item.getCollectionId() + " not found"));
                
                // Deduct stock based on whether it is a CollectionSet or not
                java.util.List<CollectionSet> sets = collectionSetRepo.findByCollection(col);
                if (sets != null && !sets.isEmpty()) {
                    // It is a Collection Set -> deduct from underlying product variances
                    for (CollectionSet set : sets) {
                        int requiredQty = set.getQty() * item.getQty();
                        java.util.List<Stock> stocks = stockRepo.findLockedByProductVarianceAndWarehouse1(set.getProductVariance());
                        if (stocks.isEmpty()) throw new RuntimeException("Stock record not found for variant in collection set");
                        Stock stock = stocks.get(0);
                        if (stock.getQty() < requiredQty) {
                            throw new RuntimeException("Insufficient stock for item in collection set: " + set.getProductVariance().getProduct().getName());
                        }
                        stock.setQty(stock.getQty() - requiredQty);
                        stock.setStockStatus(stock.getQty() == 0 ? outOfStock : inStock);
                        stockRepo.save(stock);
                    }
                } else {
                    // Normal Collection -> deduct from collection stock
                    if (col.getStockLimit() < item.getQty()) {
                        throw new RuntimeException("Insufficient stock for collection: " + col.getName());
                    }
                    col.setStockLimit(col.getStockLimit() - item.getQty());
                    collectionRepo.save(col);
                    
                    java.util.List<Stock> colStocks = stockRepo.findLockedByCollectionAndWarehouse1(col);
                    if (!colStocks.isEmpty()) {
                        Stock stock = colStocks.get(0);
                        stock.setQty(stock.getQty() - item.getQty());
                        stock.setStockStatus(stock.getQty() == 0 ? outOfStock : inStock);
                        stockRepo.save(stock);
                    }
                }

                // Add Order Item
                OrderItems oi = new OrderItems();
                oi.setOrders(order);
                oi.setCollection(col);
                oi.setQty(item.getQty());
                orderItemsRepo.save(oi);

                calculatedSubtotal += (col.getPrice() * item.getQty());
            } else {
                ProductVariance pv = productVarianceRepo.findById(item.getVariantId()).orElseThrow(() -> new RuntimeException("Product variant " + item.getVariantId() + " not found"));
                java.util.List<Stock> stocks = stockRepo.findLockedByProductVarianceAndWarehouse1(pv);
                if (stocks.isEmpty()) throw new RuntimeException("Stock record not found for variant " + item.getVariantId());
                Stock stock = stocks.get(0);

                if (stock.getQty() < item.getQty()) {
                    throw new RuntimeException("Insufficient stock for item: " + pv.getProduct().getName());
                }

                // Deduct Stock
                stock.setQty(stock.getQty() - item.getQty());
                if (stock.getQty() == 0) {
                    stock.setStockStatus(outOfStock);
                } else {
                    stock.setStockStatus(inStock);
                }
                stockRepo.save(stock);

                // Add Order Item
                OrderItems oi = new OrderItems();
                oi.setOrders(order);
                oi.setProductVariance(pv);
                oi.setQty(item.getQty());
                orderItemsRepo.save(oi);

                calculatedSubtotal += (pv.getPrice() * item.getQty());
            }
        }

        // 6. Save Payment
        Payment p = new Payment();
        p.setCreatedAt(new Date());
        p.setOrders(order);
        p.setTransactionId(order.getId());
        p.setUser(orderUser);
        
        // Since POS payments are verified instantly, mark as Paid
        p.setPaymentStatus(paymentStatusRepo.findByPaymentStatus("Paid").orElseGet(() -> {
            PaymentStatus ps = new PaymentStatus(); ps.setPaymentStatus("Paid"); return paymentStatusRepo.save(ps);
        }));
        
        String method = "Cash";
        if ("CARD".equalsIgnoreCase(request.getPaymentMethod())) method = "Credit / Debit Card";
        else if ("BANK".equalsIgnoreCase(request.getPaymentMethod())) method = "Bank Transfer";
        
        String finalMethod = method;
        p.setPaymentsMethod(paymentMethodRepo.findByMethod(method).orElseGet(() -> {
            PaymentMethod pm = new PaymentMethod(); pm.setMethod(finalMethod); return paymentMethodRepo.save(pm);
        }));
        p.setSubTotal(calculatedSubtotal);

        // Apply discount properly based on logic used in PosCheckoutRequestDTO
        double appliedDiscount = 0.0;
        if (request.getDiscountValue() != null && request.getDiscountValue() > 0) {
            if ("%".equals(request.getDiscountType())) {
                appliedDiscount = calculatedSubtotal * (request.getDiscountValue() / 100.0);
            } else if ("Rs".equals(request.getDiscountType())) {
                appliedDiscount = request.getDiscountValue();
            }
        }
        
        // Clamp: discount cannot exceed subtotal
        if (appliedDiscount > calculatedSubtotal) appliedDiscount = calculatedSubtotal;
        
        p.setDiscount(appliedDiscount);
        p.setFinalTotal(calculatedSubtotal - appliedDiscount);
        p.setTenderedAmount(request.getTenderedAmount());
        p.setChangeDue(request.getChangeDue());
        p.setCompletedAt(new Date());
        
        paymentRepo.save(p);

        return "Order " + order.getId() + " Placed Successfully via POS.";
    }

    private String generateCustomOrderId() {
        String prefix = "POS-" + YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Optional<String> lastId = ordersRepo.findLastOrderIdByPrefix(prefix + "-%");
        long seq = 1;
        if(lastId.isPresent()) {
            String[] parts = lastId.get().split("-");
            if(parts.length == 4) seq = Long.parseLong(parts[3]) + 1;
        }
        return prefix + "-" + String.format("%05d", seq);
    }
}
