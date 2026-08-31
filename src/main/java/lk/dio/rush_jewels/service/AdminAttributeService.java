package lk.dio.rush_jewels.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.model.*;
import lk.dio.rush_jewels.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminAttributeService {

    private final SizeRepository sizeRepository;
    private final ColorRepository colorRepository;
    private final GemstoneRepository gemstoneRepository;
    private final CategoryRepository categoryRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AdminAttributeService(SizeRepository sizeRepository,
                                 ColorRepository colorRepository,
                                 GemstoneRepository gemstoneRepository,
                                 CategoryRepository categoryRepository,
                                 AdminAuditLogRepository auditLogRepository,
                                 ObjectMapper objectMapper) {
        this.sizeRepository = sizeRepository;
        this.colorRepository = colorRepository;
        this.gemstoneRepository = gemstoneRepository;
        this.categoryRepository = categoryRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    // ==========================================
    // SIZE OPERATIONS
    // ==========================================
    public List<Size> getAllSizes() {
        return sizeRepository.findAll();
    }

    public Size saveSize(Size size) {
        int catId = size.getCategory().getId();

        // Check for duplicates
        if (sizeRepository.existsBySizeAndCategory_Id(size.getSize(), catId)) {
            throw new IllegalArgumentException("This Size already exists in the selected Category.");
        }

        Category cat = categoryRepository.findById(catId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        size.setCategory(cat);

        Size savedSize = sizeRepository.save(size);

        // Audit Log
        logAction("CREATE", "size", String.valueOf(savedSize.getId()), null, savedSize);

        return savedSize;
    }

    public Size updateSize(int id, Size size) {
        int catId = size.getCategory().getId();

        // Check for duplicates (exclude current ID)
        if (sizeRepository.existsBySizeAndCategory_IdAndIdNot(size.getSize(), catId, id)) {
            throw new IllegalArgumentException("This Size already exists in the selected Category.");
        }

        Size existing = sizeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Size not found with ID: " + id));

        // Capture Old Value
        String oldValue = convertToJson(existing);

        // Update fields
        existing.setSize(size.getSize());
        Category cat = categoryRepository.findById(catId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        existing.setCategory(cat);

        Size savedSize = sizeRepository.save(existing);

        // Audit Log
        logAction("UPDATE", "size", String.valueOf(savedSize.getId()), oldValue, savedSize);

        return savedSize;
    }

    // ==========================================
    // COLOR (METAL) OPERATIONS
    // ==========================================
    public List<Color> getAllColors() {
        return colorRepository.findAll();
    }

    public Color saveColor(Color color) {
        if (colorRepository.existsByColor(color.getColor())) {
            throw new IllegalArgumentException("This Metal/Color already exists.");
        }
        Color savedColor = colorRepository.save(color);

        // Audit Log
        logAction("CREATE", "color", String.valueOf(savedColor.getId()), null, savedColor);

        return savedColor;
    }

    public Color updateColor(int id, Color color) {
        if (colorRepository.existsByColorAndIdNot(color.getColor(), id)) {
            throw new IllegalArgumentException("This Metal/Color already exists.");
        }

        Color existing = colorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Color not found with ID: " + id));

        // Capture Old Value
        String oldValue = convertToJson(existing);

        existing.setColor(color.getColor());
        Color savedColor = colorRepository.save(existing);

        // Audit Log
        logAction("UPDATE", "color", String.valueOf(savedColor.getId()), oldValue, savedColor);

        return savedColor;
    }

    // ==========================================
    // GEMSTONE OPERATIONS
    // ==========================================
    public List<Gemstone> getAllGemstones() {
        return gemstoneRepository.findAll();
    }

    public Gemstone saveGemstone(Gemstone gemstone) {
        if (gemstoneRepository.existsByGemStone(gemstone.getGemStone())) {
            throw new IllegalArgumentException("This Gemstone already exists.");
        }
        Gemstone savedGemstone = gemstoneRepository.save(gemstone);

        // Audit Log
        logAction("CREATE", "gemstone", String.valueOf(savedGemstone.getId()), null, savedGemstone);

        return savedGemstone;
    }

    public Gemstone updateGemstone(int id, Gemstone gemstone) {
        if (gemstoneRepository.existsByGemStoneAndIdNot(gemstone.getGemStone(), id)) {
            throw new IllegalArgumentException("This Gemstone already exists.");
        }

        Gemstone existing = gemstoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gemstone not found with ID: " + id));

        // Capture Old Value
        String oldValue = convertToJson(existing);

        existing.setGemStone(gemstone.getGemStone());
        Gemstone savedGemstone = gemstoneRepository.save(existing);

        // Audit Log
        logAction("UPDATE", "gemstone", String.valueOf(savedGemstone.getId()), oldValue, savedGemstone);

        return savedGemstone;
    }

    // ==========================================
    // AUDIT LOG HELPER METHODS
    // ==========================================

    private void logAction(String action, String table, String recordId, String oldValue, Object newValueObj) {
        try {
            String newValue = convertToJson(newValueObj);
            AdminAuditLog log = new AdminAuditLog(
                    action,
                    table,
                    recordId,
                    oldValue,
                    newValue,
                    LocalDateTime.now()
            );
            auditLogRepository.save(log);
        } catch (Exception e) {
            // Log error to console but DO NOT fail the transaction.
            // This ensures the actual Data Update still succeeds even if Audit Log fails.
            System.err.println("WARNING: Failed to save audit log: " + e.getMessage());
        }
    }

    private String convertToJson(Object object) {
        try {
            // ✅ FIX: Sanitize object before serializing to avoid ByteBuddy/Proxy errors
            Object safeObject = sanitizeForAudit(object);
            return objectMapper.writeValueAsString(safeObject);
        } catch (JsonProcessingException e) {
            // Return error string instead of crashing
            return "{\"error\": \"Serialization Failed\"}";
        }
    }

    // ✅ HELPER: Convert Complex Entities to Simple Maps for Audit Log
    private Object sanitizeForAudit(Object obj) {
        if (obj instanceof Size) {
            Size s = (Size) obj;
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("size", s.getSize());
            // Extract only simple fields from Category to avoid Recursion/Proxy issues
            if (s.getCategory() != null) {
                map.put("categoryId", s.getCategory().getId());
                map.put("categoryName", s.getCategory().getCategory());
            }
            return map;
        }
        // Color and Gemstone are simple entities, safe to return as is
        return obj;
    }
}