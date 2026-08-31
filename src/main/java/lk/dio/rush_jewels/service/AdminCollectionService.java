package lk.dio.rush_jewels.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.dto.CollectionRequestDTO;
import lk.dio.rush_jewels.dto.CollectionResponseDTO;
import lk.dio.rush_jewels.model.*;
import lk.dio.rush_jewels.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminCollectionService {

    private final CollectionRepository collectionRepository;
    private final StatusRepository statusRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final StockRepository stockRepository;
    private final StockStatusRepository stockStatusRepository;
    private final WarehouseRepository warehouseRepository;
    private final ObjectMapper objectMapper;
    private final CloudinaryService cloudinaryService; // Cloudinary සර්විස් එක මෙතනට ගත්තා

    public AdminCollectionService(CollectionRepository collectionRepository,
                                  StatusRepository statusRepository,
                                  AdminAuditLogRepository auditLogRepository,
                                  StockRepository stockRepository,
                                  StockStatusRepository stockStatusRepository,
                                  WarehouseRepository warehouseRepository,
                                  ObjectMapper objectMapper,
                                  CloudinaryService cloudinaryService) { // Constructor එකටත් දැම්මා
        this.collectionRepository = collectionRepository;
        this.statusRepository = statusRepository;
        this.auditLogRepository = auditLogRepository;
        this.stockRepository = stockRepository;
        this.stockStatusRepository = stockStatusRepository;
        this.warehouseRepository = warehouseRepository;
        this.objectMapper = objectMapper;
        this.cloudinaryService = cloudinaryService;
    }

    public List<CollectionResponseDTO> getAllCollections() {
        return collectionRepository.findAll().stream().map(c -> {
            // හාඩ් ඩිස්ක් එකෙන් නැතුව කෙලින්ම DB එකේ තියෙන ලින්ක් එක ගන්නවා
            String imagePath = c.getImage1();
            return convertToDTO(c, imagePath);
        }).collect(Collectors.toList());
    }

    public CollectionResponseDTO getCollectionById(int id) {
        Collection c = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found"));
        // හාඩ් ඩිස්ක් එකෙන් නැතුව කෙලින්ම DB එකේ තියෙන ලින්ක් එක ගන්නවා
        String imagePath = c.getImage1();
        return convertToDTO(c, imagePath);
    }

    public Collection saveCollection(CollectionRequestDTO dto) {
        Collection collection = new Collection();
        mapDtoToEntity(dto, collection);
        collection.setCreatedAt(LocalDateTime.now());

        collection.setType("STANDALONE");

        Status status = statusRepository.findById(1).orElseThrow(() -> new RuntimeException("Active Status not found"));
        collection.setStatus(status);

        Collection saved = collectionRepository.save(collection);

        createInitialStock(saved);

        logAction("CREATE", "collection", String.valueOf(saved.getId()), null, saved);
        return saved;
    }

    public Collection updateCollection(int id, CollectionRequestDTO dto) {
        Collection existing = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        String oldValue = convertToJson(existing);
        mapDtoToEntity(dto, existing);

        Collection saved = collectionRepository.save(existing);
        logAction("UPDATE", "collection", String.valueOf(saved.getId()), oldValue, saved);
        return saved;
    }

    public void updateStatus(int id, boolean active) {
        Collection c = collectionRepository.findById(id).orElseThrow(() -> new RuntimeException("Collection not found"));
        String oldValue = convertToJson(c);
        Status status = statusRepository.findById(active ? 1 : 2).orElseThrow(() -> new RuntimeException("Status not found"));
        c.setStatus(status);
        collectionRepository.save(c);
        logAction("UPDATE_STATUS", "collection", String.valueOf(id), oldValue, c);
    }

    private void createInitialStock(Collection collection) {
        Warehouse factory = warehouseRepository.findById(1)
                .orElseGet(() -> warehouseRepository.save(new Warehouse(1, "Factory", null)));
        Warehouse shop = warehouseRepository.findById(2)
                .orElseGet(() -> warehouseRepository.save(new Warehouse(2, "Shop", null)));

        createOrUpdateStockRecord(collection, factory);
        createOrUpdateStockRecord(collection, shop);
    }

    private void createOrUpdateStockRecord(Collection collection, Warehouse warehouse) {
        boolean exists = stockRepository.findAll().stream().anyMatch(s ->
                s.getCollection() != null && s.getCollection().getId() == collection.getId() && s.getWarehouse().getId() == warehouse.getId());

        if (!exists) {
            Stock stock = new Stock();
            stock.setCollection(collection);
            stock.setQty(0);
            stock.setWarehouse(warehouse);
            stock.setStockStatus(stockStatusRepository.findById(3).orElseThrow());
            stockRepository.save(stock);
        }
    }

    // මෙන්න මේ මෙතඩ් එක සම්පූර්ණයෙන්ම වෙනස් කළා Cloudinary වලට හරියන්න
    public void saveCollectionImages(int collectionId, MultipartFile img1, MultipartFile img2, MultipartFile img3, MultipartFile img4) throws IOException {
        Collection c = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        // Image 1 Upload
        if (img1 != null && !img1.isEmpty()) {
            String url = cloudinaryService.uploadImage(img1);
            c.setImage1(url); // ඔයාගේ Entity එකේ image1 කියලා Field එකක් තියෙන්න ඕන
        }

        // Image 2 Upload
        if (img2 != null && !img2.isEmpty()) {
            String url = cloudinaryService.uploadImage(img2);
            c.setImage2(url); // ඔයාගේ Entity එකේ image2 කියලා Field එකක් තියෙන්න ඕන
        }

        // Image 3 Upload
        if (img3 != null && !img3.isEmpty()) {
            String url = cloudinaryService.uploadImage(img3);
            c.setImage3(url);
        }

        // Image 4 Upload
        if (img4 != null && !img4.isEmpty()) {
            String url = cloudinaryService.uploadImage(img4);
            c.setImage4(url);
        }

        collectionRepository.save(c); // අන්තිමට URL ටික Database එකේ සේව් කරනවා
    }

    // පරණ saveFile මෙතඩ් එක මැකුවා (දැන් ඕන වෙන්නේ නෑ)

    private void mapDtoToEntity(CollectionRequestDTO dto, Collection c) {
        c.setName(dto.getName());
        c.setTitle(dto.getTitle());
        c.setDescription(dto.getDescription());
        c.setSpecifications(dto.getSpecifications());
        c.setWarranty(dto.getWarranty());
        c.setMaterial(dto.getMaterial());
        double reg = dto.getRegularPrice();
        double disc = dto.getDiscountPercentage() != null ? dto.getDiscountPercentage() : 0.0;
        c.setRegularPrice(reg);
        c.setDiscountPercentage(disc);
        c.setPrice(reg - (reg * disc / 100));
        c.setStockLimit(dto.getStockLimit());
    }

    private CollectionResponseDTO convertToDTO(Collection c, String imagePath) {
        return new CollectionResponseDTO(c.getId(), c.getName(), c.getTitle(), c.getDescription(), c.getSpecifications(), c.getWarranty(), c.getMaterial(), c.getPrice(), c.getRegularPrice(), c.getDiscountPercentage(), c.getStockLimit(), c.getStatus().getId(), c.getStatus().getStatus(), imagePath);
    }

    private void logAction(String action, String table, String recordId, String oldVal, Object newValObj) {
        try {
            String newVal = convertToJson(newValObj);
            auditLogRepository.save(new AdminAuditLog(action, table, recordId, oldVal, newVal, LocalDateTime.now()));
        } catch (Exception e) { System.err.println("Audit failed: " + e.getMessage()); }
    }

    private String convertToJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); } catch (JsonProcessingException e) { return "{}"; }
    }
}