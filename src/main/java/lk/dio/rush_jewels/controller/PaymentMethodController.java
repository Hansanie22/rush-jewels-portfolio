package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.PaymentMethodDTO;
import lk.dio.rush_jewels.service.PaymentMethodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment-methods")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    public PaymentMethodController(PaymentMethodService paymentMethodService) {
        this.paymentMethodService = paymentMethodService;
    }

    @GetMapping
    public ResponseEntity<?> getPaymentMethods() {
        try {
            List<PaymentMethodDTO> methods = paymentMethodService.getAllPaymentMethods();
            return ResponseEntity.ok(Map.of("status", true, "methods", methods));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("status", false, "message", "Error fetching payment methods"));
        }
    }
}