package lk.dio.rush_jewels.controller;

import jakarta.servlet.http.HttpSession;
import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class CartController {

    private final CartService cartService;
    private static final String USER_SESSION_KEY = "user";

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // ---------------- Session Helper ----------------
    private User getSessionUser(HttpSession session) {
        return (User) session.getAttribute(USER_SESSION_KEY);
    }

    private ResponseEntity<Map<String, Object>> requireLogin(HttpSession session) {
        User user = getSessionUser(session);
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "User not authenticated"));
        }
        return null;
    }

    // ---------------- Load Cart ----------------
    @GetMapping("/cart")
    public ResponseEntity<?> loadCart(HttpSession session) {
        ResponseEntity<Map<String, Object>> loginCheck = requireLogin(session);
        if (loginCheck != null) return loginCheck;

        User user = getSessionUser(session);
        var data = cartService.loadCartData(user);

        if (data.success()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "totalItems", data.totalItems(),
                    "subtotal", data.subtotal(),
                    "tax", data.tax(),
                    "total", data.total(),
                    "cartItems", data.cartItems()
            ));
        } else {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", data.message()));
        }
    }

    // ---------------- Add to Cart ----------------
    @PostMapping("/add-to-cart")
    public ResponseEntity<?> addToCart(
            @RequestParam(value = "varianceId", required = false) Integer varianceId,
            @RequestParam(value = "collectionId", required = false) Integer collectionId,
            @RequestParam(value = "qty", required = false, defaultValue = "1") Integer qty,
            HttpSession session
    ) {
        ResponseEntity<Map<String, Object>> loginCheck = requireLogin(session);
        if (loginCheck != null) return loginCheck;

        User user = getSessionUser(session);

        // Ensure at least one ID is present
        if (varianceId == null && collectionId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Product or Collection ID required."));
        }

        var response = cartService.addToCart(user.getId(), varianceId, collectionId, qty);

        if (response.success()) return ResponseEntity.ok(Map.of("success", true, "message", response.message()));
        else return ResponseEntity.badRequest().body(Map.of("success", false, "message", response.message()));
    }

    // ---------------- Update Cart Quantity ----------------
    @PostMapping("/update-cart-quantity")
    public ResponseEntity<?> updateCartQuantity(
            @RequestParam("cartId") Integer cartId,
            @RequestParam("action") String action,
            HttpSession session
    ) {
        ResponseEntity<Map<String, Object>> loginCheck = requireLogin(session);
        if (loginCheck != null) return loginCheck;

        User user = getSessionUser(session);
        var response = cartService.updateCartQuantity(user.getId(), cartId, action);

        if (response.success()) return ResponseEntity.ok(Map.of("success", true, "message", response.message()));
        else return ResponseEntity.badRequest().body(Map.of("success", false, "message", response.message()));
    }

    // ---------------- Remove Item ----------------
    @PostMapping("/remove-from-cart")
    public ResponseEntity<?> removeFromCart(
            @RequestParam("cartId") Integer cartId,
            HttpSession session
    ) {
        ResponseEntity<Map<String, Object>> loginCheck = requireLogin(session);
        if (loginCheck != null) return loginCheck;

        User user = getSessionUser(session);
        var response = cartService.removeCartItem(user.getId(), cartId);

        if (response.success()) return ResponseEntity.ok(Map.of(
                "success", true,
                "message", response.message(),
                "cartId", cartId
        ));
        else return ResponseEntity.badRequest().body(Map.of("success", false, "message", response.message()));
    }

    // ---------------- Clear Cart ----------------
    @PostMapping("/clear-cart")
    public ResponseEntity<?> clearCart(HttpSession session) {
        ResponseEntity<Map<String, Object>> loginCheck = requireLogin(session);
        if (loginCheck != null) return loginCheck;

        User user = getSessionUser(session);
        var response = cartService.clearUserCart(user.getId());

        if (response.success()) return ResponseEntity.ok(Map.of("success", true, "message", response.message()));
        else return ResponseEntity.badRequest().body(Map.of("success", false, "message", response.message()));
    }
}