package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.CollectionDTO;
import lk.dio.rush_jewels.service.CollectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for Collection operations
 *
 * ✅ STOCK CALCULATION NOTE:
 * This controller uses CollectionService methods which apply
 * the correct stock logic in mapToDTO():
 * - Only counts stock from warehouse_id = 1
 * - Excludes stock_status_id = 3 (Out of Stock)
 * - All other statuses are considered available stock
 */
@RestController
@RequestMapping("/api")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    /**
     * Get all active collections with optional search filter
     *
     * @param search Optional search term to filter by name or description
     * @return JSON response with collections array and total count
     *
     * ✅ Stock quantities are correctly calculated per collection:
     *    - Only from warehouse_id = 1
     *    - Excluding stock_status_id = 3
     */
    @GetMapping("/collections")
    public ResponseEntity<Map<String, Object>> getAllCollections(
            @RequestParam(value = "search", required = false) String search) {

        try {
            // ✅ CollectionService.getActiveCollections() handles stock calculation correctly
            List<CollectionDTO> collections = collectionService.getActiveCollections();

            // Apply search filter if provided
            if (search != null && !search.isEmpty()) {
                String searchLower = search.toLowerCase();
                collections = collections.stream()
                        .filter(c -> c.getName().toLowerCase().contains(searchLower) ||
                                c.getDescription().toLowerCase().contains(searchLower))
                        .collect(Collectors.toList());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("collections", collections);
            response.put("totalCollections", collections.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error loading collections");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get detailed information for a specific collection
     *
     * @param id Collection ID
     * @return JSON response with collection details, stock quantity, images, and related collections
     *
     * ✅ Stock quantities are correctly calculated:
     *    - Main collection: Only warehouse_id = 1, excluding stock_status_id = 3
     *    - Related collections: Same logic applied via CollectionService
     */
    @GetMapping("/collections/details")
    public ResponseEntity<Map<String, Object>> getCollectionDetails(@RequestParam("id") int id) {
        try {
            // ✅ CollectionService.getCollectionById() handles stock calculation correctly
            CollectionDTO collection = collectionService.getCollectionById(id);

            if (collection == null) {
                return ResponseEntity.notFound().build();
            }

            // ✅ CollectionService.getRelatedCollections() also handles stock correctly
            List<CollectionDTO> related = collectionService.getRelatedCollections(id);

            Map<String, Object> response = new HashMap<>();
            response.put("status", true);
            response.put("collection", collection);

            // Stock quantity is already correctly calculated in CollectionService.mapToDTO()
            // Lines 91-99: filters by warehouse_id = 1 AND excludes stock_status_id = 3
            response.put("stockQty", collection.getStockLimit());

            // Images are populated in mapToDTO() lines 106-111
            response.put("images", collection.getImages());

            response.put("relatedCollections", related);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", false);
            errorResponse.put("message", "Error loading details");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}