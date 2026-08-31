package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.service.DiscountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/discounts")
public class DiscountController {

    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateCoupon(@RequestBody Map<String, Object> payload) {
        String code = (String) payload.get("code");
        Double subtotal = null;

        // 1. Safe Parsing Logic for Subtotal
        try {
            Object subtotalObj = payload.get("subtotal");

            // Check if null
            if (subtotalObj == null) {
                return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "Subtotal is missing in request"));
            }

            // Convert Object to String, sanitize (remove currency symbols/commas), then parse
            String subtotalStr = subtotalObj.toString().replaceAll("[^0-9.]", "");

            if (subtotalStr.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "Subtotal is empty"));
            }

            subtotal = Double.parseDouble(subtotalStr);

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "Invalid subtotal format: Must be a number"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "Error parsing request data"));
        }

        // 2. Validate Code via Service
        try {
            double discountAmount = discountService.calculateDiscount(code, subtotal);
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "discountAmount", discountAmount,
                    "message", "Coupon applied successfully!"
            ));
        } catch (IllegalArgumentException e) {
            // Business logic errors (expired, invalid code, etc.)
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("valid", false, "message", "Server error validating coupon"));
        }
    }
}