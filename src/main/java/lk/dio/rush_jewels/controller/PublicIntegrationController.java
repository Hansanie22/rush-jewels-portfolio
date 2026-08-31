package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.model.Integration;
import lk.dio.rush_jewels.repository.IntegrationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/integrations")
public class PublicIntegrationController {

    private final IntegrationRepository integrationRepository;

    public PublicIntegrationController(IntegrationRepository integrationRepository) {
        this.integrationRepository = integrationRepository;
    }

    @GetMapping("/active")
    public ResponseEntity<List<Integration>> getActiveIntegrations() {
        // Return only connected integrations. 
        // Note: For security, you might want to map this to a DTO to avoid exposing unnecessary fields, 
        // but since apiKey here is just a public tracking ID (like G-XXXX or Pixel ID), it's safe to send to the frontend.
        List<Integration> active = integrationRepository.findAll().stream()
                .filter(Integration::isConnected)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(active);
    }
}
