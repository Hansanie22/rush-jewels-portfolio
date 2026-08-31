package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.model.Product;
import lk.dio.rush_jewels.model.ProductVariance;
import lk.dio.rush_jewels.model.Stock;
import lk.dio.rush_jewels.repository.ProductRepository;
import lk.dio.rush_jewels.repository.ProductVarianceRepository;
import lk.dio.rush_jewels.repository.StockRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductDetailsController {

    private final ProductRepository productRepository;
    private final ProductVarianceRepository productVarianceRepository;
    private final StockRepository stockRepository;
    private final lk.dio.rush_jewels.repository.ReviewRepository reviewRepository;

    // ✅ NOTE: Upload Base Path removed.
    // We now use Cloudinary URLs stored directly in the database.

    // 🏭 Warehouse Configuration
    private static final int WAREHOUSE_ID_MAIN = 1;

    // 📦 Stock Status Configuration
    private static final int OUT_OF_STOCK_STATUS_ID = 3;

    public ProductDetailsController(ProductRepository productRepository,
                                    ProductVarianceRepository productVarianceRepository,
                                    StockRepository stockRepository,
                                    lk.dio.rush_jewels.repository.ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.productVarianceRepository = productVarianceRepository;
        this.stockRepository = stockRepository;
        this.reviewRepository = reviewRepository;
    }

    /**
     * Helper method to calculate stock quantity for a specific product variance
     */
    private int calculateStockQuantity(ProductVariance pv) {
        List<Stock> stocks = stockRepository.findByProductVariance(pv);
        int currentStockQty = 0;

        for (Stock st : stocks) {
            // 🛑 STRICT FILTER: Only consider stock from Warehouse ID 1
            if (st.getWarehouse() != null && st.getWarehouse().getId() == WAREHOUSE_ID_MAIN) {
                int statusId = st.getStockStatus().getId();

                // Sum quantity for ANY status that is NOT "Out of Stock" (ID 3)
                if (statusId != OUT_OF_STOCK_STATUS_ID) {
                    currentStockQty += st.getQty();
                }
            }
        }

        return currentStockQty;
    }

    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> getProductDetails(@RequestParam("id") Integer id) {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            Product product = null;
            ProductVariance selectedVariance = null;

            // 1️⃣ Try variance ID
            Optional<ProductVariance> varianceOpt = productVarianceRepository.findById(id);
            if (varianceOpt.isPresent()) {
                selectedVariance = varianceOpt.get();
                product = selectedVariance.getProduct();
            } else {
                // 2️⃣ Try product ID
                Optional<Product> productOpt = productRepository.findById(id);
                if (productOpt.isPresent()) {
                    product = productOpt.get();
                    List<ProductVariance> variances = productVarianceRepository.findByProduct_Id(product.getId());
                    if (!variances.isEmpty()) {
                        selectedVariance = variances.get(0);
                    }
                }
            }

            if (product == null || selectedVariance == null) {
                return ResponseEntity.ok(Map.of("status", false, "message", "Product not found."));
            }

            if (product.getStatus() == null ||
                    !"Active".equalsIgnoreCase(product.getStatus().getStatus())) {
                return ResponseEntity.ok(Map.of("status", false, "message", "Product is not active."));
            }

            // ✅ Product details
            Map<String, Object> productJson = new LinkedHashMap<>();
            productJson.put("id", product.getId());
            productJson.put("name", product.getName());
            productJson.put("title", product.getTitle());
            productJson.put("description", product.getDescription());
            productJson.put("warranty", product.getWarranty());
            productJson.put("category", product.getCategory() != null ? product.getCategory().getCategory() : "");

            // ✅ Specs
            List<String> specs = new ArrayList<>();
            try {
                String specsText = product.getSpecifications();
                if (specsText != null && !specsText.trim().isEmpty()) {
                    specs = Arrays.stream(specsText.split("\\r?\\n|•|,"))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                }
            } catch (Exception ignored) {
            }
            productJson.put("specifications", specs);

            // ✅ Selected variance
            Map<String, Object> selectedVarJson = new LinkedHashMap<>();
            selectedVarJson.put("id", selectedVariance.getId());

            Double price = selectedVariance.getPrice() != null ?
                    selectedVariance.getPrice() :
                    selectedVariance.getRegularPrice();

            selectedVarJson.put("price", price);
            selectedVarJson.put("regularPrice", selectedVariance.getRegularPrice());
            selectedVarJson.put("discountPercentage",
                    selectedVariance.getDiscountPercentage() != null ?
                            selectedVariance.getDiscountPercentage() : 0.0);

            int stockQty = calculateStockQuantity(selectedVariance);
            selectedVarJson.put("stockLimit", stockQty);

            selectedVarJson.put("size", selectedVariance.getSize() != null ? selectedVariance.getSize().getSize() : null);
            selectedVarJson.put("color", selectedVariance.getColor() != null ? selectedVariance.getColor().getColor() : null);
            selectedVarJson.put("gemstone", selectedVariance.getGemstone() != null ? selectedVariance.getGemstone().getGemStone() : null);

            // ✅ All variances
            List<Map<String, Object>> allVarList = productVarianceRepository.findByProduct_Id(product.getId())
                    .stream()
                    .map(v -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", v.getId());

                        Double varPrice = v.getPrice() != null ? v.getPrice() : v.getRegularPrice();
                        m.put("price", varPrice);
                        m.put("regularPrice", v.getRegularPrice());

                        int varStockQty = calculateStockQuantity(v);
                        m.put("stockLimit", varStockQty);

                        m.put("size", v.getSize() != null ? v.getSize().getSize() : null);
                        m.put("color", v.getColor() != null ? v.getColor().getColor() : null);
                        m.put("gemstone", v.getGemstone() != null ? v.getGemstone().getGemStone() : null);
                        return m;
                    })
                    .collect(Collectors.toList());

            // ✅ Product images (Cloudinary URLs)
            List<String> images = new ArrayList<>();
            if (product.getImage1() != null) images.add(product.getImage1());
            if (product.getImage2() != null) images.add(product.getImage2());
            if (product.getImage3() != null) images.add(product.getImage3());
            if (product.getImage4() != null) images.add(product.getImage4());

            // ✅ Related products
            final Product finalProduct = product;
            List<Map<String, Object>> relatedProducts = productRepository.findAll()
                    .stream()
                    .filter(p -> Objects.equals(p.getCategory(), finalProduct.getCategory()))
                    .filter(p -> !Objects.equals(p.getId(), finalProduct.getId()))
                    .filter(p -> p.getStatus() != null && "Active".equalsIgnoreCase(p.getStatus().getStatus()))
                    .limit(4)
                    .map(p -> {
                        Map<String, Object> rel = new LinkedHashMap<>();
                        rel.put("productId", p.getId());

                        ProductVariance v = productVarianceRepository.findByProduct_Id(p.getId())
                                .stream().findFirst().orElse(null);

                        // ✅ CHANGE: Use Cloudinary URL directly
                        String relImgPath = p.getImage1() != null ? p.getImage1() : "/assets/images/fallback.png";
                        rel.put("image", relImgPath);
                        rel.put("name", p.getName());

                        if (v != null) {
                            rel.put("varianceId", v.getId());
                            Double relPrice = v.getPrice() != null ? v.getPrice() : v.getRegularPrice();
                            rel.put("price", relPrice);
                            int relStockQty = calculateStockQuantity(v);
                            rel.put("stockQty", relStockQty);
                        } else {
                            rel.put("varianceId", p.getId());
                            rel.put("price", 0.0);
                            rel.put("stockQty", 0);
                        }

                        return rel;
                    })
                    .collect(Collectors.toList());

            // ✅ Fetch Approved Reviews for ALL variances of this product
            List<ProductVariance> allProductVariances = productVarianceRepository.findByProduct_Id(product.getId());
            List<lk.dio.rush_jewels.model.Review> allReviews = new ArrayList<>();
            for (ProductVariance pv : allProductVariances) {
                allReviews.addAll(reviewRepository.findByProductVariance(pv));
            }
            
            List<Map<String, Object>> reviewsJson = allReviews.stream()
                .filter(r -> r.getStatus() != null && "Approved".equalsIgnoreCase(r.getStatus().getReviewStatus()))
                .map(r -> {
                    Map<String, Object> rMap = new LinkedHashMap<>();
                    rMap.put("rating", r.getRating());
                    rMap.put("comment", r.getComment());
                    if (r.getUser() != null) {
                        rMap.put("reviewerName", r.getUser().getFname() + " " + r.getUser().getLname());
                    } else if (r.getAdmin() != null) {
                        rMap.put("reviewerName", r.getAdmin().getFname() + " " + r.getAdmin().getLname());
                    } else {
                        rMap.put("reviewerName", "Anonymous");
                    }
                    rMap.put("date", r.getApprovedAt() != null ? r.getApprovedAt().toLocalDate().toString() : r.getCreatedAt().toLocalDate().toString());
                    return rMap;
                }).collect(Collectors.toList());

            // ✅ Final response
            response.put("status", true);
            response.put("product", productJson);
            response.put("selectedVariance", selectedVarJson);
            response.put("allVariances", allVarList);
            response.put("images", images);
            response.put("relatedProducts", relatedProducts);
            response.put("reviews", reviewsJson);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", false);
            response.put("message", "Internal Server Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}