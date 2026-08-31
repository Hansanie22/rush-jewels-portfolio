package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.model.City;
import lk.dio.rush_jewels.service.CityService; // <-- Import the SERVICE
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping("/cities")
    public ResponseEntity<?> getCities(
            @RequestParam(name = "provinceId", required = false) Integer provinceId
    ) {
        List<City> cities;
        if (provinceId != null) {
            // --- Call the SERVICE ---
            cities = cityService.getCitiesByProvince(provinceId);
        } else {
            // Return empty list if no province is selected
            cities = Collections.emptyList();
        }

        // --- Return in Map format JS expects ---
        return ResponseEntity.ok(Map.of("cities", cities));
    }
}