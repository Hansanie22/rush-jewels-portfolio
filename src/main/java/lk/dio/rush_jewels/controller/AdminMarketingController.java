package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.ChartDataDTO;
import lk.dio.rush_jewels.dto.MarketingStatsDTO;
import lk.dio.rush_jewels.model.ProductVariance;
import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.repository.ProductVarianceRepository;
import lk.dio.rush_jewels.repository.UserRepository;
import lk.dio.rush_jewels.service.AdminMarketingService;
import lk.dio.rush_jewels.service.OrderEmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/marketing")
public class AdminMarketingController {

    private final AdminMarketingService marketingService;
    private final ProductVarianceRepository productVarianceRepo;
    private final UserRepository userRepo;
    private final OrderEmailService orderEmailService;

    public AdminMarketingController(AdminMarketingService marketingService, 
                                    ProductVarianceRepository productVarianceRepo, 
                                    UserRepository userRepo, 
                                    OrderEmailService orderEmailService) {
        this.marketingService = marketingService;
        this.productVarianceRepo = productVarianceRepo;
        this.userRepo = userRepo;
        this.orderEmailService = orderEmailService;
    }

    @GetMapping("/stats")
    public ResponseEntity<MarketingStatsDTO> getStats() {
        return ResponseEntity.ok(marketingService.getMarketingStats());
    }

    // ✅ NEW Endpoint
    @GetMapping("/chart")
    public ResponseEntity<ChartDataDTO> getChart() {
        return ResponseEntity.ok(marketingService.getMarketingChartData());
    }

    // ✅ Smart Campaigns
    @PostMapping("/smart-campaign/{type}")
    public ResponseEntity<?> triggerSmartCampaign(@PathVariable String type) {
        try {
            List<ProductVariance> products;
            String title;
            String subtitle;

            if ("new-arrivals".equalsIgnoreCase(type)) {
                products = productVarianceRepo.findTop6ByProduct_Status_IdOrderByIdDesc(1);
                title = "Discover Our Latest Additions!";
                subtitle = "Handpicked new arrivals just for you. Upgrade your style with our premium collection.";
            } else if ("hot-deals".equalsIgnoreCase(type)) {
                products = productVarianceRepo.findTop6ByProduct_Status_IdOrderByDiscountPercentageDesc(1);
                title = "Exclusive Deals Just For You!";
                subtitle = "Don't miss out on these amazing discounts. Shop our hot deals before they are gone.";
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid campaign type."));
            }

            if (products.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "No products found for this campaign."));
            }

            List<User> subscribers = userRepo.findBySubscribedTrue();
            if (subscribers.isEmpty()) {
                return ResponseEntity.ok(Map.of("success", true, "message", "No active subscribers found."));
            }

            orderEmailService.sendSmartCampaign(title, subtitle, products, subscribers);

            return ResponseEntity.ok(Map.of("success", true, "message", "Campaign launched successfully! Email is being sent to " + subscribers.size() + " subscribers."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Failed to launch campaign: " + e.getMessage()));
        }
    }
}