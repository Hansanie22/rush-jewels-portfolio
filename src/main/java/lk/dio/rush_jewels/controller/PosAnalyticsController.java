package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.service.PosAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/analytics/pos")
public class PosAnalyticsController {

    private final PosAnalyticsService analyticsService;

    public PosAnalyticsController(PosAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/sales-report")
    public ResponseEntity<Map<String, Object>> getSalesReport() {
        return ResponseEntity.ok(analyticsService.getSalesSplitReport());
    }
}
