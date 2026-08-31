package lk.dio.rush_jewels.controller;

import jakarta.servlet.http.HttpSession;
import lk.dio.rush_jewels.dto.ForgotPasswordRequest;
import lk.dio.rush_jewels.dto.VerifyResetCodeRequest;
import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.repository.UserRepository;
import lk.dio.rush_jewels.service.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final UserRepository userRepository;

    public PasswordResetController(PasswordResetService passwordResetService, UserRepository userRepository) {
        this.passwordResetService = passwordResetService;
        this.userRepository = userRepository;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.sendResetCode(request.getEmail());

            return ResponseEntity.ok(Map.of(
                    "status", true,
                    "message", "A verification code has been sent to your email."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", false,
                    "message", "Failed to send reset code. Please try again later."
            ));
        }
    }

    @PostMapping("/verify-reset-password")
    public ResponseEntity<Map<String, Object>> verifyResetPassword(@RequestBody VerifyResetCodeRequest request, HttpSession session) {
        try {
            boolean isValid = passwordResetService.verifyResetCode(request.getEmail(), request.getCode());

            if (isValid) {
                // Get the user and set up session
                Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    // Set user in session so they can access account.html
                    session.setAttribute("user", user);
                    session.setAttribute("email", request.getEmail());
                }

                return ResponseEntity.ok(Map.of(
                        "status", true,
                        "message", "Verification successful! Redirecting to reset password...",
                        "redirect", "/account.html?tab=password"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", false,
                        "message", "Invalid verification code."
                ));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", false,
                    "message", "Server error. Please try again later."
            ));
        }
    }

    @PostMapping("/resend-reset-code")
    public ResponseEntity<Map<String, Object>> resendResetCode(@RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.sendResetCode(request.getEmail());

            return ResponseEntity.ok(Map.of(
                    "status", true,
                    "message", "Verification code has been resent to your email."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", false,
                    "message", "Failed to resend code. Please try again later."
            ));
        }
    }
}
