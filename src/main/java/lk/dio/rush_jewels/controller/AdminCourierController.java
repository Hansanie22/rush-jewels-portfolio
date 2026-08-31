package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.model.CourierCompany;
import lk.dio.rush_jewels.model.Shipping;
import lk.dio.rush_jewels.service.AdminCourierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/courier")
public class AdminCourierController {

    private final AdminCourierService courierService;

    public AdminCourierController(AdminCourierService courierService) {
        this.courierService = courierService;
    }

    // --- Shipping Methods ---

    @GetMapping("/shipping")
    public ResponseEntity<List<Shipping>> getShippingMethods() {
        return ResponseEntity.ok(courierService.getAllActiveShippingMethods());
    }

    @PostMapping("/shipping")
    public ResponseEntity<?> saveShippingMethod(@RequestBody Shipping shipping) {
        // Handles both Add (id=null) and Update (id exists)
        return ResponseEntity.ok(courierService.saveShippingMethod(shipping));
    }

    @PostMapping("/shipping/{id}/delete")
    public ResponseEntity<?> removeShippingMethod(@PathVariable int id) {
        courierService.softDeleteShippingMethod(id);
        return ResponseEntity.ok().build();
    }

    // --- Courier Companies ---

    @GetMapping("/companies")
    public ResponseEntity<List<CourierCompany>> getCourierCompanies() {
        return ResponseEntity.ok(courierService.getAllActiveCourierCompanies());
    }

    @PostMapping("/companies")
    public ResponseEntity<?> saveCourierCompany(@RequestBody CourierCompany company) {
        // Handles both Add (id=null) and Update (id exists)
        return ResponseEntity.ok(courierService.saveCourierCompany(company));
    }

    @PostMapping("/companies/{id}/delete")
    public ResponseEntity<?> removeCourierCompany(@PathVariable int id) {
        courierService.softDeleteCourierCompany(id);
        return ResponseEntity.ok().build();
    }
}