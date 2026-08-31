package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.ChartDataDTO;
import lk.dio.rush_jewels.dto.DashboardStatsDTO;
import lk.dio.rush_jewels.dto.StockAlertDTO;
import lk.dio.rush_jewels.service.AdminDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    public AdminDashboardController(AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/chart")
    public ResponseEntity<ChartDataDTO> getChartData(@RequestParam(defaultValue = "daily") String filter) {
        return ResponseEntity.ok(dashboardService.getChartData(filter));
    }

    // ✅ New Endpoint for Alerts
    @GetMapping("/alerts")
    public ResponseEntity<List<StockAlertDTO>> getStockAlerts() {
        return ResponseEntity.ok(dashboardService.getStockAlerts());
    }
}