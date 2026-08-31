package lk.dio.rush_jewels.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.dto.ProductVarianceRequestDTO;
import lk.dio.rush_jewels.dto.ProductVarianceResponseDTO;
import lk.dio.rush_jewels.model.*;
import lk.dio.rush_jewels.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminVarianceService {

    private final ProductVarianceRepository varianceRepository;
    private final ProductRepository productRepository;
    private final SizeRepository sizeRepository;
    private final ColorRepository colorRepository;
    private final GemstoneRepository gemstoneRepository;
    private final StatusRepository statusRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final StockRepository stockRepository;
    private final StockStatusRepository stockStatusRepository;
    private final WarehouseRepository warehouseRepository; // ✅ Required for Warehouse Logic
    private final ObjectMapper objectMapper;

    public AdminVarianceService(ProductVarianceRepository varianceRepository,
                                ProductRepository productRepository,
                                SizeRepository sizeRepository,
                                ColorRepository colorRepository,
                                GemstoneRepository gemstoneRepository,
                                StatusRepository statusRepository,
                                AdminAuditLogRepository auditLogRepository,
                                StockRepository stockRepository,
                                StockStatusRepository stockStatusRepository,
                                WarehouseRepository warehouseRepository, // ✅ Injected
                                ObjectMapper objectMapper) {
        this.varianceRepository = varianceRepository;
        this.productRepository = productRepository;
        this.sizeRepository = sizeRepository;
        this.colorRepository = colorRepository;
        this.gemstoneRepository = gemstoneRepository;
        this.statusRepository = statusRepository;
        this.auditLogRepository = auditLogRepository;
        this.stockRepository = stockRepository;
        this.stockStatusRepository = stockStatusRepository;
        this.warehouseRepository = warehouseRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<ProductVarianceResponseDTO> getAllVariances() {
        return varianceRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductVariance getVarianceById(int id) {
        return varianceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variance not found"));
    }

    public ProductVarianceResponseDTO getVarianceDtoById(int id) {
        return convertToDTO(getVarianceById(id));
    }

    public ProductVariance saveVariance(ProductVarianceRequestDTO dto) {
        ProductVariance variance = new ProductVariance();
        mapDtoToEntity(dto, variance);

        // Default Status: Active (1)
        Status status = statusRepository.findById(1).orElseThrow(() -> new RuntimeException("Active Status not found"));
        variance.setStatus(status);

        ProductVariance saved = varianceRepository.save(variance);

        // ✅ AUTOMATICALLY CREATE STOCK RECORDS (Factory & Shop)
        createInitialStock(saved);

        // Audit Log
        logAction("CREATE", "product_variance", String.valueOf(saved.getId()), null, saved);

        return saved;
    }

    public ProductVariance updateVariance(int id, ProductVarianceRequestDTO dto) {
        ProductVariance existing = getVarianceById(id);

        // Capture Snapshot
        String oldValue = convertToJson(existing);

        mapDtoToEntity(dto, existing);
        ProductVariance saved = varianceRepository.save(existing);

        // Audit Log
        logAction("UPDATE", "product_variance", String.valueOf(saved.getId()), oldValue, saved);

        return saved;
    }

    public void updateStatus(int id, boolean active) {
        ProductVariance variance = getVarianceById(id);

        String oldValue = convertToJson(variance);

        Status status = statusRepository.findById(active ? 1 : 2).orElseThrow(() -> new RuntimeException("Status not found"));
        variance.setStatus(status);
        ProductVariance saved = varianceRepository.save(variance);

        logAction("UPDATE_STATUS", "product_variance", String.valueOf(id), oldValue, saved);
    }

    // --- Helper: Create Initial Stock for Two Locations ---
    private void createInitialStock(ProductVariance variance) {
        // 1. Ensure Warehouse 1 (Factory) exists and create stock
        Warehouse factory = warehouseRepository.findById(1)
                .orElseGet(() -> warehouseRepository.save(new Warehouse(1, "Factory", null)));
        createStockForWarehouse(variance, factory);

        // 2. Ensure Warehouse 2 (Shop) exists and create stock
        Warehouse shop = warehouseRepository.findById(2)
                .orElseGet(() -> warehouseRepository.save(new Warehouse(2, "Shop", null)));
        createStockForWarehouse(variance, shop);
    }

    private void createStockForWarehouse(ProductVariance variance, Warehouse warehouse) {
        Stock stock = new Stock();
        stock.setProductVariance(variance);
        stock.setQty(0); // Initial qty is 0
        stock.setWarehouse(warehouse); // ✅ Set specific warehouse

        // Set Status to 'Out of Stock' (ID 3)
        StockStatus outOfStock = stockStatusRepository.findById(3)
                .orElseThrow(() -> new RuntimeException("Stock Status 'Out of Stock' (ID 3) not found"));
        stock.setStockStatus(outOfStock);

        stockRepository.save(stock);
    }

    // --- Helpers ---

    private void mapDtoToEntity(ProductVarianceRequestDTO dto, ProductVariance variance) {
        variance.setProduct(productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found")));

        // Handle Nullable Attributes
        variance.setSize(dto.getSizeId() != null ? sizeRepository.findById(dto.getSizeId()).orElse(null) : null);
        variance.setColor(dto.getColorId() != null ? colorRepository.findById(dto.getColorId()).orElse(null) : null);
        variance.setGemstone(dto.getGemstoneId() != null ? gemstoneRepository.findById(dto.getGemstoneId()).orElse(null) : null);

        // Price Logic
        double regular = dto.getRegularPrice();
        double discount = dto.getDiscountPercentage() != null ? dto.getDiscountPercentage() : 0.0;
        double finalPrice = regular - (regular * discount / 100);

        variance.setRegularPrice(regular);
        variance.setDiscountPercentage(discount);
        variance.setPrice(finalPrice);
        variance.setStockLimit(dto.getStockLimit());
    }

    private ProductVarianceResponseDTO convertToDTO(ProductVariance v) {
        return new ProductVarianceResponseDTO(
                v.getId(),
                v.getProduct().getId(),
                v.getProduct().getName(),
                v.getSize() != null ? v.getSize().getId() : null,
                v.getSize() != null ? v.getSize().getSize() : "-",
                v.getColor() != null ? v.getColor().getId() : null,
                v.getColor() != null ? v.getColor().getColor() : "-",
                v.getGemstone() != null ? v.getGemstone().getId() : null,
                v.getGemstone() != null ? v.getGemstone().getGemStone() : "-",
                v.getPrice() != null ? v.getPrice() : 0.0,
                v.getRegularPrice(),
                v.getDiscountPercentage() != null ? v.getDiscountPercentage() : 0.0,
                v.getStockLimit(),
                v.getStatus().getId(),
                v.getStatus().getStatus()
        );
    }

    // --- Audit Log Helpers ---

    private void logAction(String action, String table, String recordId, String oldValue, Object newValueObj) {
        try {
            String newValue = convertToJson(newValueObj);
            AdminAuditLog log = new AdminAuditLog(action, table, recordId, oldValue, newValue, LocalDateTime.now());
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
        if (obj instanceof ProductVariance) {
            ProductVariance v = (ProductVariance) obj;
            Map<String, Object> map = new HashMap<>();
            map.put("id", v.getId());
            map.put("price", v.getPrice() != null ? v.getPrice() : 0.0);
            map.put("stock", v.getStockLimit());
            map.put("product", v.getProduct().getName());
            if(v.getSize() != null) map.put("size", v.getSize().getSize());
            if(v.getColor() != null) map.put("color", v.getColor().getColor());
            if(v.getGemstone() != null) map.put("gemstone", v.getGemstone().getGemStone());
            map.put("status", v.getStatus().getStatus());
            return map;
        }
        return obj;
    }
}