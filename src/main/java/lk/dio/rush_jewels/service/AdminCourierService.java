package lk.dio.rush_jewels.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.model.AdminAuditLog;
import lk.dio.rush_jewels.model.CourierCompany;
import lk.dio.rush_jewels.model.Shipping;
import lk.dio.rush_jewels.repository.AdminAuditLogRepository;
import lk.dio.rush_jewels.repository.CourierCompanyRepository;
import lk.dio.rush_jewels.repository.ShippingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminCourierService {

    private final ShippingRepository shippingRepository;
    private final CourierCompanyRepository courierRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AdminCourierService(ShippingRepository shippingRepository,
                               CourierCompanyRepository courierRepository,
                               AdminAuditLogRepository auditLogRepository,
                               ObjectMapper objectMapper) {
        this.shippingRepository = shippingRepository;
        this.courierRepository = courierRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    // ==========================================
    // SHIPPING METHODS
    // ==========================================

    public List<Shipping> getAllActiveShippingMethods() {
        return shippingRepository.findAll().stream()
                .filter(s -> s.getStatus() == 1)
                .collect(Collectors.toList());
    }

    public Shipping saveShippingMethod(Shipping shipping) {
        String action = (shipping.getId() > 0) ? "UPDATE_SHIPPING" : "CREATE_SHIPPING";
        String oldVal = "{}";

        // Logic to keep status=1 on update unless specified
        if(shipping.getId() > 0) {
            Shipping existing = shippingRepository.findById(shipping.getId()).orElse(null);
            if(existing != null) {
                oldVal = convertToJson(existing);
                shipping.setStatus(existing.getStatus()); // Preserve existing status
            } else {
                shipping.setStatus(1); // Default active
            }
        } else {
            shipping.setStatus(1); // Default active on create
        }

        Shipping saved = shippingRepository.save(shipping);
        logAction(action, "shipping", String.valueOf(saved.getId()), oldVal, saved);
        return saved;
    }

    public void softDeleteShippingMethod(int id) {
        Shipping shipping = shippingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipping Method not found"));

        String oldVal = convertToJson(shipping);
        shipping.setStatus(0);
        Shipping updated = shippingRepository.save(shipping);

        logAction("DELETE_SHIPPING", "shipping", String.valueOf(id), oldVal, updated);
    }

    // ==========================================
    // COURIER COMPANIES
    // ==========================================

    public List<CourierCompany> getAllActiveCourierCompanies() {
        return courierRepository.findAll().stream()
                .filter(c -> c.getStatus() == 1)
                .collect(Collectors.toList());
    }

    public CourierCompany saveCourierCompany(CourierCompany company) {
        String action = (company.getId() > 0) ? "UPDATE_COURIER" : "CREATE_COURIER";
        String oldVal = "{}";

        if(company.getId() > 0) {
            CourierCompany existing = courierRepository.findById(company.getId()).orElse(null);
            if(existing != null) {
                oldVal = convertToJson(existing);
                company.setStatus(existing.getStatus());
            } else {
                company.setStatus(1);
            }
        } else {
            company.setStatus(1);
        }

        CourierCompany saved = courierRepository.save(company);
        logAction(action, "courier_company", String.valueOf(saved.getId()), oldVal, saved);
        return saved;
    }

    public void softDeleteCourierCompany(int id) {
        CourierCompany company = courierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Courier Company not found"));

        String oldVal = convertToJson(company);
        company.setStatus(0);
        CourierCompany updated = courierRepository.save(company);

        logAction("DELETE_COURIER", "courier_company", String.valueOf(id), oldVal, updated);
    }

    // ==========================================
    // AUDIT LOGGING
    // ==========================================

    private void logAction(String action, String table, String recordId, Object oldValObj, Object newValObj) {
        try {
            String oldVal = (oldValObj instanceof String) ? (String) oldValObj : convertToJson(oldValObj);
            String newVal = (newValObj instanceof String) ? (String) newValObj : convertToJson(newValObj);

            AdminAuditLog log = new AdminAuditLog(action, table, recordId, oldVal, newVal, LocalDateTime.now());
            auditLogRepository.save(log);
        } catch (Exception e) { System.err.println("Audit Log Failed"); }
    }

    private String convertToJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); } catch (JsonProcessingException e) { return "{}"; }
    }
}