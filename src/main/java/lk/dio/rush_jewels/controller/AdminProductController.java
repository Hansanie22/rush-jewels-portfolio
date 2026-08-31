package lk.dio.rush_jewels.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.model.Category;
import lk.dio.rush_jewels.model.Product;
import lk.dio.rush_jewels.repository.CategoryRepository;
import lk.dio.rush_jewels.service.AdminProductService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final AdminProductService productService;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper; // Required to parse the JSON string part of FormData

    public AdminProductController(AdminProductService productService,
                                  CategoryRepository categoryRepository,
                                  ObjectMapper objectMapper) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.objectMapper = objectMapper;
    }

    // --- READ OPERATIONS ---

    @GetMapping
    public ResponseEntity<List<AdminProductService.ProductWithVariantCount>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProductsWithVariantCount());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable int id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- CREATE OPERATION (Multipart/Form-Data) ---

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> createProduct(
            @RequestPart("product") String productJson, // Receive JSON as String
            @RequestPart(value = "image1", required = false) MultipartFile image1,
            @RequestPart(value = "image2", required = false) MultipartFile image2,
            @RequestPart(value = "image3", required = false) MultipartFile image3,
            @RequestPart(value = "image4", required = false) MultipartFile image4
    ) throws IOException {

        // 1. Convert JSON String to Product Object
        Product product = objectMapper.readValue(productJson, Product.class);

        // 2. Save Product to DB (Gets ID and sets CreatedAt)
        Product savedProduct = productService.saveProduct(product);

        // 3. Save Images using the new ID (Create folder if needed)
        productService.saveProductImages(savedProduct.getId(), image1, image2, image3, image4);

        return ResponseEntity.ok(savedProduct);
    }

    // --- UPDATE OPERATION (Multipart/Form-Data) ---

    @PostMapping(value = "/{id}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> updateProduct(
            @PathVariable int id,
            @RequestPart("product") String productJson,
            @RequestPart(value = "image1", required = false) MultipartFile image1,
            @RequestPart(value = "image2", required = false) MultipartFile image2,
            @RequestPart(value = "image3", required = false) MultipartFile image3,
            @RequestPart(value = "image4", required = false) MultipartFile image4
    ) throws IOException {

        // 1. Convert JSON String
        Product product = objectMapper.readValue(productJson, Product.class);
        product.setId(id); // Ensure ID matches path

        // 2. Save/Update Product Info (Merge with existing DB record)
        Product updatedProduct = productService.saveProduct(product);

        // 3. Save/Overwrite Images (Only if provided)
        productService.saveProductImages(id, image1, image2, image3, image4);

        return ResponseEntity.ok(updatedProduct);
    }

    // --- STATUS & AUXILIARY OPERATIONS ---

    @PostMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable int id, @RequestBody StatusUpdateRequest request) {
        productService.updateStatus(id, request.isActive());
        return ResponseEntity.ok().build();
    }

    public static class StatusUpdateRequest {
        private boolean active;
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getCategories(@RequestParam(required = false) String status) {
        if ("active".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(categoryRepository.findByStatusId(1));
        }
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @GetMapping("/{id}/variants")
    public ResponseEntity<List<AdminProductService.VarianceDTO>> getVariants(@PathVariable int id) {
        return ResponseEntity.ok(productService.getVariantsWithStock(id));
    }
}