package lk.dio.rush_jewels.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.dto.CustomerResponseDTO;
import lk.dio.rush_jewels.model.AdminAuditLog;
import lk.dio.rush_jewels.model.Status;
import lk.dio.rush_jewels.model.User;
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
public class AdminCustomerService {

    private final UserRepository userRepository;
    private final OrdersRepository ordersRepository;
    private final PaymentRepository paymentRepository;
    private final StatusRepository statusRepository;
    private final AdminAuditLogRepository auditLogRepository; // ✅ Added for Audit
    private final ObjectMapper objectMapper; // ✅ Added for JSON conversion

    public AdminCustomerService(UserRepository userRepository,
                                OrdersRepository ordersRepository,
                                PaymentRepository paymentRepository,
                                StatusRepository statusRepository,
                                AdminAuditLogRepository auditLogRepository,
                                ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.ordersRepository = ordersRepository;
        this.paymentRepository = paymentRepository;
        this.statusRepository = statusRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public List<CustomerResponseDTO> getAllCustomers() {
        List<User> users = userRepository.findAll();

        return users.stream().map(user -> {
            // 1. Calculate Total Spent (from Payments table)
            Double totalSpent = paymentRepository.getTotalSpentByUserId(user.getId());

            // 2. Count Orders
            long orderCount = ordersRepository.countByUser_Id(user.getId());

            // 3. Construct Name
            String fullName = user.getFname() + " " + user.getLname();

            return new CustomerResponseDTO(
                    user.getId(),
                    fullName,
                    user.getEmail(),
                    totalSpent != null ? totalSpent : 0.0,
                    orderCount,
                    user.getStatus().getId(),
                    user.getStatus().getStatus()
            );
        }).collect(Collectors.toList());
    }

    public void updateCustomerStatus(int userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ 1. Capture Old Value (Snapshot before update)
        String oldValue = convertToJson(user);

        // 2. Perform Update
        // Assuming Status ID 1 = Active, 2 = Inactive
        Status status = statusRepository.findById(isActive ? 1 : 2)
                .orElseThrow(() -> new RuntimeException("Status not found"));

        user.setStatus(status);
        User savedUser = userRepository.save(user);

        // ✅ 3. Save to Audit Log
        // New Value is the updated user object
        logAction("UPDATE_STATUS", "user", String.valueOf(userId), oldValue, savedUser);
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
            // Log error to console but don't fail the transaction
            System.err.println("Audit Log Failed: " + e.getMessage());
        }
    }

    private String convertToJson(Object object) {
        try {
            // Sanitize object to avoid LazyLoading/Proxy recursion issues
            Object safeObject = sanitizeForAudit(object);
            return objectMapper.writeValueAsString(safeObject);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    // Convert User entity to a simple Map to avoid serializing nested Carts/Addresses
    private Object sanitizeForAudit(Object obj) {
        if (obj instanceof User) {
            User u = (User) obj;
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("name", u.getFname() + " " + u.getLname());
            map.put("email", u.getEmail());
            map.put("status", u.getStatus().getStatus());
            map.put("provider", u.getLoginProvider());
            return map;
        }
        return obj;
    }
}