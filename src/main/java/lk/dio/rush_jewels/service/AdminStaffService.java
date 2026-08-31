package lk.dio.rush_jewels.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.dto.AdminDTO;
import lk.dio.rush_jewels.dto.AdminProfileDTO;
import lk.dio.rush_jewels.dto.AdminProfileUpdateDTO; // ✅ Import new DTO
import lk.dio.rush_jewels.model.Admin;
import lk.dio.rush_jewels.model.AdminAuditLog;
import lk.dio.rush_jewels.model.Status;
import lk.dio.rush_jewels.repository.AdminAuditLogRepository;
import lk.dio.rush_jewels.repository.AdminRepository;
import lk.dio.rush_jewels.repository.StatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminStaffService {

    private final AdminRepository adminRepository;
    private final StatusRepository statusRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AdminStaffService(AdminRepository adminRepository, StatusRepository statusRepository, AdminAuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.adminRepository = adminRepository;
        this.statusRepository = statusRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public List<AdminDTO> getAllStaff() {
        return adminRepository.findAll().stream().map(a -> new AdminDTO(
                a.getId(),
                a.getName(),
                a.getEmail(),
                a.getStatus().getStatus(),
                a.getStatus().getId(),
                a.getLastLogin(),
                a.getCreatedAt(),
                a.getRole(),
                a.getImagePath()
        )).collect(Collectors.toList());
    }

    // Get Logged Admin Profile
    public AdminProfileDTO getAdminProfile(int adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        return new AdminProfileDTO(
                admin.getId(),
                admin.getName(),
                admin.getEmail(),
                admin.getRole(),
                admin.getLastLogin()
        );
    }

    // ✅ NEW METHOD: Update Admin Profile
    public void updateProfile(int adminId, AdminProfileUpdateDTO dto) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        // 1. Verify Current Password (Simple check for demo, use BCrypt in prod)
        if (!admin.getPassword().equals(dto.getCurrentPassword())) {
            throw new IllegalArgumentException("Incorrect current password");
        }

        // 2. Update Basic Info
        String oldValue = convertToJson(admin);

        admin.setName(dto.getName());

        // Check email uniqueness if changed
        if (!admin.getEmail().equalsIgnoreCase(dto.getEmail())) {
            if (adminRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email already in use");
            }
            admin.setEmail(dto.getEmail());
        }

        // 3. Update Password if provided
        if (dto.getNewPassword() != null && !dto.getNewPassword().trim().isEmpty()) {
            admin.setPassword(dto.getNewPassword()); // Use Hashing here in production
        }

        Admin saved = adminRepository.save(admin);

        // Audit Log
        logAction("UPDATE_PROFILE", "admin", String.valueOf(admin.getId()), oldValue, saved);
    }

    public void createAdmin(AdminDTO dto) {
        if (adminRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Admin admin = new Admin();
        admin.setName(dto.getName());
        admin.setEmail(dto.getEmail());
        admin.setPassword(dto.getPassword());
        admin.setRole(dto.getRole() != null && !dto.getRole().isEmpty() ? dto.getRole() : "CASHIER");

        Status active = statusRepository.findById(1).orElseThrow();
        admin.setStatus(active);

        Admin saved = adminRepository.save(admin);
        logAction("CREATE_ADMIN", "admin", String.valueOf(saved.getId()), null, saved);
    }

    public void updateStatus(int id, boolean isActive) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        String oldValue = convertToJson(admin);

        Status status = statusRepository.findById(isActive ? 1 : 2).orElseThrow();
        admin.setStatus(status);

        Admin saved = adminRepository.save(admin);
        logAction("UPDATE_STATUS", "admin", String.valueOf(id), oldValue, saved);
    }

    // Audit Helpers
    private void logAction(String action, String table, String recordId, String oldVal, Object newValObj) {
        try {
            String newVal = convertToJson(newValObj);
            AdminAuditLog log = new AdminAuditLog(action, table, recordId, oldVal, newVal, LocalDateTime.now());
            auditLogRepository.save(log);
        } catch (Exception e) { System.err.println("Audit failed"); }
    }

    private String convertToJson(Object obj) {
        try { return objectMapper.writeValueAsString(sanitize(obj)); } catch (JsonProcessingException e) { return "{}"; }
    }

    private Object sanitize(Object obj) {
        if (obj instanceof Admin) {
            Admin a = (Admin) obj;
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("name", a.getName());
            map.put("email", a.getEmail());
            map.put("status", a.getStatus().getStatus());
            return map;
        }
        return obj;
    }
}