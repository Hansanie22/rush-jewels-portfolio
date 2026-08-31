package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.service.CloudinaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private final CloudinaryService cloudinaryService;

    public FileUploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("/slip")
    public ResponseEntity<?> uploadBankSlip(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("status", false, "message", "No file provided."));
            }
            String url = cloudinaryService.uploadImage(file);
            return ResponseEntity.ok(Map.of("status", true, "url", url));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("status", false, "message", "File upload failed: " + e.getMessage()));
        }
    }
}
