package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.model.CourierService;
import lk.dio.rush_jewels.repository.CourierServiceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courier")
public class CourierController {

    private final CourierServiceRepository courierServiceRepository;

    public CourierController(CourierServiceRepository courierServiceRepository) {
        this.courierServiceRepository = courierServiceRepository;
    }

    @GetMapping("/branches")
    public ResponseEntity<?> getCourierBranches() {
        try {
            int companyId = 1;
            List<CourierService> branches = courierServiceRepository.findByCourierCompanyId(companyId);

            // Because of @JsonIgnore in the model, this will now work perfectly
            return ResponseEntity.ok(Map.of(
                    "status", true,
                    "branches", branches
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("status", false, "message", "Error loading branches"));
        }
    }
}