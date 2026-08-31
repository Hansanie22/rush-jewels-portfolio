package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.model.BlogPost;
import lk.dio.rush_jewels.repository.BlogPostRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BlogService {

    private final BlogPostRepository blogRepository;

    // ✅ NOTE: Removed @Value because we now use Cloudinary URLs directly from the DB.
    // පරණ Local Path එක ගන්න එක අයින් කළා.

    public BlogService(BlogPostRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    public List<Map<String, Object>> getLatestBlogs() {
        // අලුත්ම පෝස්ට් 3 ගන්නවා
        List<BlogPost> posts = blogRepository.findTop3ByOrderByCreatedAtDesc();

        return posts.stream().map(post -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", post.getId());
            map.put("title", post.getTitle());

            // Snippet සැකසීම (මුල් අකුරු 100)
            String content = post.getContent();
            String snippet = (content != null && content.length() > 100)
                    ? content.substring(0, 100) + "..."
                    : content;
            map.put("snippet", snippet);

            // කියවීමට ගතවන කාලය (Read Time)
            int wordCount = (content != null) ? content.split("\\s+").length : 0;
            int readTime = Math.max(1, wordCount / 200);
            map.put("readTime", readTime + " min read");

            // --- Image Path Update for Cloudinary ---
            // ✅ OLD: String fullPath = "/uploads" + blogImageBasePath + "/" + post.getImagePath();
            // ✅ NEW: කෙලින්ම Database එකේ තියෙන Cloudinary URL එක ගන්නවා
            map.put("imagePath", post.getImagePath());

            // දිනය Format කිරීම
            if (post.getCreatedAt() != null) {
                map.put("date", post.getCreatedAt().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
            } else {
                map.put("date", "");
            }

            return map;
        }).collect(Collectors.toList());
    }
}