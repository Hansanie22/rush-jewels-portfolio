package lk.dio.rush_jewels.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.dto.StockTransferDTO;
import lk.dio.rush_jewels.dto.WarehouseActivityDTO;
import lk.dio.rush_jewels.dto.WarehouseStatsDTO;
import lk.dio.rush_jewels.model.*;
import lk.dio.rush_jewels.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminWarehouseService {

    private final StockRepository stockRepository;
    private final StockTransferRepository transferRepository;
    private final StockAdjustmentRepository adjustmentRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductVarianceRepository varianceRepository;
    private final StockStatusRepository statusRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AdminWarehouseService(StockRepository stockRepository,
                                 StockTransferRepository transferRepository,
                                 StockAdjustmentRepository adjustmentRepository,
                                 WarehouseRepository warehouseRepository,
                                 ProductVarianceRepository varianceRepository,
                                 StockStatusRepository statusRepository,
                                 AdminAuditLogRepository auditLogRepository,
                                 ObjectMapper objectMapper) {
        this.stockRepository = stockRepository;
        this.transferRepository = transferRepository;
        this.adjustmentRepository = adjustmentRepository;
        this.warehouseRepository = warehouseRepository;
        this.varianceRepository = varianceRepository;
        this.statusRepository = statusRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    // 1. Get Dashboard Stats
    public WarehouseStatsDTO getStats() {
        List<Stock> stocks = stockRepository.findAll();
        long total = stocks.stream().mapToLong(Stock::getQty).sum();
        long low = stocks.stream().filter(s -> s.getStockStatus().getId() == 2).count();
        long ready = stocks.stream().filter(s -> s.getStockStatus().getId() == 1).count();
        return new WarehouseStatsDTO(total, low, ready);
    }

    // 2. Perform Stock Transfer (Bidirectional: Shop <-> Factory)
    public void transferStock(StockTransferDTO dto) {
        // 1. Validation
        if (dto.getFromWarehouseId() == dto.getToWarehouseId()) {
            throw new IllegalArgumentException("Source and Destination warehouses cannot be the same");
        }

        ProductVariance variance = varianceRepository.findById(dto.getVarianceId())
                .orElseThrow(() -> new RuntimeException("Product Variant not found"));

        Warehouse fromWh = warehouseRepository.findById(dto.getFromWarehouseId())
                .orElseThrow(() -> new RuntimeException("Source Warehouse not found"));

        Warehouse toWh = warehouseRepository.findById(dto.getToWarehouseId())
                .orElseThrow(() -> new RuntimeException("Destination Warehouse not found"));

        // 2. Get Source Stock (Using Stream for now, optimized filtering)
        // Note: Ideally, use a repository method like: findByProductVarianceAndWarehouse(variance, warehouse)
        Stock sourceStock = stockRepository.findAll().stream()
                .filter(s -> s.getProductVariance() != null
                        && s.getProductVariance().getId() == variance.getId()
                        && s.getWarehouse() != null
                        && s.getWarehouse().getId() == fromWh.getId())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Stock record not found in source warehouse (" + fromWh.getWarehouse() + ")."));

        if (sourceStock.getQty() < dto.getQuantity()) {
            throw new RuntimeException("Insufficient stock in " + fromWh.getWarehouse() + ". Available: " + sourceStock.getQty());
        }

        // 3. Get or Create Destination Stock
        Stock destStock = stockRepository.findAll().stream()
                .filter(s -> s.getProductVariance() != null
                        && s.getProductVariance().getId() == variance.getId()
                        && s.getWarehouse() != null
                        && s.getWarehouse().getId() == toWh.getId())
                .findFirst()
                .orElseGet(() -> createNewStock(variance, toWh));

        // 4. Perform Update
        // Deduct from Source
        updateStockQty(sourceStock, -dto.getQuantity(), "TRANSFER_OUT", "Transferred to " + toWh.getWarehouse());
        // Add to Destination
        updateStockQty(destStock, dto.getQuantity(), "TRANSFER_IN", "Transferred from " + fromWh.getWarehouse());

        // 5. Save Transfer Record
        StockTransfer transfer = new StockTransfer();
        transfer.setQty(dto.getQuantity());
        transfer.setTransferredAt(new Date());
        transfer.setFromWarehouse(fromWh);
        transfer.setToWarehouse(toWh);
        transfer.setProductVariance(variance);

        StockTransfer savedTransfer = transferRepository.save(transfer);

        // 6. Audit Log
        logAction("TRANSFER_STOCK", "stock_transfer", String.valueOf(savedTransfer.getId()), null, savedTransfer);
    }

    // 3. Get Recent Activity
    public List<WarehouseActivityDTO> getRecentActivity() {
        List<WarehouseActivityDTO> activities = new ArrayList<>();
        List<StockAdjustment> adjustments = adjustmentRepository.findAll();

        adjustments.stream()
                .sorted((a, b) -> b.getAdjustedAt().compareTo(a.getAdjustedAt()))
                .limit(5)
                .forEach(adj -> {
                    String color = "blue";
                    String type = adj.getAdjustmentType();

                    if (type.contains("REMOVE") || type.contains("TRANSFER_OUT")) color = "red";
                    if (type.contains("ADD") || type.contains("TRANSFER_IN")) color = "green";

                    String itemName = "Unknown Item";
                    if (adj.getStock().getProductVariance() != null) {
                        itemName = adj.getStock().getProductVariance().getProduct().getName();
                    } else if (adj.getStock().getCollection() != null) {
                        itemName = adj.getStock().getCollection().getTitle();
                    }
                    // Append Warehouse name for clarity
                    String location = (adj.getStock().getWarehouse() != null) ? " (" + adj.getStock().getWarehouse().getWarehouse() + ")" : "";

                    activities.add(new WarehouseActivityDTO(
                            "Stock Adjustment",
                            type + " " + adj.getQuantity_change() + " for " + itemName + location,
                            getTimeAgo(adj.getAdjustedAt()),
                            color
                    ));
                });

        return activities;
    }

    // --- Helpers ---

    private Stock createNewStock(ProductVariance v, Warehouse w) {
        Stock s = new Stock();
        s.setProductVariance(v);
        s.setWarehouse(w);
        s.setQty(0);
        // 3 = Out of Stock
        s.setStockStatus(statusRepository.findById(3).orElseThrow(() -> new RuntimeException("Status not found")));
        return stockRepository.save(s);
    }

    private void updateStockQty(Stock s, int change, String type, String reason) {
        int oldQty = s.getQty();
        int newQty = oldQty + change;
        s.setQty(newQty);

        // Calculate new status
        int limit = 0;
        if (s.getProductVariance() != null) {
            limit = s.getProductVariance().getStockLimit();
        }
        // Status: 1=In Stock, 2=Low Stock, 3=Out of Stock
        int statusId = (newQty == 0) ? 3 : (newQty < limit ? 2 : 1);
        s.setStockStatus(statusRepository.findById(statusId).orElseThrow());

        stockRepository.save(s);

        // Log Adjustment
        StockAdjustment adj = new StockAdjustment(s, type, Math.abs(change), oldQty, newQty, reason);
        adjustmentRepository.save(adj);
    }

    private String getTimeAgo(LocalDateTime date) {
        if(date == null) return "Just now";
        Duration d = Duration.between(date, LocalDateTime.now());
        if (d.toMinutes() < 60) return d.toMinutes() + " mins ago";
        if (d.toHours() < 24) return d.toHours() + " hours ago";
        return d.toDays() + " days ago";
    }

    // --- Audit Log ---

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
        if (obj instanceof StockTransfer) {
            StockTransfer st = (StockTransfer) obj;
            Map<String, Object> map = new HashMap<>();
            map.put("id", st.getId());
            map.put("qty", st.getQty());
            if (st.getFromWarehouse() != null) map.put("from", st.getFromWarehouse().getWarehouse());
            if (st.getToWarehouse() != null) map.put("to", st.getToWarehouse().getWarehouse());
            return map;
        }
        return obj;
    }
}