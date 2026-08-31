package lk.dio.rush_jewels.controller;

import jakarta.servlet.http.HttpSession;
import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private static final String USER_SESSION_KEY = "user";

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(HttpSession session) {
        User user = (User) session.getAttribute(USER_SESSION_KEY);
        if (user == null)
            return ResponseEntity.status(401).body(Map.of("status", false, "message", "Not logged in"));

        User refreshed = profileService.getUserByEmail(user.getEmail());
        if (refreshed == null)
            return ResponseEntity.badRequest().body(Map.of("status", false, "message", "User not found"));

        String mobile = (refreshed.getMobile() != null) ? refreshed.getMobile() : "";

        Map<String, Object> response = new HashMap<>();
        response.put("status", true);
        response.put("id", refreshed.getId());
        response.put("fname", refreshed.getFname());
        response.put("lname", refreshed.getLname());
        response.put("email", refreshed.getEmail());
        response.put("mobile", mobile);

        // ✅ Cloudinary URL එක කෙලින්ම යවනවා
        response.put("profileImage", refreshed.getImagePath());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateProfile(HttpSession session, @RequestBody Map<String, String> payload) {
        User user = (User) session.getAttribute(USER_SESSION_KEY);
        if (user == null)
            return ResponseEntity.status(401).body(Map.of("status", false, "message", "Not logged in"));

        String fname = payload.get("fname");
        String lname = payload.get("lname");
        String mobile = payload.get("mobile");

        if (fname == null || lname == null || fname.isBlank() || lname.isBlank())
            return ResponseEntity.badRequest().body(Map.of("status", false, "message", "First & Last name required"));

        try {
            profileService.updateProfile(user.getEmail(), fname, lname, mobile);
            return ResponseEntity.ok(Map.of("status", true, "message", "Profile updated successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadProfileImage(HttpSession session, @RequestParam("image") MultipartFile file) {
        User user = (User) session.getAttribute(USER_SESSION_KEY);
        if (user == null)
            return ResponseEntity.status(401).body(Map.of("status", false, "message", "Not logged in"));

        try {
            // ✅ Cloudinary URL එක save කරලා return කරනවා
            String imageUrl = profileService.saveProfileImage(user.getId(), file);

            return ResponseEntity.ok(Map.of(
                    "status", true,
                    "message", "Image uploaded successfully!",
                    "imagePath", imageUrl
            ));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "status", false,
                    "message", "Failed to upload image: " + e.getMessage()
            ));
        }
    }
}