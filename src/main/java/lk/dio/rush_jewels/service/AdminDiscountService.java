package lk.dio.rush_jewels.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.dto.DiscountCodeDTO;
import lk.dio.rush_jewels.model.AdminAuditLog;
import lk.dio.rush_jewels.model.DiscountCode;
import lk.dio.rush_jewels.repository.AdminAuditLogRepository;
import lk.dio.rush_jewels.repository.DiscountCodeRepository;
import lk.dio.rush_jewels.repository.DiscountUsageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminDiscountService {

    private final DiscountCodeRepository codeRepository;
    private final DiscountUsageRepository usageRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AdminDiscountService(DiscountCodeRepository codeRepository,
                                DiscountUsageRepository usageRepository,
                                AdminAuditLogRepository auditLogRepository,
                                ObjectMapper objectMapper) {
        this.codeRepository = codeRepository;
        this.usageRepository = usageRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    // 1. Get All Active Coupons
    public List<DiscountCodeDTO> getAllCoupons() {
        // ✅ UPDATED: Uses the specific method to fetch only Active coupons
        return codeRepository.findAllByIsActiveTrueOrderByCreatedAtDesc().stream().map(c -> {
            long count = usageRepository.countByDiscountCode_Id(c.getId());
            return new DiscountCodeDTO(
                    c.getId(),
                    c.getCode(),
                    c.getValue(),
                    c.getExpirationDate(),
                    c.getUsageLimit() == 0 ? null : c.getUsageLimit(),
                    count,
                    c.isActive()
            );
        }).collect(Collectors.toList());
    }

    // 2. Create Coupon
    public void saveCoupon(DiscountCodeDTO dto) {
        if (codeRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Coupon code already exists");
        }

        DiscountCode code = new DiscountCode();
        code.setCode(dto.getCode().toUpperCase());
        code.setValue(dto.getValue()); // Percentage
        code.setExpirationDate(dto.getExpirationDate());
        code.setUsageLimit(dto.getUsageLimit() != null ? dto.getUsageLimit() : 0);

        code.setActive(true); // Always Active on Create
        code.setCreatedAt(new Date());

        DiscountCode savedCode = codeRepository.save(code);

        // ✅ Audit Log
        logAction("CREATE", "discount_code", String.valueOf(savedCode.getId()), null, savedCode);
    }

    // 3. Update Coupon
    public void updateCoupon(int id, DiscountCodeDTO dto) {
        if (codeRepository.existsByCodeAndIdNot(dto.getCode(), id)) {
            throw new IllegalArgumentException("Coupon code already exists");
        }

        DiscountCode code = codeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        // ✅ Capture Old Value for Audit
        String oldValue = convertToJson(code);

        code.setCode(dto.getCode().toUpperCase());
        code.setValue(dto.getValue());
        code.setExpirationDate(dto.getExpirationDate());
        code.setUsageLimit(dto.getUsageLimit() != null ? dto.getUsageLimit() : 0);

        DiscountCode savedCode = codeRepository.save(code);

        // ✅ Audit Log
        logAction("UPDATE", "discount_code", String.valueOf(savedCode.getId()), oldValue, savedCode);
    }

    // 4. Delete Coupon (Soft Delete)
    public void deleteCoupon(int id) {
        DiscountCode code = codeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        // ✅ Capture Old Value
        String oldValue = convertToJson(code);

        code.setActive(false); // Hides it from the main list
        DiscountCode savedCode = codeRepository.save(code);

        // ✅ Audit Log
        logAction("DEACTIVATE", "discount_code", String.valueOf(savedCode.getId()), oldValue, savedCode);
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
            System.err.println("Audit Log Failed: " + e.getMessage());
        }
    }

    private String convertToJson(Object object) {
        try {
            // Use helper to avoid infinite recursion with lazy lists
            Object safeObject = sanitizeForAudit(object);
            return objectMapper.writeValueAsString(safeObject);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private Object sanitizeForAudit(Object obj) {
        if (obj instanceof DiscountCode) {
            DiscountCode dc = (DiscountCode) obj;
            Map<String, Object> map = new HashMap<>();
            map.put("id", dc.getId());
            map.put("code", dc.getCode());
            map.put("value", dc.getValue());
            map.put("expiry", dc.getExpirationDate());
            map.put("limit", dc.getUsageLimit());
            map.put("isActive", dc.isActive());
            return map;
        }
        return obj;
    }
}