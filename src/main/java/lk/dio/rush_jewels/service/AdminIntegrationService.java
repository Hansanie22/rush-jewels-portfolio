package lk.dio.rush_jewels.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.model.AdminAuditLog;
import lk.dio.rush_jewels.model.Integration;
import lk.dio.rush_jewels.repository.AdminAuditLogRepository;
import lk.dio.rush_jewels.repository.IntegrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminIntegrationService {

    private final IntegrationRepository integrationRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper; // ✅ Added for JSON conversion

    public AdminIntegrationService(IntegrationRepository integrationRepository,
                                   AdminAuditLogRepository auditLogRepository,
                                   ObjectMapper objectMapper) {
        this.integrationRepository = integrationRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public List<Integration> getAllIntegrations() {
        List<Integration> list = integrationRepository.findAll();

        // Auto-seed defaults if table is empty
        if (list.isEmpty()) {
            createDefault("Meta Pixel", "meta", "fab fa-facebook text-blue-600");
            createDefault("Google Analytics", "google", "fab fa-google text-red-500");
            createDefault("WhatsApp API", "whatsapp", "fab fa-whatsapp text-green-500");
            return integrationRepository.findAll();
        }

        return list;
    }

    private void createDefault(String name, String key, String icon) {
        Integration integration = new Integration(name, key, false, icon);
        integration.setApiKey("");
        integrationRepository.save(integration);
    }

    public Integration toggleStatus(int id, String apiKeyInput) {
        Integration integration = integrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Integration not found"));

        boolean wasConnected = integration.isConnected();

        // ✅ Capture Old Value (As valid JSON Map)
        String oldValue = convertToJson(integration);

        if (!wasConnected) {
            // Connect
            if (apiKeyInput != null && !apiKeyInput.trim().isEmpty()) {
                integration.setApiKey(apiKeyInput);
            }
            integration.setConnected(true);
        } else {
            // Disconnect
            integration.setConnected(false);
        }

        Integration saved = integrationRepository.save(integration);

        // ✅ Audit Log: Pass Objects, not raw strings
        String action = wasConnected ? "DISCONNECT_INTEGRATION" : "CONNECT_INTEGRATION";
        logAction(action, "integration", String.valueOf(id), oldValue, saved);

        return saved;
    }

    // ==========================================
    // AUDIT LOG HELPER METHODS
    // ==========================================

    private void logAction(String action, String table, String recordId, Object oldValueObj, Object newValueObj) {
        try {
            // Convert both inputs to valid JSON Strings
            String oldValue = (oldValueObj instanceof String) ? (String) oldValueObj : convertToJson(oldValueObj);
            String newValue = (newValueObj instanceof String) ? (String) newValueObj : convertToJson(newValueObj);

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
            // Note: If the DB constraint fails here, it might still rollback the transaction.
            // Ensuring valid JSON above prevents the DB constraint failure.
        }
    }

    private String convertToJson(Object object) {
        try {
            // Sanitize object to avoid recursion
            return objectMapper.writeValueAsString(sanitizeForAudit(object));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private Object sanitizeForAudit(Object obj) {
        if (obj instanceof Integration) {
            Integration i = (Integration) obj;
            Map<String, Object> map = new HashMap<>();
            map.put("id", i.getId());
            map.put("name", i.getName());
            map.put("connected", i.isConnected());
            // Mask API key for security in logs
            map.put("hasKey", (i.getApiKey() != null && !i.getApiKey().isEmpty()));
            return map;
        }
        return obj;
    }
}