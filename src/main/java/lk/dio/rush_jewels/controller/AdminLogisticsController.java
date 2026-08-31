package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.ShipmentDTO;
import lk.dio.rush_jewels.dto.ShipmentDetailDTO;
import lk.dio.rush_jewels.service.AdminLogisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/shipments")
public class AdminLogisticsController {

    private final AdminLogisticsService logisticsService;

    public AdminLogisticsController(AdminLogisticsService logisticsService) {
        this.logisticsService = logisticsService;
    }

    @GetMapping
    public ResponseEntity<List<ShipmentDTO>> getAllShipments() {
        return ResponseEntity.ok(logisticsService.getAllShipments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentDetailDTO> getShipmentDetails(@PathVariable int id) {
        return ResponseEntity.ok(logisticsService.getShipmentDetails(id));
    }

    @PostMapping
    public ResponseEntity<?> createShipment(@RequestBody ShipmentDTO dto) {
        try {
            logisticsService.saveShipment(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<?> updateShipment(@PathVariable int id, @RequestBody ShipmentDTO dto) {
        try {
            dto.setId(id);
            logisticsService.saveShipment(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}