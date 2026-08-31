package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.dto.CartItemDTO;
import lk.dio.rush_jewels.model.*;
import lk.dio.rush_jewels.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductVarianceRepository productVarianceRepository;
    private final CollectionRepository collectionRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final SystemSettingRepository systemSettingRepository;

    public CartService(
            CartRepository cartRepository,
            ProductVarianceRepository productVarianceRepository,
            CollectionRepository collectionRepository,
            StockRepository stockRepository,
            UserRepository userRepository,
            SystemSettingRepository systemSettingRepository
    ) {
        this.cartRepository = cartRepository;
        this.productVarianceRepository = productVarianceRepository;
        this.collectionRepository = collectionRepository;
        this.stockRepository = stockRepository;
        this.userRepository = userRepository;
        this.systemSettingRepository = systemSettingRepository;
    }

    // =========================================================
    // TAX RATE
    // =========================================================
    private double getTaxRate() {
        return 0.0;
    }

    // =========================================================
    // LOAD CART
    // =========================================================
    @Transactional(readOnly = true)
    public CartData loadCartData(User sessionUser) {

        User user = userRepository.findById(sessionUser.getId()).orElse(null);
        if (user == null) return new CartData(false, "User not found.");

        List<Cart> cartList = cartRepository.findByUser(user);

        double subtotal = 0;
        int totalItems = 0;
        List<CartItemDTO> items = new ArrayList<>();

        for (Cart cart : cartList) {

            CartItemDTO dto = null;

            if (cart.getProductVariance() != null) {
                ProductVariance pv = cart.getProductVariance();
                if (pv.getProduct().getStatus().getId() != 1) continue;
                dto = mapVarianceToDTO(cart, pv);
            }
            else if (cart.getCollection() != null) {
                Collection col = cart.getCollection();
                if (col.getStatus().getId() != 1) continue;
                dto = mapCollectionToDTO(cart, col);
            }

            if (dto != null) {
                subtotal += dto.getFinalPrice() * dto.getQuantity();
                totalItems += dto.getQuantity();
                items.add(dto);
            }
        }

        double tax = subtotal * getTaxRate();
        double total = subtotal + tax;

        return new CartData(true, items, totalItems, subtotal, tax, total, null);
    }

    // =========================================================
    // ADD TO CART
    // =========================================================
    @Transactional
    public CartActionResponse addToCart(Integer userId, Integer varianceId, Integer collectionId, int quantity) {

        if (quantity <= 0) quantity = 1;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return new CartActionResponse(false, "Invalid user.");

        if (varianceId != null) {
            return addVarianceToCart(user, varianceId, quantity);
        }
        else if (collectionId != null) {
            return addCollectionToCart(user, collectionId, quantity);
        }

        return new CartActionResponse(false, "No item specified.");
    }

    // ---------------- PRODUCT (UNCHANGED) ----------------
    private CartActionResponse addVarianceToCart(User user, Integer varianceId, int quantity) {

        ProductVariance pv = productVarianceRepository.findById(varianceId).orElse(null);
        if (pv == null) return new CartActionResponse(false, "Product not found.");

        Long stockLong = stockRepository.sumAvailableStockByProductVarianceId(pv.getId());
        int stockQty = stockLong != null ? stockLong.intValue() : 0;

        if (stockQty <= 0)
            return new CartActionResponse(false, "This product is out of stock.");

        Optional<Cart> existing = cartRepository.findByUserAndProductVariance(user, pv);

        if (existing.isPresent()) {
            int newQty = existing.get().getQty() + quantity;
            if (newQty > stockQty)
                return new CartActionResponse(false, "Only " + stockQty + " unit(s) available.");

            existing.get().setQty(newQty);
            cartRepository.save(existing.get());
            return new CartActionResponse(true, "Cart updated.");
        }

        if (quantity > stockQty)
            return new CartActionResponse(false, "Only " + stockQty + " unit(s) available.");

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setProductVariance(pv);
        cart.setQty(quantity);
        cartRepository.save(cart);

        return new CartActionResponse(true, "Product added to cart.");
    }

    // ---------------- COLLECTION (FIXED) ----------------
    private CartActionResponse addCollectionToCart(User user, Integer collectionId, int quantity) {

        Collection col = collectionRepository.findById(collectionId).orElse(null);
        if (col == null) return new CartActionResponse(false, "Collection not found.");

        Long stockLong = stockRepository.sumAvailableStockByCollectionId(col.getId());
        int stockQty = stockLong != null ? stockLong.intValue() : 0;

        if (stockQty <= 0)
            return new CartActionResponse(false, "This collection is out of stock.");

        Optional<Cart> existing = cartRepository.findByUserAndCollection(user, col);

        if (existing.isPresent()) {
            int newQty = existing.get().getQty() + quantity;
            if (newQty > stockQty)
                return new CartActionResponse(false, "Only " + stockQty + " collection(s) available.");

            existing.get().setQty(newQty);
            cartRepository.save(existing.get());
            return new CartActionResponse(true, "Collection quantity updated.");
        }

        if (quantity > stockQty)
            return new CartActionResponse(false, "Only " + stockQty + " collection(s) available.");

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCollection(col);
        cart.setQty(quantity);
        cartRepository.save(cart);

        return new CartActionResponse(true, "Collection added to cart.");
    }

    // =========================================================
    // UPDATE QTY
    // =========================================================
    @Transactional
    public CartActionResponse updateCartQuantity(Integer userId, Integer cartId, String action) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByIdAndUser(cartId, user)
                .orElse(null);

        if (cart == null)
            return new CartActionResponse(false, "Cart item not found.");

        int availableStock = 0;

        if (cart.getProductVariance() != null) {
            Long stock = stockRepository
                    .sumInStockQtyByProductVarianceId(cart.getProductVariance().getId());
            availableStock = stock != null ? stock.intValue() : 0;
        }
        else if (cart.getCollection() != null) {
            Long stock = stockRepository
                    .sumAvailableStockByCollectionId(cart.getCollection().getId());
            availableStock = stock != null ? stock.intValue() : 0;
        }

        int newQty = cart.getQty();
        if ("increase".equals(action)) newQty++;
        else if ("decrease".equals(action)) newQty--;
        else return new CartActionResponse(false, "Invalid action.");

        if (newQty < 1)
            return new CartActionResponse(false, "Quantity must be at least 1.");

        if (newQty > availableStock)
            return new CartActionResponse(false, "Only " + availableStock + " available.");

        cart.setQty(newQty);
        cartRepository.save(cart);

        return new CartActionResponse(true, "Cart updated.");
    }

    // =========================================================
    // REMOVE / CLEAR
    // =========================================================
    @Transactional
    public CartActionResponse removeCartItem(Integer userId, Integer cartId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int deleted = cartRepository.deleteByIdAndUser(cartId, user);
        return new CartActionResponse(deleted > 0, deleted > 0 ? "Item removed." : "Item not found.");
    }

    @Transactional
    public CartActionResponse clearUserCart(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int deleted = cartRepository.deleteByUser(user);
        return new CartActionResponse(true, deleted + " item(s) removed.");
    }

    // =========================================================
    // DTO MAPPERS
    // =========================================================
    private CartItemDTO mapVarianceToDTO(Cart cart, ProductVariance pv) {

        Long stockLong = stockRepository.sumInStockQtyByProductVarianceId(pv.getId());
        int stock = stockLong != null ? stockLong.intValue() : 0;

        double regularPrice = pv.getRegularPrice();
        double discount = pv.getDiscountPercentage() != null ? pv.getDiscountPercentage() : 0;
        double finalPrice = regularPrice * (1 - discount / 100);

        CartItemDTO dto = new CartItemDTO();
        dto.setCartId(String.valueOf(cart.getId()));
        dto.setVarianceId(pv.getId());
        dto.setQuantity(cart.getQty());
        dto.setAvailableStock(stock);
        dto.setRegularPrice(regularPrice);
        dto.setDiscountPercentage(discount);
        dto.setFinalPrice(finalPrice);
        String name = pv.getProduct().getName();
        java.util.List<String> attrs = new java.util.ArrayList<>();
        if (pv.getSize() != null && pv.getSize().getSize() != null && !pv.getSize().getSize().isEmpty()) attrs.add("Size: " + pv.getSize().getSize());
        if (pv.getColor() != null && pv.getColor().getColor() != null && !pv.getColor().getColor().isEmpty()) attrs.add("Color: " + pv.getColor().getColor());
        if (pv.getGemstone() != null && pv.getGemstone().getGemStone() != null && !pv.getGemstone().getGemStone().isEmpty()) attrs.add("Gem: " + pv.getGemstone().getGemStone());
        if (!attrs.isEmpty()) name += " (" + String.join(", ", attrs) + ")";
        dto.setName(name);
        dto.setImage(pv.getProduct().getImage1());

        return dto;
    }

    private CartItemDTO mapCollectionToDTO(Cart cart, Collection col) {

        Long stockLong = stockRepository.sumAvailableStockByCollectionId(col.getId());
        int stock = stockLong != null ? stockLong.intValue() : 0;

        CartItemDTO dto = new CartItemDTO();
        dto.setCartId(String.valueOf(cart.getId()));
        dto.setCollectionId(col.getId());
        dto.setQuantity(cart.getQty());
        dto.setAvailableStock(stock);
        dto.setRegularPrice(col.getRegularPrice());
        dto.setDiscountPercentage(col.getDiscountPercentage());
        dto.setFinalPrice(col.getPrice());
        dto.setName("Collection: " + col.getTitle());
        dto.setImage(col.getImage1());

        return dto;
    }

    // =========================================================
    public record CartActionResponse(boolean success, String message) {}

    public record CartData(
            boolean success,
            List<CartItemDTO> cartItems,
            int totalItems,
            double subtotal,
            double tax,
            double total,
            String message
    ) {
        public CartData(boolean success, String message) {
            this(success, List.of(), 0, 0, 0, 0, message);
        }
    }
}
