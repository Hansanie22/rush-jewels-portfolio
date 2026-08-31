package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.StockTransferDTO;
import lk.dio.rush_jewels.dto.WarehouseActivityDTO;
import lk.dio.rush_jewels.dto.WarehouseStatsDTO;
import lk.dio.rush_jewels.model.Warehouse;
import lk.dio.rush_jewels.repository.WarehouseRepository;
import lk.dio.rush_jewels.service.AdminWarehouseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/warehouse")
public class AdminWarehouseController {

    private final AdminWarehouseService warehouseService;
    private final WarehouseRepository warehouseRepository;

    public AdminWarehouseController(AdminWarehouseService warehouseService, WarehouseRepository warehouseRepository) {
        this.warehouseService = warehouseService;
        this.warehouseRepository = warehouseRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<WarehouseStatsDTO> getStats() {
        return ResponseEntity.ok(warehouseService.getStats());
    }

    @GetMapping("/activity")
    public ResponseEntity<List<WarehouseActivityDTO>> getActivity() {
        return ResponseEntity.ok(warehouseService.getRecentActivity());
    }

    @GetMapping("/locations")
    public ResponseEntity<List<Warehouse>> getWarehouses() {
        return ResponseEntity.ok(warehouseRepository.findAll());
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferStock(@RequestBody StockTransferDTO dto) {
        try {
            warehouseService.transferStock(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}