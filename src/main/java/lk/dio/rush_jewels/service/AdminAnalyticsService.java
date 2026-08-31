package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.dto.AnalyticsDTOs;
import lk.dio.rush_jewels.repository.OrderItemsRepository;
import lk.dio.rush_jewels.repository.PaymentRepository;
import lk.dio.rush_jewels.repository.ReturnRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminAnalyticsService {

    private final PaymentRepository paymentRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final ReturnRepository returnRepository;
    private final lk.dio.rush_jewels.repository.OrdersRepository ordersRepository;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AdminAnalyticsService(PaymentRepository paymentRepository,
                                 OrderItemsRepository orderItemsRepository,
                                 ReturnRepository returnRepository,
                                 lk.dio.rush_jewels.repository.OrdersRepository ordersRepository) {
        this.paymentRepository = paymentRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.returnRepository = returnRepository;
        this.ordersRepository = ordersRepository;
    }

    // 1. Enhanced Sales Report
    public List<AnalyticsDTOs.SalesReportDTO> getSalesReport(String type, String startStr, String endStr,
                                                             Integer year, Integer month) {
        List<Object[]> salesResults;
        List<Object[]> returnResults;

        if ("monthly".equalsIgnoreCase(type)) {
            // Monthly view
            int targetYear = (year != null) ? year : LocalDate.now().getYear();
            int targetMonth = (month != null) ? month : LocalDate.now().getMonthValue();

            YearMonth yearMonth = YearMonth.of(targetYear, targetMonth);
            LocalDate startDate = yearMonth.atDay(1);
            LocalDate endDate = yearMonth.atEndOfMonth();

            Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date end = Date.from(endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

            salesResults = paymentRepository.getSalesByDateRange(start, end);

            // Fetch Returns based on Approved Date
            LocalDateTime startLdt = startDate.atStartOfDay();
            LocalDateTime endLdt = endDate.atTime(LocalTime.MAX);
            returnResults = returnRepository.getReturnsByDateRange(startLdt, endLdt);

        } else if ("yearly".equalsIgnoreCase(type)) {
            // Yearly view
            int targetYear = (year != null) ? year : LocalDate.now().getYear();
            salesResults = paymentRepository.getMonthlySalesByYear(targetYear);
            returnResults = returnRepository.getMonthlyReturnsByYear(targetYear);

        } else {
            // Daily / Range view
            LocalDate start = (startStr != null && !startStr.isEmpty())
                    ? LocalDate.parse(startStr)
                    : LocalDate.now().minusDays(29);
            LocalDate end = (endStr != null && !endStr.isEmpty())
                    ? LocalDate.parse(endStr)
                    : LocalDate.now();

            Date startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(end.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

            salesResults = paymentRepository.getSalesByDateRange(startDate, endDate);

            // Fetch Returns based on Approved Date
            LocalDateTime startLdt = start.atStartOfDay();
            LocalDateTime endLdt = end.atTime(LocalTime.MAX);
            returnResults = returnRepository.getReturnsByDateRange(startLdt, endLdt);
        }

        // Merge Data
        Map<String, AnalyticsDTOs.SalesReportDTO> reportMap = new LinkedHashMap<>();

        // Add Sales
        for (Object[] row : salesResults) {
            String key = row[0].toString();
            reportMap.put(key, new AnalyticsDTOs.SalesReportDTO(
                    key,
                    ((Number) row[1]).longValue(),
                    ((Number) row[2]).doubleValue()
            ));
        }

        // Merge Returns (Status=COMPLETED, Type=1or2, Date=ApprovedDate)
        for (Object[] row : returnResults) {
            String key = row[0].toString();
            long retCount = ((Number) row[1]).longValue();
            double retVal = ((Number) row[2]).doubleValue();

            if (reportMap.containsKey(key)) {
                AnalyticsDTOs.SalesReportDTO dto = reportMap.get(key);
                dto.setReturnStats(retCount, retVal);
            } else {
                AnalyticsDTOs.SalesReportDTO dto = new AnalyticsDTOs.SalesReportDTO(key, 0L, 0.0);
                dto.setReturnStats(retCount, retVal);
                reportMap.put(key, dto);
            }
        }

        return new ArrayList<>(reportMap.values());
    }

    // 2. Enhanced Product Insights
    public List<AnalyticsDTOs.ProductPerformanceDTO> getProductInsights(String startStr, String endStr) {
        List<Object[]> results;

        if (startStr != null && !startStr.isEmpty() && endStr != null && !endStr.isEmpty()) {
            LocalDate start = LocalDate.parse(startStr);
            LocalDate end = LocalDate.parse(endStr);

            Date startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(end.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

            // Convert to String for the native query's Return Date check
            String startLdtStr = start.atStartOfDay().format(DATETIME_FORMATTER);
            String endLdtStr = end.atTime(LocalTime.MAX).format(DATETIME_FORMATTER);

            results = orderItemsRepository.getProductPerformanceByDateRange(startDate, endDate, startLdtStr, endLdtStr);
        } else {
            results = orderItemsRepository.getProductPerformance();
        }

        return results.stream().map(row -> new AnalyticsDTOs.ProductPerformanceDTO(
                (String) row[0], // Name
                (String) row[1], // Category
                ((Number) row[2]).longValue(), // Gross Units Sold
                ((Number) row[3]).doubleValue(), // Gross Revenue
                ((Number) row[4]).intValue(), // Current Stock
                ((Number) row[5]).longValue() // Units Returned
        )).collect(Collectors.toList());
    }

    // 3. Finance Report (Unchanged, included for completeness)
    public List<AnalyticsDTOs.FinanceReportDTO> getFinanceReport(String startStr, String endStr) {
        List<Object[]> results;
        if (startStr != null && !startStr.isEmpty() && endStr != null && !endStr.isEmpty()) {
            LocalDate start = LocalDate.parse(startStr);
            LocalDate end = LocalDate.parse(endStr);
            Date startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(end.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
            results = paymentRepository.getFinanceByMethodAndDateRange(startDate, endDate);
        } else {
            results = paymentRepository.getFinanceByMethod();
        }

        return results.stream().map(row -> new AnalyticsDTOs.FinanceReportDTO(
                (String) row[0],
                ((Number) row[1]).longValue(),
                ((Number) row[2]).doubleValue(),
                ((Number) row[3]).doubleValue(),
                ((Number) row[4]).doubleValue()
        )).collect(Collectors.toList());
    }

    // 4. Best Selling Products Report (Using existing Product Performance Data)
    public List<AnalyticsDTOs.ProductPerformanceDTO> getBestSellersReport(String startStr, String endStr) {
        List<Object[]> rawData;
        if (startStr != null && !startStr.isEmpty() && endStr != null && !endStr.isEmpty()) {
            LocalDate start = LocalDate.parse(startStr);
            LocalDate end = LocalDate.parse(endStr);
            Date startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(end.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
            String startLdtStr = start.atStartOfDay().format(DATETIME_FORMATTER);
            String endLdtStr = end.atTime(LocalTime.MAX).format(DATETIME_FORMATTER);
            rawData = orderItemsRepository.getProductPerformanceByDateRange(startDate, endDate, startLdtStr, endLdtStr);
        } else {
            rawData = orderItemsRepository.getProductPerformance();
        }
        
        return rawData.stream()
                .limit(10) // Top 10 Best Sellers
                .map(row -> new AnalyticsDTOs.ProductPerformanceDTO(
                        (String) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).doubleValue(),
                        ((Number) row[4]).intValue(),
                        ((Number) row[5]).longValue()
                ))
                .collect(Collectors.toList());
    }

    // 5. Top Customers Report
    public List<AnalyticsDTOs.TopCustomerDTO> getTopCustomersReport(String startStr, String endStr) {
        List<Object[]> results;
        if (startStr != null && !startStr.isEmpty() && endStr != null && !endStr.isEmpty()) {
            LocalDate start = LocalDate.parse(startStr);
            LocalDate end = LocalDate.parse(endStr);
            Date startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(end.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
            results = paymentRepository.getTopCustomersByDateRange(startDate, endDate, PageRequest.of(0, 10));
        } else {
            results = paymentRepository.getTopCustomers(PageRequest.of(0, 10));
        }
        return results.stream()
                .map(row -> new AnalyticsDTOs.TopCustomerDTO(
                        row[0] + " " + row[1], // Name
                        ((Number) row[2]).longValue(), // Count
                        ((Number) row[3]).doubleValue() // Spent
                ))
                .collect(Collectors.toList());
    }

    // 6. Order Status Breakdown Report
    public List<AnalyticsDTOs.OrderStatusDTO> getOrderStatusReport(String startStr, String endStr) {
        List<Object[]> results;
        if (startStr != null && !startStr.isEmpty() && endStr != null && !endStr.isEmpty()) {
            LocalDate start = LocalDate.parse(startStr);
            LocalDate end = LocalDate.parse(endStr);
            Date startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(end.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
            results = ordersRepository.getOrderStatusBreakdownByDateRange(startDate, endDate);
        } else {
            results = ordersRepository.getOrderStatusBreakdown();
        }
        return results.stream()
                .map(row -> new AnalyticsDTOs.OrderStatusDTO(
                        (String) row[0], // Status
                        ((Number) row[1]).longValue() // Count
                ))
                .collect(Collectors.toList());
    }

    // 7. Transaction History Report
    public List<AnalyticsDTOs.TransactionHistoryDTO> getTransactionHistory(String startStr, String endStr) {
        LocalDate start = (startStr != null && !startStr.isEmpty()) ? LocalDate.parse(startStr) : LocalDate.now();
        LocalDate end   = (endStr   != null && !endStr.isEmpty())   ? LocalDate.parse(endStr)   : LocalDate.now();

        Date startDate = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate   = Date.from(end.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

        List<Object[]> results = paymentRepository.getTransactionHistory(startDate, endDate);

        return results.stream().map(row -> new AnalyticsDTOs.TransactionHistoryDTO(
                row[0] != null ? row[0].toString() : "-",                           // dateTime
                row[1] != null ? row[1].toString() : "-",                           // orderId
                row[2] != null ? row[2].toString() : "-",                           // transactionId
                row[3] != null ? row[3].toString() : "Unknown",                     // customerName
                row[4] != null ? row[4].toString().toUpperCase() : "WEB",           // channel (WEB/POS)
                row[5] != null ? row[5].toString() : "-",                           // paymentMethod
                row[6] != null ? row[6].toString() : "-",                           // paymentStatus
                row[7] != null ? ((Number) row[7]).doubleValue() : 0.0,             // subTotal
                row[8] != null ? ((Number) row[8]).doubleValue() : 0.0,             // discount
                row[9] != null ? ((Number) row[9]).doubleValue() : 0.0,             // finalTotal
                row[10] != null ? ((Number) row[10]).doubleValue() : 0.0,           // tenderedAmount
                row[11] != null ? ((Number) row[11]).doubleValue() : 0.0            // changeDue
        )).collect(Collectors.toList());
    }
}