package lk.dio.rush_jewels.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.dto.NotificationDTO;
import lk.dio.rush_jewels.model.AdminAuditLog;
import lk.dio.rush_jewels.model.Orders;
import lk.dio.rush_jewels.model.Stock;
import lk.dio.rush_jewels.repository.AdminAuditLogRepository;
import lk.dio.rush_jewels.repository.OrdersRepository;
import lk.dio.rush_jewels.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional // Read-write for Audit Log
public class AdminNotificationService {

    private final OrdersRepository ordersRepository;
    private final StockRepository stockRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AdminNotificationService(OrdersRepository ordersRepository,
                                    StockRepository stockRepository,
                                    AdminAuditLogRepository auditLogRepository,
                                    ObjectMapper objectMapper) {
        this.ordersRepository = ordersRepository;
        this.stockRepository = stockRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public List<NotificationDTO> getNotifications() {
        List<NotificationDTO> notifications = new ArrayList<>();

        // 1. Get ALL New Orders (Status ID = 1)
        List<Orders> newOrders = ordersRepository.findByOrderStatus_IdOrderByOrderedAtDesc(1);

        for (Orders order : newOrders) {
            notifications.add(new NotificationDTO(
                    "ORDER",
                    "New Order " + order.getId(),
                    getTimeAgo(order.getOrderedAt()),
                    "fas fa-shopping-bag text-blue-500",
                    "orders"
            ));
        }

        // 2. Get ALL Low Stock Alerts (Stock Status ID = 2)
        List<Stock> lowStocks = stockRepository.findByStockStatus_Id(2);

        for (Stock s : lowStocks) {
            String name = "Unknown Item";
            if (s.getProductVariance() != null) {
                name = s.getProductVariance().getProduct().getName();
            } else if (s.getCollection() != null) {
                name = s.getCollection().getTitle();
            }

            notifications.add(new NotificationDTO(
                    "STOCK",
                    "Low Stock: " + name + " (" + s.getQty() + " left)",
                    "Action Needed",
                    "fas fa-exclamation-triangle text-red-500",
                    "inventory"
            ));
        }

        // ✅ Audit Log
        logAction("CHECK_NOTIFICATIONS", "Fetched " + notifications.size() + " total notifications");

        return notifications;
    }

    private String getTimeAgo(Date date) {
        if (date == null) return "Just now";
        LocalDateTime localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        Duration d = Duration.between(localDate, LocalDateTime.now());

        if (d.toMinutes() < 1) return "Just now";
        if (d.toMinutes() < 60) return d.toMinutes() + "m ago";
        if (d.toHours() < 24) return d.toHours() + "h ago";
        return d.toDays() + "d ago";
    }

    // ==========================================
    // AUDIT LOG HELPER METHODS
    // ==========================================

    private void logAction(String action, String message) {
        try {
            Map<String, String> details = new HashMap<>();
            details.put("info", message);
            String jsonDetails = objectMapper.writeValueAsString(details);

            AdminAuditLog log = new AdminAuditLog(
                    action,
                    "notification_system",
                    "0",
                    null,
                    jsonDetails,
                    LocalDateTime.now()
            );
            auditLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Notification Audit Log Failed: " + e.getMessage());
        }
    }
}