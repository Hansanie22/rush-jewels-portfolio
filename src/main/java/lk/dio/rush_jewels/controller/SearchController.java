package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.SearchProductDTO;
import lk.dio.rush_jewels.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    // Maps to /api/search/products?q=query
    @GetMapping("/search/products")
    public ResponseEntity<?> searchProducts(
            @RequestParam(value = "q", required = false) String query) {

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        try {
            List<SearchProductDTO> results = searchService.search(query);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Error searching: " + e.getMessage()));
        }
    }

    // Maps to /api/search/suggestions?q= (Optional query)
    @GetMapping("/search/suggestions")
    public ResponseEntity<Map<String, List<SearchProductDTO>>> getSearchSuggestions(
            @RequestParam(value = "q", required = false) String query) {
        try {
            // Pass the query (can be null/empty or actual text)
            Map<String, List<SearchProductDTO>> suggestions = searchService.getSearchSuggestions(query);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new HashMap<>());
        }
    }
}