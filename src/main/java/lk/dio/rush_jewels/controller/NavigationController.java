package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.CollectionDTO;
import lk.dio.rush_jewels.service.CollectionService;
import lk.dio.rush_jewels.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/navigation")
public class NavigationController {

    private final ProductService productService;
    private final CollectionService collectionService;

    public NavigationController(ProductService productService, CollectionService collectionService) {
        this.productService = productService;
        this.collectionService = collectionService;
    }

    @GetMapping("/init")
    public ResponseEntity<Map<String, Object>> getNavigationData() {
        Map<String, Object> response = new HashMap<>();

        // 1. Fetch Active Categories (Strings)
        List<String> categories = productService.getActiveCategories();
        response.put("categories", categories);

        // 2. Fetch Active Collections (Full DTOs for flexibility)
        List<CollectionDTO> collections = collectionService.getActiveCollections();
        response.put("collections", collections);

        return ResponseEntity.ok(response);
    }
}