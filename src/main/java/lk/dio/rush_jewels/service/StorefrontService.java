package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.dto.ProductDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StorefrontService {

    private final ProductService productService;

    public StorefrontService(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Load featured products for the homepage
     * Featured = Products with "Best Seller" or "Signature" tags
     * Limit to 8 products
     */
    public List<ProductDTO> getFeaturedProducts() {
        // Load all active products
        List<ProductDTO> allProducts = productService.loadFilteredProducts(
                new String[]{}, // No category filter
                new String[]{}, // No gemstone filter
                new String[]{}, // No metal filter
                null,           // No price filter
                ""              // Context path
        );

        // Filter for featured products (Best Seller or Signature)
        List<ProductDTO> featured = allProducts.stream()
                .filter(p -> p.getTags() != null &&
                        (p.getTags().contains("Best Seller") || p.getTags().contains("Signature")))
                .limit(8)
                .collect(Collectors.toList());

        // Fallback: If no featured products, return first 8 products
        if (featured.isEmpty()) {
            featured = allProducts.stream()
                    .limit(8)
                    .collect(Collectors.toList());
        }

        return featured;
    }

    /**
     * Load new arrival products for the homepage
     * New Arrivals = Absolute latest 8 products by creation date
     */
    public List<ProductDTO> getNewArrivals() {
        // Load all active products
        List<ProductDTO> allProducts = productService.loadFilteredProducts(
                new String[]{}, // No category filter
                new String[]{}, // No gemstone filter
                new String[]{}, // No metal filter
                null,           // No price filter
                ""              // Context path
        );

        // Sort by creation date descending and limit to 8 to get the absolute latest products
        return allProducts.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(8)
                .collect(Collectors.toList());
    }

    /**
     * Get products by category for category navigation
     */
    public List<ProductDTO> getProductsByCategory(String category, int limit) {
        List<ProductDTO> allProducts = productService.loadFilteredProducts(
                new String[]{category}, // Filter by category
                new String[]{},         // No gemstone filter
                new String[]{},         // No metal filter
                null,                   // No price filter
                ""                      // Context path
        );

        return allProducts.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get sale/discounted products
     */
    public List<ProductDTO> getSaleProducts(int limit) {
        List<ProductDTO> allProducts = productService.loadFilteredProducts(
                new String[]{}, // No category filter
                new String[]{}, // No gemstone filter
                new String[]{}, // No metal filter
                null,           // No price filter
                ""              // Context path
        );

        // Filter products with discount > 0
        return allProducts.stream()
                .filter(p -> p.getDiscountPercentage() > 0)
                .sorted((a, b) -> Double.compare(b.getDiscountPercentage(), a.getDiscountPercentage()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get best selling products
     */
    public List<ProductDTO> getBestSellers(int limit) {
        List<ProductDTO> allProducts = productService.loadFilteredProducts(
                new String[]{}, // No category filter
                new String[]{}, // No gemstone filter
                new String[]{}, // No metal filter
                null,           // No price filter
                ""              // Context path
        );

        return allProducts.stream()
                .filter(p -> p.getTags() != null && p.getTags().contains("Best Seller"))
                .limit(limit)
                .collect(Collectors.toList());
    }
}