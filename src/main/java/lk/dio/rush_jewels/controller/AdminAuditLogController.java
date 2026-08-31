// AdminController.java
package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.model.AdminAuditLog;
import lk.dio.rush_jewels.repository.AdminAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
public class AdminAuditLogController {

    @Autowired
    private AdminAuditLogRepository auditLogRepository;

    @GetMapping("/api/logs/today")
    public List<AdminAuditLog> getTodayLogs() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        return auditLogRepository.findAll().stream()
                .filter(log -> log.getActionTime().isAfter(startOfDay.minusSeconds(1)) &&
                        log.getActionTime().isBefore(endOfDay.plusSeconds(1)))
                .toList();
    }
}
