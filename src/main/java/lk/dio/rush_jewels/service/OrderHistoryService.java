package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.dto.OrderHistoryDTO;
import lk.dio.rush_jewels.dto.OrderItemDTO;
import lk.dio.rush_jewels.dto.ReturnRequestDTO;
import lk.dio.rush_jewels.dto.ReviewRequestDTO;
import lk.dio.rush_jewels.model.*;
import lk.dio.rush_jewels.model.Collection;
import lk.dio.rush_jewels.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderHistoryService {

    private final OrdersRepository ordersRepo;
    private final OrderItemsRepository orderItemsRepo;
    private final OrderStatusRepository orderStatusRepo;
    private final PaymentRepository paymentRepo;
    private final ReturnRepository returnRepo;
    private final ReturnTypeRepository returnTypeRepo;
    private final ReviewRepository reviewRepo;
    private final ReviewStatusRepository reviewStatusRepo;
    private final ProductVarianceRepository productVarianceRepo;
    private final CollectionRepository collectionRepo;
    private final StockRepository stockRepo;
    private final WarehouseRepository warehouseRepo;
    private final StockStatusRepository stockStatusRepo;
    private final ReturnItemsRepository returnItemsRepo;

    // ✅ NOTE: Image Base Path configurations removed.
    // We now use Cloudinary URLs stored directly in the database.

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy");

    // CONFIGURATION IDS
    private static final int DELIVERED_STATUS_ID = 4;
    private static final int RETURN_TYPE_EXCHANGE_ID = 2;
    private static final int REVIEW_STATUS_PENDING_ID = 1;
    private static final int WAREHOUSE_MAIN_ID = 1;
    private static final int STOCK_STATUS_IN_STOCK_ID = 1;
    private static final int STOCK_STATUS_OUT_OF_STOCK_ID = 2;

    public OrderHistoryService(OrdersRepository ordersRepo, OrderItemsRepository orderItemsRepo,
                               OrderStatusRepository orderStatusRepo, PaymentRepository paymentRepo,
                               ReturnRepository returnRepo, ReturnTypeRepository returnTypeRepo,
                               ReviewRepository reviewRepo, ReviewStatusRepository reviewStatusRepo,
                               ProductVarianceRepository productVarianceRepo, CollectionRepository collectionRepo,
                               StockRepository stockRepo, WarehouseRepository warehouseRepo,
                               StockStatusRepository stockStatusRepo,ReturnItemsRepository returnItemsRepo) {
        this.ordersRepo = ordersRepo;
        this.orderItemsRepo = orderItemsRepo;
        this.orderStatusRepo = orderStatusRepo;
        this.paymentRepo = paymentRepo;
        this.returnRepo = returnRepo;
        this.returnTypeRepo = returnTypeRepo;
        this.reviewRepo = reviewRepo;
        this.reviewStatusRepo = reviewStatusRepo;
        this.productVarianceRepo = productVarianceRepo;
        this.collectionRepo = collectionRepo;
        this.stockRepo = stockRepo;
        this.warehouseRepo = warehouseRepo;
        this.stockStatusRepo = stockStatusRepo;
        this.returnItemsRepo = returnItemsRepo;
    }

    @Transactional(readOnly = true)
    public List<OrderHistoryDTO> getUserOrderHistory(User user) {
        return ordersRepo.findByUserOrderByOrderedAtDesc(user).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderHistoryDTO getOrderDetails(String orderId, User user) {
        Orders order = ordersRepo.findById(orderId).orElseThrow(() -> new NoSuchElementException("Order not found"));
        if (!Objects.equals(order.getUser().getId(), user.getId())) throw new SecurityException("Unauthorized access to order");
        return convertToDTOWithItems(order);
    }

    @Transactional
    public void requestReturn(String orderId, User user, ReturnRequestDTO requestDTO) {
        Orders order = ordersRepo.findById(orderId).orElseThrow();

        Return returnRequest = new Return();
        returnRequest.setId("RTN-" + System.currentTimeMillis());
        returnRequest.setOrders(order);
        returnRequest.setStatus(ReturnStatus.RETURN_REQUESTED);
        returnRequest.setReturnReason(requestDTO.getReason());
        returnRequest.setReturnType(returnTypeRepo.findById(RETURN_TYPE_EXCHANGE_ID).orElseThrow()); // Use constant ID
        Return savedReturn = returnRepo.save(returnRequest);

        List<OrderItems> orderItems = orderItemsRepo.findByOrders(order);
        for (String selectedName : requestDTO.getSelectedItemNames()) {
            orderItems.stream().filter(oi -> {
                String name = (oi.getProductVariance() != null)
                        ? oi.getProductVariance().getProduct().getName()
                        : "Collection: " + oi.getCollection().getTitle();
                return name.equals(selectedName);
            }).findFirst().ifPresent(match -> {
                ReturnItems ri = new ReturnItems();
                ri.setReturns(savedReturn);
                ri.setOrderItems(match);
                ri.setQty(match.getQty());
                returnItemsRepo.save(ri);
            });
        }
    }

    @Transactional
    public void submitReview(User user, ReviewRequestDTO dto) {
        if (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        ReviewStatus pendingStatus = reviewStatusRepo.findById(REVIEW_STATUS_PENDING_ID)
                .orElseThrow(() -> new IllegalStateException("Review Status configuration missing"));

        Review review = new Review();
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setCreatedAt(OffsetDateTime.now());
        review.setStatus(pendingStatus);
        review.setUser(user);

        if (dto.getProductVariantId() != null) {
            ProductVariance pv = productVarianceRepo.findById(dto.getProductVariantId())
                    .orElseThrow(() -> new NoSuchElementException("Product variant not found"));
            review.setProductVariance(pv);
        } else if (dto.getCollectionId() != null) {
            Collection col = collectionRepo.findById(dto.getCollectionId())
                    .orElseThrow(() -> new NoSuchElementException("Collection not found"));
            review.setCollection(col);
        } else {
            throw new IllegalArgumentException("Review must target a specific product or collection");
        }
        reviewRepo.save(review);
    }

    @Transactional
    public boolean cancelOrder(String orderId, User user) {
        Orders order = ordersRepo.findById(orderId).orElseThrow(() -> new NoSuchElementException("Order not found"));
        if (!Objects.equals(order.getUser().getId(), user.getId())) throw new SecurityException("Unauthorized");

        if (order.getOrderStatus().getId() >= 3) {
            throw new IllegalStateException("Cannot cancel order that has already been shipped");
        }

        OrderStatus cancelledStatus = orderStatusRepo.findByOrderStatus("Cancelled")
                .orElseThrow(() -> new IllegalStateException("Cancelled status not found in DB"));
        order.setOrderStatus(cancelledStatus);
        ordersRepo.save(order);

        List<OrderItems> items = orderItemsRepo.findByOrders(order);
        Warehouse mainWarehouse = warehouseRepo.findById(WAREHOUSE_MAIN_ID)
                .orElseThrow(() -> new IllegalStateException("Main Warehouse (ID: " + WAREHOUSE_MAIN_ID + ") not found"));

        for (OrderItems item : items) {
            restoreItemStock(item, mainWarehouse);
        }

        return true;
    }

    private void restoreItemStock(OrderItems item, Warehouse warehouse) {
        Stock stock = null;
        StockStatus outOfStock = stockStatusRepo.findById(STOCK_STATUS_OUT_OF_STOCK_ID).orElseThrow();
        StockStatus inStock = stockStatusRepo.findById(STOCK_STATUS_IN_STOCK_ID).orElseThrow();

        if (item.getProductVariance() != null) {
            stock = stockRepo.findByProductVarianceAndWarehouse(item.getProductVariance(), warehouse)
                    .orElseGet(() -> {
                        Stock newStock = new Stock();
                        newStock.setProductVariance(item.getProductVariance());
                        newStock.setWarehouse(warehouse);
                        newStock.setQty(0);
                        newStock.setStockStatus(outOfStock);
                        return newStock;
                    });

        } else if (item.getCollection() != null) {
            stock = stockRepo.findByCollectionAndWarehouse(item.getCollection(), warehouse)
                    .orElseGet(() -> {
                        Stock newStock = new Stock();
                        newStock.setCollection(item.getCollection());
                        newStock.setWarehouse(warehouse);
                        newStock.setQty(0);
                        newStock.setStockStatus(outOfStock);
                        return newStock;
                    });
        }

        if (stock != null) {
            int newQty = stock.getQty() + item.getQty();
            stock.setQty(newQty);

            if (newQty > 0) {
                stock.setStockStatus(inStock);
            } else {
                stock.setStockStatus(outOfStock);
            }

            stockRepo.save(stock);
        }
    }

    private OrderHistoryDTO convertToDTO(Orders order) {
        OrderHistoryDTO dto = new OrderHistoryDTO();

        dto.setOrderId(order.getId());
        dto.setOrderDate(DATE_FORMAT.format(order.getOrderedAt()));
        dto.setOrderStatus(order.getOrderStatus().getOrderStatus());

        Optional<Return> returnReq = returnRepo.findByOrders(order);
        if (returnReq.isPresent()) {
            dto.setHasReturn(true);
            String rawStatus = returnReq.get().getStatus().name().toLowerCase().replace("_", " ");
            dto.setReturnStatus(rawStatus.substring(0, 1).toUpperCase() + rawStatus.substring(1));
            if (returnReq.get().getReturnType() != null) {
                dto.setReturnType(returnReq.get().getReturnType().getReturnType());
            } else {
                dto.setReturnType("");
            }
        } else {
            dto.setHasReturn(false);
            dto.setReturnStatus("");
            dto.setReturnType("");
        }

        paymentRepo.findByOrders(order).ifPresent(p -> {
            dto.setTotalAmount(p.getFinalTotal());
            dto.setTax(p.getTax() != null ? p.getTax() : 0.0);
            dto.setDiscount(p.getDiscount() != null ? p.getDiscount() : 0.0);

            if (p.getPaymentsMethod() != null) {
                dto.setPaymentMethod(p.getPaymentsMethod().getMethod());
            }

            if (p.getPaymentStatus() != null) {
                dto.setPaymentStatus(p.getPaymentStatus().getPaymentStatus());
            }
        });

        List<OrderItems> items = orderItemsRepo.findByOrders(order);
        dto.setTotalItems(items.stream().mapToInt(OrderItems::getQty).sum());

        if (order.getDeliveryAddress() != null) {
            dto.setDeliveryAddress(buildAddressString(order.getDeliveryAddress()));
        }

        dto.setCanCancel(order.getOrderStatus().getId() < 3);

        return dto;
    }

    private OrderHistoryDTO convertToDTOWithItems(Orders order) {
        OrderHistoryDTO dto = convertToDTO(order);
        List<OrderItems> items = orderItemsRepo.findByOrders(order);
        List<OrderItemDTO> itemDTOs = items.stream().map(this::convertItemToDTO).collect(Collectors.toList());
        dto.setItems(itemDTOs);

        if(order.getShipping() != null) {
            dto.setShippingCost(order.getShipping().getValue());
            dto.setShippingMethod(order.getShipping().getShippingMethod());
        }

        dto.setSubTotal(itemDTOs.stream().mapToDouble(OrderItemDTO::getSubtotal).sum());
        return dto;
    }

    private OrderItemDTO convertItemToDTO(OrderItems item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setQuantity(item.getQty());

        if (item.getProductVariance() != null) {
            ProductVariance pv = item.getProductVariance();
            dto.setVariantId(pv.getId());
            dto.setProductName(pv.getProduct().getName());
            dto.setPrice(pv.getRegularPrice());
            if(pv.getDiscountPercentage() != null && pv.getDiscountPercentage() > 0) {
                dto.setPrice(pv.getRegularPrice() * (1 - pv.getDiscountPercentage() / 100));
            }
            // ✅ CHANGE: Use Cloudinary URL directly
            dto.setImage(pv.getProduct().getImage1());

        } else if (item.getCollection() != null) {
            Collection c = item.getCollection();
            dto.setVariantId(c.getId());
            dto.setProductName("Collection: " + c.getTitle());
            dto.setPrice(c.getPrice());
            // ✅ CHANGE: Use Cloudinary URL directly
            dto.setImage(c.getImage1());
        }

        dto.setSubtotal(dto.getPrice() * item.getQty());
        return dto;
    }

    private String buildAddressString(DeliveryAddress addr) {
        List<String> addressParts = new ArrayList<>();
        if (addr.getLine1() != null && !addr.getLine1().trim().isEmpty()) addressParts.add(addr.getLine1().trim());
        if (addr.getLine2() != null && !addr.getLine2().trim().isEmpty()) addressParts.add(addr.getLine2().trim());

        String city = (addr.getCity() != null) ? addr.getCity().getCity() : addr.getCityText();
        if (city != null && !city.trim().isEmpty()) addressParts.add(city.trim());

        String province = (addr.getProvince() != null) ? addr.getProvince().getProvince() : addr.getStateText();
        if (province != null && !province.trim().isEmpty()) addressParts.add(province.trim());

        if (addr.getCountry() != null) addressParts.add(addr.getCountry().getCountry());

        return String.join(", ", addressParts);
    }
}