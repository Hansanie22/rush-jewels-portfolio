package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.CollectionSetDTO;
import lk.dio.rush_jewels.dto.SetItemDTO;
import lk.dio.rush_jewels.service.AdminCollectionSetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/collection-sets")
public class AdminCollectionSetController {

    private final AdminCollectionSetService setService;

    public AdminCollectionSetController(AdminCollectionSetService setService) {
        this.setService = setService;
    }

    @GetMapping
    public ResponseEntity<List<CollectionSetDTO>> getAllSets() {
        return ResponseEntity.ok(setService.getAllSets());
    }

    @PostMapping("/{collectionId}")
    public ResponseEntity<?> saveSet(@PathVariable int collectionId, @RequestBody List<SetItemDTO> items) {
        try {
            setService.saveCollectionSet(collectionId, items);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}