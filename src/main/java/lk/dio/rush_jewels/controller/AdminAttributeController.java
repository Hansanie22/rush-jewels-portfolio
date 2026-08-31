package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.model.Color;
import lk.dio.rush_jewels.model.Gemstone;
import lk.dio.rush_jewels.model.Size;
import lk.dio.rush_jewels.service.AdminAttributeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/attributes")
public class AdminAttributeController {

    private final AdminAttributeService attributeService;

    public AdminAttributeController(AdminAttributeService attributeService) {
        this.attributeService = attributeService;
    }

    // ==========================================
    // SIZE ENDPOINTS
    // ==========================================
    @GetMapping("/sizes")
    public ResponseEntity<List<SizeResponseDTO>> getAllSizes() {
        List<Size> sizes = attributeService.getAllSizes();
        List<SizeResponseDTO> dtos = sizes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/sizes")
    public ResponseEntity<?> createSize(@RequestBody Size size) {
        try {
            Size savedSize = attributeService.saveSize(size);
            // ✅ CRITICAL FIX: Return DTO, not Entity
            return ResponseEntity.ok(convertToDto(savedSize));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/sizes/{id}/update")
    public ResponseEntity<?> updateSize(@PathVariable int id, @RequestBody Size size) {
        try {
            Size updatedSize = attributeService.updateSize(id, size);
            // ✅ CRITICAL FIX: Return DTO, not Entity
            return ResponseEntity.ok(convertToDto(updatedSize));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Helper: Convert Entity to DTO to remove Proxies
    private SizeResponseDTO convertToDto(Size s) {
        return new SizeResponseDTO(
                s.getId(),
                s.getSize(),
                (s.getCategory() != null) ? s.getCategory().getCategory() : "General",
                (s.getCategory() != null) ? s.getCategory().getId() : null
        );
    }

    // ==========================================
    // COLOR (METAL) ENDPOINTS
    // ==========================================
    @GetMapping("/colors")
    public ResponseEntity<List<Color>> getAllColors() {
        return ResponseEntity.ok(attributeService.getAllColors());
    }

    @PostMapping("/colors")
    public ResponseEntity<?> createColor(@RequestBody Color color) {
        try {
            return ResponseEntity.ok(attributeService.saveColor(color));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/colors/{id}/update")
    public ResponseEntity<?> updateColor(@PathVariable int id, @RequestBody Color color) {
        try {
            return ResponseEntity.ok(attributeService.updateColor(id, color));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==========================================
    // GEMSTONE ENDPOINTS
    // ==========================================
    @GetMapping("/gemstones")
    public ResponseEntity<List<Gemstone>> getAllGemstones() {
        return ResponseEntity.ok(attributeService.getAllGemstones());
    }

    @PostMapping("/gemstones")
    public ResponseEntity<?> createGemstone(@RequestBody Gemstone gemstone) {
        try {
            return ResponseEntity.ok(attributeService.saveGemstone(gemstone));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/gemstones/{id}/update")
    public ResponseEntity<?> updateGemstone(@PathVariable int id, @RequestBody Gemstone gemstone) {
        try {
            return ResponseEntity.ok(attributeService.updateGemstone(id, gemstone));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- DTO for Size Response ---
    public static class SizeResponseDTO {
        public int id;
        public String size;
        public String categoryName;
        public Integer categoryId;

        public SizeResponseDTO(int id, String size, String categoryName, Integer categoryId) {
            this.id = id;
            this.size = size;
            this.categoryName = categoryName;
            this.categoryId = categoryId;
        }
    }
}