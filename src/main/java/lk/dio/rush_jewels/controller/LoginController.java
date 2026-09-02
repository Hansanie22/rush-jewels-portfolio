package lk.dio.rush_jewels.controller;

import jakarta.servlet.http.HttpSession;
import lk.dio.rush_jewels.dto.LoginRequest;
import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String USER_SESSION_KEY = "user";

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = Optional.ofNullable(request.getEmail()).orElse("").trim();
            String password = Optional.ofNullable(request.getPassword()).orElse("").trim();

            if (email.isEmpty()) return badResponse(response, "Email cannot be empty.");
            if (password.isEmpty()) return badResponse(response, "Password cannot be empty.");

            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isEmpty()) return unauthorizedResponse(response);

            User user = optionalUser.get();

            boolean isResetCode = user.getPassword() != null && user.getPassword().matches("\\d{6}");
            boolean matchesPassword = user.getPassword() != null && passwordEncoder.matches(password, user.getPassword());

            // Fallback: If password was stored as plaintext (e.g. from SQL seed data), auto-upgrade to BCrypt
            if (!matchesPassword && !(isResetCode && password.equals(user.getPassword()))) {
                if (user.getPassword() != null && password.equals(user.getPassword())) {
                    user.setPassword(passwordEncoder.encode(password));
                    if (user.getVerification() == null || user.getVerification().isEmpty()) {
                        user.setVerification("Verified");
                    }
                    userRepository.save(user);
                    matchesPassword = true;
                } else {
                    return unauthorizedResponse(response);
                }
            }

            String redirect;
            if (isResetCode && password.equals(user.getPassword())) {
                session.setAttribute("email", email);
                redirect = "/account.html?tab=password";
                response.put("message", "Verification code accepted. Please reset your password.");
            } else if (user.getVerification() != null && !user.getVerification().isEmpty() && !"Verified".equalsIgnoreCase(user.getVerification())) {
                session.setAttribute("email", email);
                redirect = "/verify-account.html";
                response.put("message", "Account not verified.");
            } else {
                redirect = "/index.html";
                response.put("message", "Login successful!");
            }

            // If user's verification is null/empty, mark as Verified now that login succeeded
            if (user.getVerification() == null || user.getVerification().isEmpty()) {
                user.setVerification("Verified");
                userRepository.save(user);
            }

            // This is fine for the server-side session
            session.setAttribute(USER_SESSION_KEY, user);

            // === THIS IS THE FIX ===
            // Create a "safe" user object to send to the frontend
            // This avoids the JSON infinite loop and sending sensitive data
            Map<String, Object> safeUser = new HashMap<>();
            safeUser.put("id", user.getId()); // Assuming User has getId()
            safeUser.put("firstName", user.getFname()); // Assuming User has getFirstName()
            safeUser.put("lastName", user.getLname()); // Assuming User has getLastName()
            safeUser.put("email", user.getEmail());
            // DO NOT add password, tokens, etc.

            response.put("status", true);
            response.put("redirect", redirect);
            response.put("user", safeUser); // Send the safeUser map, NOT the 'user' entity
            // === END OF FIX ===

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", false);
            response.put("message", "Server error.");
            return ResponseEntity.status(500).body(response);
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("status", true, "message", "Logout successful."));
    }

    private ResponseEntity<Map<String, Object>> badResponse(Map<String, Object> map, String message) {
        map.put("status", false);
        map.put("message", message);
        return ResponseEntity.badRequest().body(map);
    }

    private ResponseEntity<Map<String, Object>> unauthorizedResponse(Map<String, Object> map) {
        map.put("status", false);
        map.put("message", "Invalid email or password.");
        return ResponseEntity.status(401).body(map);
    }
}