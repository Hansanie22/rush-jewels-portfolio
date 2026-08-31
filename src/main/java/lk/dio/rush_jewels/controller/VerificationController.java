package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class VerificationController {

    private final UserService userService;

    public VerificationController(UserService userService) {
        this.userService = userService;
    }

    // ✅ Verify Account (POST)
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyAccount(
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", false);

        // 1. Try to get email from Request Body (More reliable)
        String email = body.get("email");

        // 2. If not in body, try Session (Fallback)
        if (email == null || email.isEmpty()) {
            email = (String) session.getAttribute("email");
        }

        if (email == null) {
            resp.put("message", "1"); // email missing
            return ResponseEntity.ok(resp);
        }

        String code = body.get("verificationCode");
        Optional<User> optional = userService.findUserByEmail(email);

        if (optional.isEmpty()) {
            resp.put("message", "1"); // User not found
            return ResponseEntity.ok(resp);
        }

        User user = optional.get();

        // ✅ Check code
        if (user.getVerification() == null || !user.getVerification().equals(code)) {
            resp.put("message", "Invalid verification code!");
            return ResponseEntity.ok(resp);
        }

        // ✅ Check expiry
        Date now = new Date();
        if (user.getVerificationExpiry() != null &&
                user.getVerificationExpiry().before(now)) {

            resp.put("message", "Verification code has expired!");
            return ResponseEntity.ok(resp);
        }

        // ✅ Mark verified
        user.setVerification("Verified");
        user.setVerificationExpiry(null);
        userService.saveUser(user);

        // Update session user
        session.setAttribute("user", user);

        resp.put("status", true);
        resp.put("message", "Verification successful!");

        return ResponseEntity.ok(resp);
    }

    // ✅ Resend verification code
    @PostMapping("/resend")
    public ResponseEntity<Map<String, Object>> resendCode(
            @RequestBody(required = false) Map<String, String> body,
            HttpSession session
    ) {
        Map<String, Object> resp = new HashMap<>();

        // 1. Try to get email from Request Body
        String email = (body != null) ? body.get("email") : null;

        // 2. If not in body, try Session
        if (email == null || email.isEmpty()) {
            email = (String) session.getAttribute("email");
        }

        if (email == null) {
            resp.put("status", false);
            resp.put("message", "Email not found. Please login again.");
            return ResponseEntity.ok(resp);
        }

        try {
            boolean ok = userService.resendVerificationCode(email);

            if (ok) {
                resp.put("status", true);
                resp.put("message", "Verification code resent successfully!");
            } else {
                resp.put("status", false);
                resp.put("message", "User not found.");
            }

        } catch (Exception e) {
            resp.put("status", false);
            resp.put("message", "Server error: " + e.getMessage());
        }

        return ResponseEntity.ok(resp);
    }
}