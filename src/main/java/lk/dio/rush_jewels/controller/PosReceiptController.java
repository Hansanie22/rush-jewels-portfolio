package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.PosReceiptDTO;
import lk.dio.rush_jewels.service.PosReceiptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pos/receipt")
public class PosReceiptController {

    private final PosReceiptService posReceiptService;

    public PosReceiptController(PosReceiptService posReceiptService) {
        this.posReceiptService = posReceiptService;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getReceipt(@PathVariable String orderId) {
        try {
            PosReceiptDTO receipt = posReceiptService.generateReceipt(orderId);
            return ResponseEntity.ok(receipt);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Could not generate receipt: " + e.getMessage());
        }
    }
}
