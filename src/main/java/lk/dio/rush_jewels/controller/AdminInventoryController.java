package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.InventoryDTOs;
import lk.dio.rush_jewels.service.AdminInventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventory")
public class AdminInventoryController {

    private final AdminInventoryService inventoryService;

    public AdminInventoryController(AdminInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // ✅ Updated: Accepts optional warehouseId (defaults to 1 for backward compatibility)
    @GetMapping
    public ResponseEntity<List<InventoryDTOs.InventoryItemDTO>> getInventory(
            @RequestParam(defaultValue = "1") int warehouseId) {
        return ResponseEntity.ok(inventoryService.getInventoryByWarehouse(warehouseId));
    }

    @PostMapping("/adjust")
    public ResponseEntity<?> adjustStock(@RequestBody InventoryDTOs.StockAdjustmentRequestDTO dto) {
        try {
            inventoryService.adjustStock(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}