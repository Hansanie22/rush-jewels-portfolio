package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.model.PaymentMethod;
import lk.dio.rush_jewels.service.AdminFinanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/finance")
public class AdminFinanceController {

    private final AdminFinanceService financeService;

    public AdminFinanceController(AdminFinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentMethod>> getPaymentMethods() {
        return ResponseEntity.ok(financeService.getAllPaymentMethods());
    }

    @PostMapping("/payments/{id}/toggle")
    public ResponseEntity<?> togglePayment(@PathVariable int id, @RequestBody Map<String, Boolean> payload) {
        return ResponseEntity.ok(financeService.togglePaymentMethod(id, payload.get("active")));
    }

    @GetMapping("/tax")
    public ResponseEntity<Map<String, String>> getTaxRate() {
        return ResponseEntity.ok(Map.of("rate", financeService.getTaxRate()));
    }

    @PostMapping("/tax")
    public ResponseEntity<?> updateTaxRate(@RequestBody Map<String, String> payload) {
        financeService.updateTaxRate(payload.get("rate"));
        return ResponseEntity.ok().build();
    }
}