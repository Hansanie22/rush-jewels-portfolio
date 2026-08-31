package lk.dio.rush_jewels.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.dto.ChartDataDTO;
import lk.dio.rush_jewels.dto.DashboardStatsDTO;
import lk.dio.rush_jewels.dto.StockAlertDTO;
import lk.dio.rush_jewels.model.AdminAuditLog;
import lk.dio.rush_jewels.model.Stock;
import lk.dio.rush_jewels.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminDashboardService {

    private final PaymentRepository paymentRepository;
    private final OrdersRepository ordersRepository;
    private final UserRepository userRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final StockRepository stockRepository;

    public AdminDashboardService(PaymentRepository paymentRepository,
                                 OrdersRepository ordersRepository,
                                 UserRepository userRepository,
                                 AdminAuditLogRepository auditLogRepository,
                                 ObjectMapper objectMapper,
                                 StockRepository stockRepository) {
        this.paymentRepository = paymentRepository;
        this.ordersRepository = ordersRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
        this.stockRepository = stockRepository;
    }

    public DashboardStatsDTO getStats() {
        Double revenue = paymentRepository.calculateTotalRevenue();
        if (revenue == null) revenue = 0.0;
        long totalOrders = ordersRepository.count();
        long pendingOrders = ordersRepository.countByOrderStatus_OrderStatus("Order Placed");
        long totalCustomers = userRepository.count();
        long newCustomers = userRepository.countNewCustomersThisMonth();
        double avgOrder = totalOrders > 0 ? revenue / totalOrders : 0.0;

        return new DashboardStatsDTO(revenue, totalOrders, pendingOrders, totalCustomers, newCustomers, avgOrder);
    }

    public ChartDataDTO getChartData(String filter) {
        List<Object[]> rawData;
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        switch (filter.toLowerCase()) {
            case "weekly":
                rawData = paymentRepository.getWeeklySales();
                for (Object[] row : rawData) {
                    String weekStr = row[0].toString();
                    String weekNum = weekStr.contains("-") ? weekStr.split("-")[1] : weekStr;
                    labels.add("W" + weekNum);
                    values.add(((Number) row[1]).doubleValue());
                }
                break;
            case "monthly":
                rawData = paymentRepository.getMonthlySales();
                for (Object[] row : rawData) {
                    labels.add(row[0].toString());
                    values.add(((Number) row[1]).doubleValue());
                }
                break;
            case "daily":
            default:
                rawData = paymentRepository.getDailySales();
                for (Object[] row : rawData) {
                    String dateStr = row[0].toString();
                    String shortDate = dateStr.length() >= 10 ? dateStr.substring(5) : dateStr;
                    labels.add(shortDate);
                    values.add(((Number) row[1]).doubleValue());
                }
                break;
        }
        return new ChartDataDTO(labels, values);
    }

    public List<StockAlertDTO> getStockAlerts() {
        // Status 2 = Low Stock, Status 3 = Out of Stock
        List<Stock> criticalStocks = stockRepository.findByStockStatus_IdIn(List.of(2, 3));
        List<StockAlertDTO> alerts = new ArrayList<>();

        for (Stock stock : criticalStocks) {
            String name = "Unknown Item";
            String type = "Unknown";
            String status = stock.getStockStatus().getId() == 3 ? "Out of Stock" : "Low Stock";

            // ✅ Get Warehouse Name
            String warehouse = (stock.getWarehouse() != null) ? stock.getWarehouse().getWarehouse() : "Unassigned";

            if (stock.getProductVariance() != null) {
                String pName = stock.getProductVariance().getProduct().getTitle();
                if(pName == null) pName = "Product #" + stock.getProductVariance().getProduct().getId();

                // Add Size/Color details if available for better context
                String size = (stock.getProductVariance().getSize() != null) ? " [" + stock.getProductVariance().getSize().getSize() + "]" : "";

                name = pName + size;
                type = "Product";
            } else if (stock.getCollection() != null) {
                name = stock.getCollection().getName();
                type = "Collection";
            }

            alerts.add(new StockAlertDTO(name, stock.getQty(), status, type, warehouse));
        }

        if (!alerts.isEmpty()) {
            logAction("STOCK_ALERT", "Dashboard showed " + alerts.size() + " inventory warnings.");
        }

        return alerts;
    }

    private void logAction(String action, String message) {
        try {
            Map<String, String> details = new HashMap<>();
            details.put("info", message);
            String jsonDetails = objectMapper.writeValueAsString(details);

            AdminAuditLog log = new AdminAuditLog(
                    action,
                    "dashboard",
                    "0",
                    null,
                    jsonDetails,
                    LocalDateTime.now()
            );
            auditLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Dashboard Audit Log Failed: " + e.getMessage());
        }
    }
}