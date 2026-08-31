package lk.dio.rush_jewels.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.model.AdminAuditLog;
import lk.dio.rush_jewels.model.PaymentMethod;
import lk.dio.rush_jewels.model.SystemSetting;
import lk.dio.rush_jewels.repository.AdminAuditLogRepository;
import lk.dio.rush_jewels.repository.PaymentMethodRepository;
import lk.dio.rush_jewels.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AdminFinanceService {

    private final PaymentMethodRepository paymentRepository;
    private final SystemSettingRepository settingRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AdminFinanceService(PaymentMethodRepository paymentRepository,
                               SystemSettingRepository settingRepository,
                               AdminAuditLogRepository auditLogRepository,
                               ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.settingRepository = settingRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    // --- Payment Methods ---
    public List<PaymentMethod> getAllPaymentMethods() {
        List<PaymentMethod> list = paymentRepository.findAll();

        // Auto-Seed Default Methods if Table is Empty
        if (list.isEmpty()) {
            paymentRepository.save(new PaymentMethod("Cash on Delivery (COD)", true));
            paymentRepository.save(new PaymentMethod("PayHere / WebXPay", false));
            paymentRepository.save(new PaymentMethod("Bank Transfer", false));
            return paymentRepository.findAll();
        }
        return list;
    }

    public PaymentMethod togglePaymentMethod(int id, boolean isActive) {
        PaymentMethod pm = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Method not found"));

        String oldVal = convertToJson(pm);

        pm.setActive(isActive);
        PaymentMethod saved = paymentRepository.save(pm);

        logAction("TOGGLE_PAYMENT", "payment_method", String.valueOf(id), oldVal, saved);
        return saved;
    }

    // --- Tax Settings ---
    public String getTaxRate() {
        return settingRepository.findByKey("TAX_RATE")
                .map(SystemSetting::getValue)
                .orElse("0.0");
    }

    public void updateTaxRate(String newRate) {
        SystemSetting setting = settingRepository.findByKey("TAX_RATE")
                .orElse(new SystemSetting("TAX_RATE", "0.0"));

        String oldVal = setting.getValue();

        setting.setValue(newRate);
        settingRepository.save(setting);

        logAction("UPDATE_TAX", "system_setting", "TAX_RATE", oldVal, newRate);
    }

    // --- Audit Helpers ---
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