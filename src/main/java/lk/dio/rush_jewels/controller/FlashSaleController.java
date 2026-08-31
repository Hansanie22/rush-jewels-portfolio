package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.FlashSaleDTO;
import lk.dio.rush_jewels.service.FlashSaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flash-sale")
public class FlashSaleController {

    @Autowired
    private FlashSaleService flashSaleService;

    @GetMapping("/latest")
    public FlashSaleDTO getLatestSale() {
        return flashSaleService.getLatestFlashSale();
    }
}
