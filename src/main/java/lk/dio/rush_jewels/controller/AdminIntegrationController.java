package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.model.Integration;
import lk.dio.rush_jewels.service.AdminIntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/integrations")
public class AdminIntegrationController {

    private final AdminIntegrationService integrationService;

    public AdminIntegrationController(AdminIntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @GetMapping
    public ResponseEntity<List<Integration>> getAllIntegrations() {
        return ResponseEntity.ok(integrationService.getAllIntegrations());
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<?> toggleStatus(@PathVariable int id, @RequestBody Map<String, Object> payload) {
        try {
            // ✅ FIX: Cast to String safely (handles null)
            String apiKey = (String) payload.get("apiKey");
            return ResponseEntity.ok(integrationService.toggleStatus(id, apiKey));
        } catch (Exception e) {
            // Returns the specific database error (e.g., "Column cannot be null")
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}