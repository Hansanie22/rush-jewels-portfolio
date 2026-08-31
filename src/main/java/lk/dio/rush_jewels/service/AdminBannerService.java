package lk.dio.rush_jewels.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.model.AdminAuditLog;
import lk.dio.rush_jewels.model.Banner;
import lk.dio.rush_jewels.repository.AdminAuditLogRepository;
import lk.dio.rush_jewels.repository.BannerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminBannerService {

    private final BannerRepository bannerRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final CloudinaryService cloudinaryService; // ✅ Cloudinary Service එක සම්බන්ධ කළා

    public AdminBannerService(BannerRepository bannerRepository,
                              AdminAuditLogRepository auditLogRepository,
                              ObjectMapper objectMapper,
                              CloudinaryService cloudinaryService) {
        this.bannerRepository = bannerRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
        this.cloudinaryService = cloudinaryService;
    }

    public List<Banner> getAllBanners() {
        return bannerRepository.findAll();
    }

    public Banner saveBanner(MultipartFile file) throws IOException {
        // 1. වීඩියෝ එකක්ද ෆොටෝ එකක්ද කියලා බලනවා
        String contentType = file.getContentType();
        String type = "IMAGE";
        if (contentType != null && contentType.toLowerCase().startsWith("video")) {
            type = "VIDEO";
        }

        // 2. Cloudinary එකට Upload කරනවා (හාඩ් ඩිස්ක් එකට සේව් වෙන්නේ නෑ)
        String mediaUrl = cloudinaryService.uploadImage(file);

        // 3. Database එකට Link එක දානවා
        Banner banner = new Banner(mediaUrl, type);
        Banner savedBanner = bannerRepository.save(banner);

        // 4. Audit Log එකක් තියනවා
        logAction("CREATE", "banner", String.valueOf(savedBanner.getId()), null, savedBanner);

        return savedBanner;
    }

    public void deleteBanner(int id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found"));

        String oldValue = convertToJson(banner);

        // Local Files delete කරන කෑලි දැන් ඕන නෑ. කෙලින්ම DB එකෙන් අයින් කරනවා.
        bannerRepository.delete(banner);

        logAction("DELETE", "banner", String.valueOf(id), oldValue, "Deleted Banner: " + banner.getMediaPath());
    }

    // ==========================================
    // AUDIT LOG HELPER METHODS
    // ==========================================

    private void logAction(String action, String table, String recordId, String oldValue, Object newValueObj) {
        try {
            String newValue;

            if (newValueObj instanceof String) {
                newValue = objectMapper.writeValueAsString(newValueObj);
            } else {
                newValue = convertToJson(newValueObj);
            }

            AdminAuditLog log = new AdminAuditLog(
                    action,
                    table,
                    recordId,
                    oldValue != null ? oldValue : "{}",
                    newValue != null ? newValue : "{}",
                    LocalDateTime.now()
            );
            auditLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Audit Log Failed: " + e.getMessage());
        }
    }

    private String convertToJson(Object object) {
        try {
            Object sanitized = sanitizeForAudit(object);
            if (sanitized == null) return "{}";
            return objectMapper.writeValueAsString(sanitized);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private Object sanitizeForAudit(Object obj) {
        if (obj instanceof Banner) {
            Banner b = (Banner) obj;
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("path", b.getMediaPath());
            map.put("type", b.getMediaType());
            return map;
        }
        return obj;
    }
}