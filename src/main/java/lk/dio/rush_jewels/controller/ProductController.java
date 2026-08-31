package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.ProductDTO;
import lk.dio.rush_jewels.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Product operations
 *
 * ✅ STOCK CALCULATION NOTE:
 * This controller uses ProductService.loadFilteredProducts() which applies
 * the correct stock logic in mapVarianceToDTO():
 * - Only counts stock from warehouse_id = 1
 * - Excludes stock_status_id = 3 (Out of Stock)
 * - All other statuses are considered available stock
 */
@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Load filtered products
     *
     * @param categories Comma-separated category names (e.g., "rings,necklaces")
     * @param gemstones Comma-separated gemstone names (e.g., "diamond,ruby")
     * @param metals Comma-separated metal/color names (e.g., "gold,silver")
     * @param maxPrice Maximum price filter
     * @param request HTTP request for context path
     * @return JSON response with filtered products and total count
     *
     * ✅ Stock quantities in the response are correctly calculated:
     *    - Only from warehouse_id = 1
     *    - Excluding stock_status_id = 3
     */
    @GetMapping("/products")
    public ResponseEntity<Map<String, Object>> loadProducts(
            @RequestParam(value = "categories", required = false) String categories,
            @RequestParam(value = "gemstones", required = false) String gemstones,
            @RequestParam(value = "metals", required = false) String metals,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            HttpServletRequest request) {

        // Parse CSV strings into arrays, handling nulls safely
        String[] categoryFilters = categories != null && !categories.isEmpty()
                ? categories.split(",")
                : new String[0];
        String[] gemstoneFilters = gemstones != null && !gemstones.isEmpty()
                ? gemstones.split(",")
                : new String[0];
        String[] metalFilters = metals != null && !metals.isEmpty()
                ? metals.split(",")
                : new String[0];

        // Get context path for building image URLs
        String contextPath = request.getContextPath();

        try {
            // ✅ ProductService.loadFilteredProducts() handles stock calculation correctly
            List<ProductDTO> products = productService.loadFilteredProducts(
                    categoryFilters,
                    gemstoneFilters,
                    metalFilters,
                    maxPrice,
                    contextPath
            );

            Map<String, Object> response = new HashMap<>();
            response.put("products", products);
            response.put("totalProducts", products.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Log and return error response
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error loading products: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}