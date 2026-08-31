package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/banners")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getBanners() {
        // Frontend එකට ගැලපෙන විදිහට Data Map කරනවා
        List<Map<String, Object>> result = bannerService.getAllBanners().stream().map(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());

            // Cloudinary URL එක 'url' නමින් යවනවා
            map.put("url", b.getMediaPath());

            map.put("type", b.getMediaType());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}