package lk.dio.rush_jewels.controller;

import jakarta.servlet.http.HttpSession;
import lk.dio.rush_jewels.dto.OrderConfirmationDTO;
import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.service.OrderConfirmationService;
import lk.dio.rush_jewels.service.OrderEmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/order-confirmation")
public class OrderConfirmationController {

    private final OrderConfirmationService orderConfirmationService;
    private final OrderEmailService orderEmailService;
    private static final String USER_SESSION_KEY = "user";

    public OrderConfirmationController(
            OrderConfirmationService orderConfirmationService,
            OrderEmailService orderEmailService) {
        this.orderConfirmationService = orderConfirmationService;
        this.orderEmailService = orderEmailService;
    }

    /**
     * Get order confirmation details by order ID
     * GET /api/order-confirmation/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderConfirmation(@PathVariable String orderId, HttpSession session) {
        User user = (User) session.getAttribute(USER_SESSION_KEY);

        if (user == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("status", false, "message", "User not authenticated."));
        }

        try {
            // Get order confirmation DTO for API response
            OrderConfirmationDTO confirmation = orderConfirmationService.getOrderConfirmation(orderId, user);

            // Send email asynchronously when order confirmation is loaded
            try {
                OrderConfirmationService.OrderEmailData emailData =
                        orderConfirmationService.getOrderEmailData(orderId, user);

                orderEmailService.sendOrderConfirmationEmail(
                        emailData.getOrder(),
                        emailData.getOrderItems(),
                        emailData.getPayment()
                );
            } catch (Exception emailError) {
                // Log email error but don't fail the request
                System.err.println("⚠️ Failed to send email for order " + orderId + ": " + emailError.getMessage());
            }

            return ResponseEntity.ok(Map.of(
                    "status", true,
                    "data", confirmation
            ));

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("status", false, "message", "Order not found: " + orderId));

        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                    .body(Map.of("status", false, "message", "Unauthorized access to this order."));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("status", false, "message", "Error loading order confirmation: " + e.getMessage()));
        }
    }
}