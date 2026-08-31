package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.BlogPostDTO;
import lk.dio.rush_jewels.service.BlogPostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/post")
public class BlogPostController {

    private final BlogPostService blogService;

    public BlogPostController(BlogPostService blogService) {
        this.blogService = blogService;
    }

    // List of Blogs
    @GetMapping("/latest")
    public ResponseEntity<List<BlogPostDTO>> getLatestBlogs() {
        try {
            return ResponseEntity.ok(blogService.getLatestBlogs());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // Single Blog Detail
    @GetMapping("/details")
    public ResponseEntity<?> getBlogDetails(@RequestParam int id) {
        try {
            return blogService.getBlogDetailById(id)
                    .map(ResponseEntity::ok)
                    // වැදගත්ම කොටස: ID එක නැති වුනොත් HTML එවන්නේ නැතුව 404 Status එක යවනවා
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            e.printStackTrace();
            // Server Error එකක් ආවොත් JSON Message එකක් යවනවා
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Server Error: " + e.getMessage()));
        }
    }
}