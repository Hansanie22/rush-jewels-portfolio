package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.model.Province;
import lk.dio.rush_jewels.service.ProvinceService; // Import Service
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProvinceController {

    private final ProvinceService provinceService; // Inject Service

    public ProvinceController(ProvinceService provinceService) {
        this.provinceService = provinceService;
    }

    @GetMapping("/provinces")
    public ResponseEntity<?> getProvinces(
            // --- UPDATED: Accept countryId parameter ---
            @RequestParam(name = "countryId", required = false) Integer countryId
    ) {
        List<Province> provinces;
        if (countryId != null) {
            // --- UPDATED: Call service method ---
            provinces = provinceService.getProvincesByCountry(countryId);
        } else {
            // Return empty list if no country is selected
            provinces = Collections.emptyList();
        }

        // --- UPDATED: Return in Map format JS expects ---
        return ResponseEntity.ok(Map.of("provinces", provinces));
    }
}