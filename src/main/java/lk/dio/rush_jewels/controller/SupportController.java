package lk.dio.rush_jewels.controller;

import jakarta.servlet.http.HttpSession;
import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.service.SupportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/support")
public class SupportController {

    private final SupportService supportService;
    private static final String USER_SESSION_KEY = "user";

    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    private User getSessionUser(HttpSession session) {
        return (User) session.getAttribute(USER_SESSION_KEY);
    }

    // 1. List Tickets
    @GetMapping("/tickets")
    public ResponseEntity<?> getTickets(HttpSession session) {
        User user = getSessionUser(session);
        if (user == null) return ResponseEntity.status(401).body(Map.of("status", false, "message", "Unauthorized"));

        return ResponseEntity.ok(Map.of("status", true, "tickets", supportService.getUserTickets(user)));
    }

    // HEAD method for session checks
    @RequestMapping(value = "/tickets", method = RequestMethod.HEAD)
    public ResponseEntity<?> checkSession(HttpSession session) {
        User user = getSessionUser(session);
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok().build();
    }

    // 2. Create Ticket
    // NOTE: HTML Form sends name="file", so we keep "file" here
    @PostMapping("/tickets")
    public ResponseEntity<?> createTicket(
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpSession session
    ) {
        User user = getSessionUser(session);
        if (user == null) return ResponseEntity.status(401).body(Map.of("status", false, "message", "Unauthorized"));

        try {
            return ResponseEntity.ok(Map.of(
                    "status", true,
                    "ticket", supportService.createTicket(user, subject, message, file)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", false, "message", e.getMessage()));
        }
    }

    // 3. Get Ticket Details (Chat)
    @GetMapping("/tickets/{id}")
    public ResponseEntity<?> getTicketDetails(@PathVariable int id, HttpSession session) {
        User user = getSessionUser(session);
        if (user == null) return ResponseEntity.status(401).body(Map.of("status", false, "message", "Unauthorized"));

        try {
            return ResponseEntity.ok(Map.of(
                    "status", true,
                    "ticket", supportService.getTicketDetails(id, user)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", false, "message", e.getMessage()));
        }
    }

    // 4. Reply to Ticket (UPDATED)
    @PostMapping("/tickets/{id}/reply")
    public ResponseEntity<?> replyToTicket(
            @PathVariable int id,
            @RequestParam("message") String message,
            // CRITICAL UPDATE: Changed "file" to "attachment" to match JS FormData
            @RequestParam(value = "attachment", required = false) MultipartFile file,
            HttpSession session
    ) {
        User user = getSessionUser(session);
        if (user == null) return ResponseEntity.status(401).body(Map.of("status", false, "message", "Unauthorized"));

        try {
            return ResponseEntity.ok(Map.of(
                    "status", true,
                    "message", supportService.replyToTicket(id, user, message, file)
            ));
        } catch (Exception e) {
            e.printStackTrace(); // Helpful for debugging server logs
            return ResponseEntity.badRequest().body(Map.of("status", false, "message", e.getMessage()));
        }
    }
}