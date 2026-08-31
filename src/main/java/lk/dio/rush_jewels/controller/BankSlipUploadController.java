package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.service.BankSlipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/bank-slips")
public class BankSlipUploadController {

    private final BankSlipService bankSlipService;

    public BankSlipUploadController(BankSlipService bankSlipService) {
        this.bankSlipService = bankSlipService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadSlip(@RequestParam("orderId") String orderId,
                                        @RequestParam("file") MultipartFile file) {
        try {
            String url = bankSlipService.uploadSlipForOrder(orderId, file);
            return ResponseEntity.ok("Slip uploaded successfully. URL: " + url);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to upload slip: " + e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifySlip(@RequestParam("orderId") String orderId,
                                        @RequestParam("status") String status) {
        try {
            bankSlipService.verifySlip(orderId, status);
            return ResponseEntity.ok("Slip " + status.toLowerCase() + " successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to verify slip: " + e.getMessage());
        }
    }
}
