package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.AnalyticsDTOs;
import lk.dio.rush_jewels.service.AdminAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsController {

    private final AdminAnalyticsService analyticsService;

    public AdminAnalyticsController(AdminAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/sales")
    public ResponseEntity<List<AnalyticsDTOs.SalesReportDTO>> getSalesReport(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        return ResponseEntity.ok(analyticsService.getSalesReport(type, start, end, year, month));
    }

    @GetMapping("/product")
    public ResponseEntity<List<AnalyticsDTOs.ProductPerformanceDTO>> getProductInsights(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end
    ) {
        return ResponseEntity.ok(analyticsService.getProductInsights(start, end));
    }

    @GetMapping("/finance")
    public ResponseEntity<List<AnalyticsDTOs.FinanceReportDTO>> getFinanceReport(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end
    ) {
        return ResponseEntity.ok(analyticsService.getFinanceReport(start, end));
    }

    @GetMapping("/best-sellers")
    public ResponseEntity<List<AnalyticsDTOs.ProductPerformanceDTO>> getBestSellersReport(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end
    ) {
        return ResponseEntity.ok(analyticsService.getBestSellersReport(start, end));
    }

    @GetMapping("/top-customers")
    public ResponseEntity<List<AnalyticsDTOs.TopCustomerDTO>> getTopCustomersReport(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end
    ) {
        return ResponseEntity.ok(analyticsService.getTopCustomersReport(start, end));
    }

    @GetMapping("/order-status")
    public ResponseEntity<List<AnalyticsDTOs.OrderStatusDTO>> getOrderStatusReport(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end
    ) {
        return ResponseEntity.ok(analyticsService.getOrderStatusReport(start, end));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<AnalyticsDTOs.TransactionHistoryDTO>> getTransactionHistory(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end
    ) {
        return ResponseEntity.ok(analyticsService.getTransactionHistory(start, end));
    }
}