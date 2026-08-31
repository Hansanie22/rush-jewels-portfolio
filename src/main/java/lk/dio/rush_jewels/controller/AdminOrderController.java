package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.OrderDetailDTO;
import lk.dio.rush_jewels.dto.OrderListDTO;
import lk.dio.rush_jewels.dto.ReturnListDTO;
import lk.dio.rush_jewels.service.AdminOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final AdminOrderService orderService;

    public AdminOrderController(AdminOrderService orderService) {
        this.orderService = orderService;
    }

    // 1. Get Active Orders
    @GetMapping("/list")
    public ResponseEntity<List<OrderListDTO>> getActiveOrders() {
        return ResponseEntity.ok(orderService.getActiveOrders());
    }

    // 2. Get All Returns
    @GetMapping("/returns")
    public ResponseEntity<List<ReturnListDTO>> getAllReturns() {
        return ResponseEntity.ok(orderService.getAllReturns());
    }

    // 3. Get Details
    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailDTO> getOrderDetails(@PathVariable String id) {
        return ResponseEntity.ok(orderService.getOrderDetails(id));
    }

    // 4. Update Order Status (NEW ENDPOINT)
    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable String id, @RequestBody Map<String, String> payload) {
        if (!payload.containsKey("status")) {
            return ResponseEntity.badRequest().body("Missing status value");
        }

        orderService.updateOrderStatus(id, payload.get("status"));
        return ResponseEntity.ok("Status updated successfully");
    }

    // 5. Handle Return Action
    @PostMapping("/{id}/return/action")
    public ResponseEntity<?> handleReturnAction(@PathVariable String id, @RequestBody Map<String, String> payload) {
        try {
            orderService.handleAdminReturnAction(id, payload.get("action"));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/payment-complete")
    public ResponseEntity<?> markPaymentAsComplete(@PathVariable String id) {
        try {
            orderService.updatePaymentToCompleted(id);
            return ResponseEntity.ok("Payment status updated to COMPLETED");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/pickup-payment")
    public ResponseEntity<?> processPickupPayment(@PathVariable String id) {
        try {
            orderService.processPickupPayment(id);
            return ResponseEntity.ok("Payment processed and order handed over");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/handover")
    public ResponseEntity<?> handoverOrder(@PathVariable String id) {
        try {
            orderService.handoverOnlinePickup(id);
            return ResponseEntity.ok("Order handed over successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/pos-return")
    public ResponseEntity<?> processPosReturn(@PathVariable String id, @RequestBody lk.dio.rush_jewels.dto.ReturnRequestDTO payload, @RequestParam String action) {
        try {
            orderService.processPosReturn(id, payload, action);
            return ResponseEntity.ok("POS " + action + " processed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}