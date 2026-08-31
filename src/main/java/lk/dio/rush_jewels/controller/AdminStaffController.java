package lk.dio.rush_jewels.controller;

import jakarta.servlet.http.HttpSession;
import lk.dio.rush_jewels.dto.AdminDTO;
import lk.dio.rush_jewels.dto.AdminProfileDTO;
import lk.dio.rush_jewels.dto.AdminProfileUpdateDTO; // ✅ Added
import lk.dio.rush_jewels.service.AdminStaffService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/staff")
public class AdminStaffController {

    private final AdminStaffService staffService;

    public AdminStaffController(AdminStaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public ResponseEntity<List<AdminDTO>> getAllStaff() {
        return ResponseEntity.ok(staffService.getAllStaff());
    }

    // Get Current Profile
    @GetMapping("/profile")
    public ResponseEntity<AdminProfileDTO> getProfile(HttpSession session) {
        Integer adminId = (Integer) session.getAttribute("adminId");
        if (adminId != null) {
            return ResponseEntity.ok(staffService.getAdminProfile(adminId));
        }
        return ResponseEntity.status(401).build();
    }

    @PostMapping("/profile/update")
    public ResponseEntity<?> updateProfile(@RequestBody AdminProfileUpdateDTO dto, HttpSession session) {
        try {
            Integer adminId = (Integer) session.getAttribute("adminId");
            if (adminId == null) return ResponseEntity.status(401).build();
            
            staffService.updateProfile(adminId, dto);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            // 401 Unauthorized if current password doesn't match
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createAdmin(@RequestBody AdminDTO dto) {
        try {
            staffService.createAdmin(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable int id, @RequestBody Map<String, Boolean> payload) {
        staffService.updateStatus(id, payload.get("active"));
        return ResponseEntity.ok().build();
    }
}