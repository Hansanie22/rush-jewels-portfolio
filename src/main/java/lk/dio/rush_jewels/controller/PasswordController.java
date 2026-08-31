package lk.dio.rush_jewels.controller;

import jakarta.servlet.http.HttpSession;
import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime; // Import if you have verificationExpiry
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class PasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String USER_SESSION_KEY = "user";

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(HttpSession session, @RequestBody Map<String, String> payload) {

        User sessionUser = (User) session.getAttribute(USER_SESSION_KEY);
        if (sessionUser == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("status", false, "message", "Not logged in"));
        }

        // --- 1. Get Fields from Payload ---
        // CHANGED: Get 'credential' instead of 'currentPassword'
        String credential = payload.getOrDefault("credential", "").trim();
        String newPassword = payload.getOrDefault("newPassword", "").trim();
        String confirmPassword = payload.getOrDefault("confirmPassword", "").trim();

        // --- 2. Basic Validation ---
        // CHANGED: Check 'credential'
        if (credential.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("status", false, "message", "All fields are required."));
        }

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("status", false, "message", "New passwords do not match."));
        }

        // --- 3. Get Fresh User from Database ---
        Optional<User> optionalUser = userRepository.findById(sessionUser.getId());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("status", false, "message", "User not found."));
        }
        User user = optionalUser.get();

        // --- 4. NEW VALIDATION: Check credential as Password OR Code ---
        String storedPassword = user.getPassword();
        String storedCode = user.getVerification();

        boolean passwordMatches = false;
        boolean codeMatches = false;

        // Check 1: Is it their password?
        if (storedPassword != null && storedPassword.startsWith("$2a$")) {
            passwordMatches = passwordEncoder.matches(credential, storedPassword);
        } else if (storedPassword != null) {
            passwordMatches = credential.equals(storedPassword); // Legacy plain-text check
        }

        // Check 2: Is it their verification code?
        // (We check this *even if* password check failed)
        if (storedCode != null && !storedCode.isEmpty()) {
            // Add expiry check if you have one, e.g.:
            // && user.getVerificationExpiry().isAfter(LocalDateTime.now())
            codeMatches = credential.equals(storedCode);
        }

        // Check 3: Final decision
        if (!passwordMatches && !codeMatches) {
            return ResponseEntity.badRequest().body(Map.of("status", false, "message", "Current password or code is incorrect."));
        }

        // --- 5. Prevent Password Reuse ---
        if (storedPassword != null && storedPassword.startsWith("$2a$") && passwordEncoder.matches(newPassword, storedPassword)) {
            return ResponseEntity.badRequest().body(Map.of("status", false, "message", "New password cannot be the same as the old password."));
        }

        // --- 6. Save New Password ---
        user.setPassword(passwordEncoder.encode(newPassword));

        // --- 7. SECURITY FIX: Consume the verification code if it was used ---
        if (codeMatches) {
            user.setVerification(null);
            // Also nullify the expiry date if you have one
            // user.setVerificationExpiry(null);
        }

        userRepository.save(user);

        // Update session
        session.setAttribute(USER_SESSION_KEY, user);

        return ResponseEntity.ok(Map.of("status", true, "message", "Password updated successfully."));
    }
}