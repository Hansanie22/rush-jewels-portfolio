package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.ItemSearchDTO;
import lk.dio.rush_jewels.dto.SaleDTO;
import lk.dio.rush_jewels.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/seasonal-sales")
public class SaleController {

    @Autowired
    private SaleService saleService;

    @GetMapping
    public ResponseEntity<List<SaleDTO>> getAllActiveSales() {
        return ResponseEntity.ok(saleService.getAllActiveSales());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemSearchDTO>> searchItems(@RequestParam("q") String query) {
        return ResponseEntity.ok(saleService.searchItems(query));
    }

    @PostMapping
    public ResponseEntity<SaleDTO> saveSale(@RequestBody SaleDTO dto) {
        return ResponseEntity.ok(saleService.saveSale(dto));
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<SaleDTO> updateSale(@PathVariable int id, @RequestBody SaleDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(saleService.saveSale(dto));
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<Void> deleteSale(@PathVariable int id) {
        saleService.deleteSale(id);
        return ResponseEntity.ok().build();
    }
}
