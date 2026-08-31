package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.SeasonalSaleBannerDTO;
import lk.dio.rush_jewels.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicSaleController {

    @Autowired
    private SaleService saleService;

    // Public endpoint for seasonal sale banner
    @GetMapping("/api/seasonal-sale-banner")
    public ResponseEntity<SeasonalSaleBannerDTO> getSeasonalSaleBanner() {
        return ResponseEntity.ok(saleService.getLatestSeasonalSaleBanner());
    }
}
