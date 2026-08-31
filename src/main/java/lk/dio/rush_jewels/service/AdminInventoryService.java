package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.dto.InventoryDTOs;
import lk.dio.rush_jewels.model.*;
import lk.dio.rush_jewels.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminInventoryService {

    private final StockRepository stockRepository;
    private final StockStatusRepository stockStatusRepository;
    private final StockAdjustmentRepository adjustmentRepository;
    private final ProductVarianceRepository varianceRepository;
    private final CollectionRepository collectionRepository;
    private final WarehouseRepository warehouseRepository;
    private final CollectionSetRepository collectionSetRepository;

    public AdminInventoryService(StockRepository stockRepository,
                                 StockStatusRepository stockStatusRepository,
                                 StockAdjustmentRepository adjustmentRepository,
                                 ProductVarianceRepository varianceRepository,
                                 CollectionRepository collectionRepository,
                                 WarehouseRepository warehouseRepository,
                                 CollectionSetRepository collectionSetRepository) {
        this.stockRepository = stockRepository;
        this.stockStatusRepository = stockStatusRepository;
        this.adjustmentRepository = adjustmentRepository;
        this.varianceRepository = varianceRepository;
        this.collectionRepository = collectionRepository;
        this.warehouseRepository = warehouseRepository;
        this.collectionSetRepository = collectionSetRepository;
    }

    public List<InventoryDTOs.InventoryItemDTO> getInventoryByWarehouse(int warehouseId) {
        syncMissingStock();

        return stockRepository.findAll().stream()
                .filter(stock -> stock.getWarehouse() != null && stock.getWarehouse().getId() == warehouseId)
                .map(stock -> {
                    String type = "";
                    int itemId = 0;
                    String name = "";
                    String category = "";
                    int limit = 0;
                    double price = 0.0;
                    int finalQty = stock.getQty();

                    if (stock.getProductVariance() != null) {
                        ProductVariance v = stock.getProductVariance();
                        type = "Product";
                        itemId = v.getId();
                        name = generateVarianceName(v);
                        category = (v.getProduct().getCategory() != null) ? v.getProduct().getCategory().getCategory() : "Uncategorized";
                        limit = v.getStockLimit();
                        price = v.getPrice() != null ? v.getPrice() : v.getRegularPrice();
                    } else if (stock.getCollection() != null) {
                        Collection c = stock.getCollection();
                        itemId = c.getId();
                        name = c.getTitle();
                        category = "Collection Set";
                        limit = c.getStockLimit();
                        price = c.getPrice();

                        // ✅ BUNDLE වර්ගයේ එකක් නම්, Virtual Stock එක ගණනය කර DB එකේ Qty එක Update කිරීම
                        if ("BUNDLE".equalsIgnoreCase(c.getType())) {
                            type = "Collection (BUNDLE)";
                            finalQty = calculateAndUpdateBundleStock(stock);
                        } else {
                            type = "Collection (STANDALONE)";
                            finalQty = stock.getQty();
                        }
                    } else {
                        return null;
                    }

                    return new InventoryDTOs.InventoryItemDTO(
                            stock.getId(),
                            type,
                            itemId,
                            name,
                            "SKU-" + stock.getId(),
                            category,
                            warehouseId,
                            stock.getWarehouse().getWarehouse(),
                            finalQty,
                            limit,
                            calculateStatusText(finalQty, limit),
                            price
                    );
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    /**
     * Bundle එකක අඩංගු Products වල ප්‍රමාණය අනුව Collection එකේ
     * DB Stock Quantity එක Update කිරීම.
     */
    private int calculateAndUpdateBundleStock(Stock bundleStockRecord) {
        int collectionId = bundleStockRecord.getCollection().getId();
        int warehouseId = bundleStockRecord.getWarehouse().getId();

        List<CollectionSet> components = collectionSetRepository.findByCollection_Id(collectionId);
        if (components.isEmpty()) return 0;

        int minSetsPossible = Integer.MAX_VALUE;

        for (CollectionSet item : components) {
            Optional<Stock> partStock = stockRepository.findByProductVariance_IdAndWarehouse_Id(
                    item.getProductVariance().getId(), warehouseId);

            if (partStock.isPresent()) {
                int possibleWithThisPart = partStock.get().getQty() / item.getQty();
                if (possibleWithThisPart < minSetsPossible) {
                    minSetsPossible = possibleWithThisPart;
                }
            } else {
                minSetsPossible = 0;
                break;
            }
        }

        int calculatedQty = (minSetsPossible == Integer.MAX_VALUE) ? 0 : minSetsPossible;

        // ✅ DATABASE UPDATE: ප්ලාස්ටික් stock value එක database එකේම update කිරීම
        if (bundleStockRecord.getQty() != calculatedQty) {
            bundleStockRecord.setQty(calculatedQty);

            // Status එකත් auto update කිරීම
            int limit = bundleStockRecord.getCollection().getStockLimit();
            int statusId = (calculatedQty == 0) ? 3 : (calculatedQty < limit ? 2 : 1);
            bundleStockRecord.setStockStatus(stockStatusRepository.findById(statusId).orElseThrow());

            stockRepository.save(bundleStockRecord);
        }

        return calculatedQty;
    }

    private void syncMissingStock() {
        Warehouse w1 = warehouseRepository.findById(1).orElseGet(() -> warehouseRepository.save(new Warehouse(1, "Factory", null)));
        Warehouse w2 = warehouseRepository.findById(2).orElseGet(() -> warehouseRepository.save(new Warehouse(2, "Shop", null)));

        varianceRepository.findAll().forEach(v -> {
            checkAndCreateStock(v, null, w1);
            checkAndCreateStock(v, null, w2);
        });

        collectionRepository.findAll().forEach(c -> {
            checkAndCreateStock(null, c, w1);
            checkAndCreateStock(null, c, w2);
        });
    }

    private void checkAndCreateStock(ProductVariance v, Collection c, Warehouse w) {
        boolean exists = stockRepository.findAll().stream().anyMatch(s ->
                s.getWarehouse().getId() == w.getId() &&
                        ((v != null && s.getProductVariance() != null && s.getProductVariance().getId() == v.getId()) ||
                                (c != null && s.getCollection() != null && s.getCollection().getId() == c.getId()))
        );

        if (!exists) {
            Stock stock = new Stock();
            stock.setProductVariance(v);
            stock.setCollection(c);
            stock.setQty(0);
            stock.setStockStatus(stockStatusRepository.findById(3).orElseThrow());
            stock.setWarehouse(w);
            stockRepository.save(stock);
        }
    }

    public void adjustStock(InventoryDTOs.StockAdjustmentRequestDTO dto) {
        Stock stock = stockRepository.findById(dto.getStockId())
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        // BUNDLE එකක stock එක කෙලින්ම adjust කරන්න බැහැ
        if (stock.getCollection() != null && "BUNDLE".equalsIgnoreCase(stock.getCollection().getType())) {
            throw new RuntimeException("BUNDLE stock is calculated automatically from products. You cannot adjust it manually.");
        }

        int oldQty = stock.getQty();
        int newQty = oldQty;
        int limit = (stock.getProductVariance() != null) ? stock.getProductVariance().getStockLimit() : stock.getCollection().getStockLimit();

        switch (dto.getAdjustmentType().toLowerCase()) {
            case "add": newQty = oldQty + dto.getQuantity(); break;
            case "remove": newQty = Math.max(0, oldQty - dto.getQuantity()); break;
            case "set": newQty = Math.max(0, dto.getQuantity()); break;
            default: throw new IllegalArgumentException("Invalid adjustment type");
        }

        stock.setQty(newQty);
        int statusId = (newQty == 0) ? 3 : (newQty < limit ? 2 : 1);
        stock.setStockStatus(stockStatusRepository.findById(statusId).orElseThrow());
        stockRepository.save(stock);

        adjustmentRepository.save(new StockAdjustment(stock, dto.getAdjustmentType().toUpperCase(), dto.getQuantity(), oldQty, newQty, dto.getReason()));
    }

    private String generateVarianceName(ProductVariance v) {
        StringBuilder sb = new StringBuilder(v.getProduct().getName());
        if (v.getSize() != null) sb.append(" - ").append(v.getSize().getSize());
        if (v.getColor() != null) sb.append(" / ").append(v.getColor().getColor());
        if (v.getGemstone() != null) sb.append(" / ").append(v.getGemstone().getGemStone());
        return sb.toString();
    }

    private String calculateStatusText(int qty, int limit) {
        if (qty == 0) return "Out of Stock";
        if (qty < limit) return "Low Stock";
        return "In Stock";
    }
}