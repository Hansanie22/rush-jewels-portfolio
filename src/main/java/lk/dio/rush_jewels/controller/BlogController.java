package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.service.BlogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class BlogController {

    private final BlogService blogService;

    // Service එක Inject කරගැනීම
    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping("/latest-blogs")
    public ResponseEntity<List<Map<String, Object>>> getLatestBlogs() {
        // කෙලින්ම Service එකෙන් data ඉල්ලීම
        return ResponseEntity.ok(blogService.getLatestBlogs());
    }
}