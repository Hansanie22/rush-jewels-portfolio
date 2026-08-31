package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.service.AdminBannerService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/banners")
public class AdminBannerController {

    private final AdminBannerService bannerService;


    public AdminBannerController(AdminBannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllBanners() {
        List<Map<String, Object>> result = bannerService.getAllBanners().stream().map(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());

            // ✅ CHANGE: Use the Cloudinary URL directly from mediaPath
            // Old Code: map.put("url", "/uploads" + bannerBasePath + "/" + b.getMediaPath());
            map.put("url", b.getMediaPath());

            map.put("type", b.getMediaType());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadBanner(@RequestPart("file") MultipartFile file) {
        try {
            bannerService.saveBanner(file);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<?> deleteBanner(@PathVariable Integer id) {
        if (id == null) return ResponseEntity.badRequest().body("Invalid banner ID");

        try {
            bannerService.deleteBanner(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}