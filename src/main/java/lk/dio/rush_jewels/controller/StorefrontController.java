package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.ProductDTO;
import lk.dio.rush_jewels.service.StorefrontService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/storefront")
public class StorefrontController {

    private final StorefrontService storefrontService;

    public StorefrontController(StorefrontService storefrontService) {
        this.storefrontService = storefrontService;
    }

    /**
     * Get home page content (Featured Products + New Arrivals)
     * Returns products with varianceId for proper cart and detail page routing
     */
    @GetMapping("/home-content")
    public ResponseEntity<StorefrontDataDTO> getHomeContent() {
        List<ProductDTO> featured = storefrontService.getFeaturedProducts();
        List<ProductDTO> newArrivals = storefrontService.getNewArrivals();

        return ResponseEntity.ok(new StorefrontDataDTO(featured, newArrivals));
    }

    /**
     * Get products by category
     * Example: /api/v1/storefront/category/Rings?limit=12
     */
    @GetMapping("/category/{categoryName}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(
            @PathVariable String categoryName,
            @RequestParam(defaultValue = "12") int limit) {

        List<ProductDTO> products = storefrontService.getProductsByCategory(categoryName, limit);
        return ResponseEntity.ok(products);
    }

    /**
     * Get sale/discounted products
     * Example: /api/v1/storefront/sale?limit=8
     */
    @GetMapping("/sale")
    public ResponseEntity<List<ProductDTO>> getSaleProducts(
            @RequestParam(defaultValue = "8") int limit) {

        List<ProductDTO> products = storefrontService.getSaleProducts(limit);
        return ResponseEntity.ok(products);
    }

    /**
     * Get best seller products
     * Example: /api/v1/storefront/bestsellers?limit=8
     */
    @GetMapping("/bestsellers")
    public ResponseEntity<List<ProductDTO>> getBestSellers(
            @RequestParam(defaultValue = "8") int limit) {

        List<ProductDTO> products = storefrontService.getBestSellers(limit);
        return ResponseEntity.ok(products);
    }

    /**
     * Inner DTO class to group the home page response
     */
    public static class StorefrontDataDTO {
        public List<ProductDTO> featuredProducts;
        public List<ProductDTO> newArrivals;

        public StorefrontDataDTO(List<ProductDTO> featuredProducts, List<ProductDTO> newArrivals) {
            this.featuredProducts = featuredProducts;
            this.newArrivals = newArrivals;
        }

        // Getters for JSON serialization
        public List<ProductDTO> getFeaturedProducts() {
            return featuredProducts;
        }

        public List<ProductDTO> getNewArrivals() {
            return newArrivals;
        }
    }
}