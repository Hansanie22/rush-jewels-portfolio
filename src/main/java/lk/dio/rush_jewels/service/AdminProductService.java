package lk.dio.rush_jewels.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.model.AdminAuditLog;
import lk.dio.rush_jewels.model.Product;
import lk.dio.rush_jewels.model.ProductVariance;
import lk.dio.rush_jewels.model.Status;
import lk.dio.rush_jewels.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminProductService {

    private final ProductRepository productRepository;
    private final StatusRepository statusRepository;
    private final ProductVarianceRepository varianceRepository;
    private final StockRepository stockRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final CloudinaryService cloudinaryService; // ✅ Cloudinary Service එක සම්බන්ධ කළා

    public AdminProductService(ProductRepository productRepository,
                               StatusRepository statusRepository,
                               ProductVarianceRepository varianceRepository,
                               StockRepository stockRepository,
                               AdminAuditLogRepository auditLogRepository,
                               ObjectMapper objectMapper,
                               CloudinaryService cloudinaryService) {
        this.productRepository = productRepository;
        this.statusRepository = statusRepository;
        this.varianceRepository = varianceRepository;
        this.stockRepository = stockRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
        this.cloudinaryService = cloudinaryService;
    }

    // --- READ OPERATIONS ---

    public List<ProductWithVariantCount> getAllProductsWithVariantCount() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(p -> {
            long variantCount = varianceRepository.countByProductId(p.getId());
            // ✅ හාඩ් ඩිස්ක් එකෙන් නැතුව කෙලින්ම DB එකේ තියෙන Cloudinary Link එක ගන්නවා
            String imagePath = p.getImage1();
            return new ProductWithVariantCount(p, variantCount, imagePath);
        }).collect(Collectors.toList());
    }

    public Optional<Product> getProductById(int id) {
        return productRepository.findById(id);
    }

    // --- SAVE OPERATION (CREATE/UPDATE) WITH AUDIT LOG ---

    public Product saveProduct(Product product) {
        if (product.getId() > 0) {
            // --- UPDATE LOGIC ---
            Product existingProduct = productRepository.findById(product.getId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + product.getId()));

            String oldValue = convertToJson(existingProduct);

            existingProduct.setName(product.getName());
            existingProduct.setTitle(product.getTitle());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setSpecifications(product.getSpecifications());
            existingProduct.setWarranty(product.getWarranty());

            if (product.getCategory() != null) {
                existingProduct.setCategory(product.getCategory());
            }

            Product savedProduct = productRepository.save(existingProduct);
            logAction("UPDATE", "product", String.valueOf(savedProduct.getId()), oldValue, savedProduct);
            return savedProduct;

        } else {
            // --- CREATE LOGIC ---
            product.setCreatedAt(new Date());

            if (product.getStatus() == null) {
                Status activeStatus = statusRepository.findById(1)
                        .orElseThrow(() -> new RuntimeException("Default status (Active/1) not found in DB"));
                product.setStatus(activeStatus);
            }

            Product savedProduct = productRepository.save(product);
            logAction("CREATE", "product", String.valueOf(savedProduct.getId()), null, savedProduct);
            return savedProduct;
        }
    }

    // --- IMAGE SAVING LOGIC (CLOUDINARY) ---

    public void saveProductImages(int productId, MultipartFile img1, MultipartFile img2, MultipartFile img3, MultipartFile img4) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // ✅ Image 1 Upload
        if (img1 != null && !img1.isEmpty()) {
            String url = cloudinaryService.uploadImage(img1);
            product.setImage1(url);
        }

        // ✅ Image 2 Upload
        if (img2 != null && !img2.isEmpty()) {
            String url = cloudinaryService.uploadImage(img2);
            product.setImage2(url);
        }

        // ✅ Image 3 Upload
        if (img3 != null && !img3.isEmpty()) {
            String url = cloudinaryService.uploadImage(img3);
            product.setImage3(url);
        }

        // ✅ Image 4 Upload
        if (img4 != null && !img4.isEmpty()) {
            String url = cloudinaryService.uploadImage(img4);
            product.setImage4(url);
        }

        productRepository.save(product); // ලින්ක් ටික ඩේටාබේස් එකේ සේව් කරනවා
    }

    // --- STATUS UPDATE ---

    public void updateStatus(int productId, boolean active) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        String oldValue = convertToJson(product);

        Status status = statusRepository.findById(active ? 1 : 2)
                .orElseThrow(() -> new RuntimeException("Status not found"));
        product.setStatus(status);

        Product savedProduct = productRepository.save(product);

        logAction("UPDATE_STATUS", "product", String.valueOf(productId), oldValue, savedProduct);
    }

    // --- AUDIT LOG HELPER METHODS ---

    private void logAction(String action, String table, String recordId, String oldVal, Object newValObj) {
        try {
            String newVal = convertToJson(newValObj);
            AdminAuditLog log = new AdminAuditLog(
                    action,
                    table,
                    recordId,
                    oldVal,
                    newVal,
                    LocalDateTime.now()
            );
            auditLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Audit Log Failed: " + e.getMessage());
        }
    }

    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(sanitizeForAudit(object));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private Object sanitizeForAudit(Object obj) {
        if (obj instanceof Product) {
            Product p = (Product) obj;
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("category", p.getCategory() != null ? p.getCategory().getCategory() : null);
            map.put("status", p.getStatus() != null ? p.getStatus().getStatus() : null);
            return map;
        }
        return obj;
    }

    // --- VARIANCE LOGIC ---

    public List<VarianceDTO> getVariantsWithStock(int productId) {
        List<ProductVariance> variances = varianceRepository.findByProduct_Id(productId);

        return variances.stream().map(v -> {
            Long qtyLong = stockRepository.sumAvailableStockByProductVarianceId(v.getId());
            int qty = (qtyLong != null) ? qtyLong.intValue() : 0;
            return new VarianceDTO(v, qty);
        }).collect(Collectors.toList());
    }

    // --- DTO CLASSES ---

    public static class ProductWithVariantCount {
        private final Product product;
        private final long variantCount;
        private final String image;

        public ProductWithVariantCount(Product product, long variantCount, String image) {
            this.product = product;
            this.variantCount = variantCount;
            this.image = image;
        }
        public int getId() { return product.getId(); }
        public String getName() { return product.getName(); }
        public String getTitle() { return product.getTitle(); }
        public String getSpecifications() { return product.getSpecifications(); }
        public String getWarranty() { return product.getWarranty(); }
        public Object getCategory() { return product.getCategory(); }
        public Object getStatus() { return product.getStatus(); }
        public long getVariantCount() { return variantCount; }
        public String getImage() { return image; }
    }

    public static class VarianceDTO {
        private int id;
        private int stockQty;
        private SizeDTO size;
        private ColorDTO color;
        private GemstoneDTO gemstone;

        public VarianceDTO(ProductVariance v, int stockQty) {
            this.id = v.getId();
            this.stockQty = stockQty;
            if (v.getSize() != null) this.size = new SizeDTO(v.getSize().getId(), v.getSize().getSize());
            if (v.getColor() != null) this.color = new ColorDTO(v.getColor().getId(), v.getColor().getColor());
            if (v.getGemstone() != null) this.gemstone = new GemstoneDTO(v.getGemstone().getId(), v.getGemstone().getGemStone());
        }
        public int getId() { return id; }
        public int getStockQty() { return stockQty; }
        public SizeDTO getSize() { return size; }
        public ColorDTO getColor() { return color; }
        public GemstoneDTO getGemstone() { return gemstone; }
    }

    public static class SizeDTO {
        private int id;
        private String size;
        public SizeDTO(int id, String size) { this.id = id; this.size = size; }
        public int getId() { return id; }
        public String getSize() { return size; }
    }

    public static class ColorDTO {
        private int id;
        private String name;
        public ColorDTO(int id, String name) { this.id = id; this.name = name; }
        public int getId() { return id; }
        public String getName() { return name; }
    }

    public static class GemstoneDTO {
        private int id;
        private String name;
        public GemstoneDTO(int id, String name) { this.id = id; this.name = name; }
        public int getId() { return id; }
        public String getName() { return name; }
    }
}