package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.ShippingDTO;
import lk.dio.rush_jewels.service.ShippingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shipping-methods")
public class ShippingController {

    private final ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @GetMapping
    public ResponseEntity<?> getShippingMethods() {
        try {
            List<ShippingDTO> methods = shippingService.getAllShippingMethods();
            return ResponseEntity.ok(Map.of("status", true, "methods", methods));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("status", false, "message", "Error fetching shipping methods"));
        }
    }
}