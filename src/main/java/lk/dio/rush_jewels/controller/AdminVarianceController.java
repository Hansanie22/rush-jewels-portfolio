package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.ProductVarianceRequestDTO;
import lk.dio.rush_jewels.dto.ProductVarianceResponseDTO;
import lk.dio.rush_jewels.service.AdminVarianceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/variances")
public class AdminVarianceController {

    private final AdminVarianceService varianceService;

    public AdminVarianceController(AdminVarianceService varianceService) {
        this.varianceService = varianceService;
    }

    @GetMapping
    public ResponseEntity<List<ProductVarianceResponseDTO>> getAllVariances() {
        return ResponseEntity.ok(varianceService.getAllVariances());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductVarianceResponseDTO> getVariance(@PathVariable int id) {
        return ResponseEntity.ok(varianceService.getVarianceDtoById(id));
    }

    @PostMapping
    public ResponseEntity<?> createVariance(@RequestBody ProductVarianceRequestDTO dto) {
        try {
            varianceService.saveVariance(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<?> updateVariance(@PathVariable int id, @RequestBody ProductVarianceRequestDTO dto) {
        try {
            varianceService.updateVariance(id, dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable int id, @RequestBody StatusUpdateRequest request) {
        varianceService.updateStatus(id, request.isActive());
        return ResponseEntity.ok().build();
    }

    public static class StatusUpdateRequest {
        private boolean active;
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
}