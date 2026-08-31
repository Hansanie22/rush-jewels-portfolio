package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.UserDTO;
import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private static final String USER_SESSION_KEY = "user";

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(
            @RequestBody Map<String, String> userJson,
            HttpSession session
    ) {
        try {
            String fname = userJson.getOrDefault("fname", "").trim();
            String lname = userJson.getOrDefault("lname", "").trim();
            String email = userJson.getOrDefault("email", "").trim();
            String password = userJson.getOrDefault("password", "").trim();

            String loginProvider = userJson.getOrDefault("loginProvider", "LOCAL")
                    .toUpperCase();

            String providerId = userJson.getOrDefault("providerId", null);

            if (email.isEmpty())
                return ResponseEntity.badRequest()
                        .body(Map.of("status", false, "message", "Email cannot be empty"));

            Optional<User> existingUser = userService.findUserByEmail(email);

            if (existingUser.isPresent()) {
                return ResponseEntity.ok(Map.of(
                        "status", false,
                        "message", "User already exists"
                ));
            }

            User newUser;

            if (loginProvider.equals("LOCAL")) {
                newUser = userService.registerLocalUser(fname, lname, email, password);
            } else {
                newUser = userService.registerSocialUser(fname, lname, email, loginProvider, providerId);
            }

            // ✅ 1. Store user object in session (General usage)
            session.setAttribute(USER_SESSION_KEY, newUser);

            // ✅ 2. Store EMAIL explicitly in session (For Verification Controller fallback)
            // මේ කොටස අනිවාර්යයෙන් එකතු කරන්න.
            session.setAttribute("email", newUser.getEmail());

            // ✅ Return DTO
            return ResponseEntity.ok(Map.of(
                    "status", true,
                    "message", "Registration successful",
                    "user", UserDTO.from(newUser)
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", false,
                    "message", e.getMessage()
            ));
        }
    }

    // Login Method එකක් ඔබ එවා නොතිබුණත්, Login වන අවස්ථාවේදීත් පහත දේ කරන්න:
    // session.setAttribute("email", user.getEmail());

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of(
                "status", true,
                "message", "Logout successful."
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpSession session) {
        User currentUser = (User) session.getAttribute(USER_SESSION_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", false,
                    "message", "No user logged in."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "status", true,
                "user", UserDTO.from(currentUser)
        ));
    }

    @GetMapping("/session-check")
    public ResponseEntity<Map<String, Object>> sessionCheck(HttpSession session) {
        User currentUser = (User) session.getAttribute(USER_SESSION_KEY);
        boolean loggedIn = currentUser != null;
        return ResponseEntity.ok(Map.of(
                "loggedIn", loggedIn,
                "sessionId", session.getId(),
                "loginMethod", session.getAttribute("loginMethod") != null ? session.getAttribute("loginMethod") : "none"
        ));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, Object>> subscribe(HttpSession session) {
        User currentUser = (User) session.getAttribute(USER_SESSION_KEY);

        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", false,
                    "message", "User must be logged in to subscribe."
            ));
        }

        try {
            userService.updateSubscriptionStatus(currentUser.getEmail(), true);
            Optional<User> updatedUserOpt = userService.findUserByEmail(currentUser.getEmail());

            if (updatedUserOpt.isPresent()) {
                User updatedUser = updatedUserOpt.get();
                session.setAttribute(USER_SESSION_KEY, updatedUser);

                // Ensure email is still set in session if updated
                session.setAttribute("email", updatedUser.getEmail());

                return ResponseEntity.ok(Map.of(
                        "status", true,
                        "message", "Successfully subscribed to newsletter.",
                        "user", UserDTO.from(updatedUser)
                ));
            } else {
                return ResponseEntity.internalServerError().body(Map.of(
                        "status", false,
                        "message", "Failed to refresh user data."
                ));
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", false,
                    "message", e.getMessage()
            ));
        }
    }
}