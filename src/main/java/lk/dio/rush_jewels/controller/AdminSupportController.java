package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.SupportTicketDTO;
import lk.dio.rush_jewels.dto.TicketDetailDTO;
import lk.dio.rush_jewels.service.AdminSupportService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/support")
public class AdminSupportController {

    private final AdminSupportService supportService;

    public AdminSupportController(AdminSupportService supportService) {
        this.supportService = supportService;
    }

    @GetMapping
    public ResponseEntity<List<SupportTicketDTO>> getAllTickets() {
        return ResponseEntity.ok(supportService.getAllTickets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDetailDTO> getTicket(@PathVariable int id) {
        return ResponseEntity.ok(supportService.getTicketDetails(id));
    }

    // ✅ FIXED: Changed @RequestPart to @RequestParam for the string message
    @PostMapping(value = "/{id}/reply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> replyToTicket(
            @PathVariable int id,
            @RequestParam("message") String message, // Use RequestParam for text in FormData
            @RequestPart(value = "file", required = false) MultipartFile file) { // Keep RequestPart for File
        try {
            supportService.replyToTicket(id, message, file);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable int id, @RequestBody Map<String, String> payload) {
        try {
            supportService.updateStatus(id, payload.get("status"));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}