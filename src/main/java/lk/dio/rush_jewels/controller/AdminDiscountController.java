package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.DiscountCodeDTO;
import lk.dio.rush_jewels.service.AdminDiscountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/coupons")
public class AdminDiscountController {

    private final AdminDiscountService discountService;

    public AdminDiscountController(AdminDiscountService discountService) {
        this.discountService = discountService;
    }

    @GetMapping
    public ResponseEntity<List<DiscountCodeDTO>> getAllCoupons() {
        return ResponseEntity.ok(discountService.getAllCoupons());
    }

    @PostMapping
    public ResponseEntity<?> createCoupon(@RequestBody DiscountCodeDTO dto) {
        try {
            discountService.saveCoupon(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<?> updateCoupon(@PathVariable int id, @RequestBody DiscountCodeDTO dto) {
        try {
            discountService.updateCoupon(id, dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<?> deleteCoupon(@PathVariable int id) {
        try {
            discountService.deleteCoupon(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}