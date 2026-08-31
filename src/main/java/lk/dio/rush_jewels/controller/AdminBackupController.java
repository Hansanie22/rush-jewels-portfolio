package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.service.DatabaseBackupService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/admin/backups")
public class AdminBackupController {

    private final DatabaseBackupService backupService;

    public AdminBackupController(DatabaseBackupService backupService) {
        this.backupService = backupService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> listBackups() {
        return ResponseEntity.ok(backupService.getAvailableBackups());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createBackup() {
        String fileName = backupService.performBackup();
        if (fileName != null) {
            return ResponseEntity.ok("{\"status\": true, \"message\": \"Backup created successfully\", \"fileName\": \"" + fileName + "\"}");
        } else {
            return ResponseEntity.badRequest().body("{\"status\": false, \"message\": \"Failed to create backup\"}");
        }
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadBackup(@PathVariable String fileName) {
        try {
            Path file = backupService.getBackupPath(fileName);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("application/gzip"))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
