package lk.dio.rush_jewels.controller;

import jakarta.servlet.http.HttpSession;
import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    // Constructor injection is a best practice
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Updates the subscription status for the currently authenticated user.
     * Expects a JSON payload like: {"subscribed": true}
     */
    @PostMapping("/subscribe/update")
    public ResponseEntity<?> updateSubscription(
            @RequestBody Map<String, Boolean> payload,
            HttpSession session // <-- This is the fix: We use HttpSession
    ) {
        // 1. Get the User object from the session
        // "user" is the key you used in LoginController
        User sessionUser = (User) session.getAttribute("user");

        // 2. Check if user is authenticated (i.e., if the object exists)
        if (sessionUser == null || sessionUser.getEmail() == null) {
            // This is the 401 Unauthorized error you were seeing
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "User not authenticated. Please log in again."));
        }

        try {
            // 3. Get the new status from the request body
            Boolean isSubscribed = payload.get("subscribed");
            if (isSubscribed == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing 'subscribed' field in request body"));
            }

            String email = sessionUser.getEmail();

            // 4. Call the service to update the database
            userService.updateSubscriptionStatus(email, isSubscribed);

            // 5. IMPORTANT: We must also update the user object in the session
            // so that if the page is refreshed, it remembers the new status.
            sessionUser.setSubscribed(isSubscribed);
            session.setAttribute("user", sessionUser);

            // 6. Send a success response
            String message = isSubscribed ? "Successfully subscribed to news and offers!" : "Successfully unsubscribed.";
            return ResponseEntity.ok(Map.of("success", true, "message", message));

        } catch (RuntimeException e) {
            // Handle cases like "User not found" from the service
            return ResponseEntity.status(404).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            // Catch-all for other errors
            e.printStackTrace(); // Good for debugging
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "An unexpected error occurred"));
        }
    }
}