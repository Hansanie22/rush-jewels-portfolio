package lk.dio.rush_jewels.controller;

import jakarta.servlet.http.HttpSession;
import lk.dio.rush_jewels.dto.OrderHistoryDTO;
import lk.dio.rush_jewels.dto.ReturnRequestDTO;
import lk.dio.rush_jewels.dto.ReviewRequestDTO;
import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.service.OrderHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderHistoryController {

    private static final String USER_SESSION_KEY = "user";
    private final OrderHistoryService orderHistoryService;

    public OrderHistoryController(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = orderHistoryService;
    }

    @GetMapping
    public ResponseEntity<?> getOrderHistory(HttpSession session) {
        User user = (User) session.getAttribute(USER_SESSION_KEY);
        if (user == null) return ResponseEntity.status(401).body(Map.of("status", false));
        return ResponseEntity.ok(Map.of("status", true, "orders", orderHistoryService.getUserOrderHistory(user)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetails(@PathVariable String orderId, HttpSession session) {
        User user = (User) session.getAttribute(USER_SESSION_KEY);
        if (user == null) return ResponseEntity.status(401).body(Map.of("status", false));
        return ResponseEntity.ok(Map.of("status", true, "order", orderHistoryService.getOrderDetails(orderId, user)));
    }

    @PostMapping("/{orderId}/cancel/update")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderId, HttpSession session) {
        User user = (User) session.getAttribute(USER_SESSION_KEY);
        if (user == null) return ResponseEntity.status(401).body(Map.of("status", false));
        orderHistoryService.cancelOrder(orderId, user);
        return ResponseEntity.ok(Map.of("status", true));
    }

    // ✅ NEW: Request Return
    @PostMapping("/{orderId}/return")
    public ResponseEntity<?> requestReturn(@PathVariable String orderId, @RequestBody ReturnRequestDTO requestDTO, HttpSession session) {
        User user = (User) session.getAttribute(USER_SESSION_KEY);
        if (user == null) return ResponseEntity.status(401).body(Map.of("status", false));

        try {
            orderHistoryService.requestReturn(orderId, user, requestDTO);
            return ResponseEntity.ok(Map.of("status", true, "message", "Return requested successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", false, "message", e.getMessage()));
        }
    }

    // ✅ NEW: Submit Review
    @PostMapping("/review")
    public ResponseEntity<?> submitReview(@RequestBody ReviewRequestDTO requestDTO, HttpSession session) {
        User user = (User) session.getAttribute(USER_SESSION_KEY);
        if (user == null) return ResponseEntity.status(401).body(Map.of("status", false));

        try {
            orderHistoryService.submitReview(user, requestDTO);
            return ResponseEntity.ok(Map.of("status", true, "message", "Review submitted for approval."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", false, "message", e.getMessage()));
        }
    }
}