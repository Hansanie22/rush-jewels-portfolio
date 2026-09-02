package lk.dio.rush_jewels.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.model.BlogPost;
import lk.dio.rush_jewels.repository.CollectionRepository;
import lk.dio.rush_jewels.repository.ProductVarianceRepository;
import lk.dio.rush_jewels.service.AdminBlogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/blog")
public class AdminBlogController {

    private final AdminBlogService blogService;
    private final ProductVarianceRepository productVarianceRepository;
    private final CollectionRepository collectionRepository;

    // ✅ NOTE: Local Image Base Path removed.
    // Images are now served directly via Cloudinary URLs.

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public AdminBlogController(AdminBlogService blogService,
                               ProductVarianceRepository productVarianceRepository,
                               CollectionRepository collectionRepository) {
        this.blogService = blogService;
        this.productVarianceRepository = productVarianceRepository;
        this.collectionRepository = collectionRepository;
    }

    // -------------------- BLOG CRUD --------------------
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllPosts() {
        List<Map<String, Object>> result = blogService.getAllPosts().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPost(@PathVariable int id) {
        BlogPost post = blogService.getPostById(id);
        Map<String, Object> dto = mapToDto(post);

        dto.put("tags", blogService.getTagsForPost(id));
        dto.put("productVarianceIds", blogService.getProductVarianceIdsForPost(id));
        dto.put("collectionIds", blogService.getCollectionIdsForPost(id));

        return ResponseEntity.ok(dto);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPost(
            @RequestPart("title") String title,
            @RequestPart("slug") String slug,
            @RequestPart("snippet") String snippet,
            @RequestPart("content") String content,
            @RequestPart(value = "category", required = false) String category,
            @RequestPart(value = "readTime", required = false) String readTime,
            @RequestPart(value = "isPublished", required = false) String isPublished,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "tags", required = false) String tags,
            @RequestPart(value = "productVarianceIds", required = false) String productVarianceIds,
            @RequestPart(value = "collectionIds", required = false) String collectionIds) {
        try {
            List<String> tagList = parseStringList(tags);
            List<Integer> productIds = parseIntegerList(productVarianceIds);
            List<Integer> collIds = parseIntegerList(collectionIds);
            boolean published = isPublished != null ? Boolean.parseBoolean(isPublished) : true;

            blogService.savePost(title, slug, snippet, content, category, readTime,
                    published, image, tagList, productIds, collIds);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/{id}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePost(
            @PathVariable int id,
            @RequestPart("title") String title,
            @RequestPart("slug") String slug,
            @RequestPart("snippet") String snippet,
            @RequestPart("content") String content,
            @RequestPart(value = "category", required = false) String category,
            @RequestPart(value = "readTime", required = false) String readTime,
            @RequestPart(value = "isPublished", required = false) String isPublished,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "tags", required = false) String tags,
            @RequestPart(value = "productVarianceIds", required = false) String productVarianceIds,
            @RequestPart(value = "collectionIds", required = false) String collectionIds) {
        try {
            List<String> tagList = parseStringList(tags);
            List<Integer> productIds = parseIntegerList(productVarianceIds);
            List<Integer> collIds = parseIntegerList(collectionIds);
            boolean published = isPublished != null ? Boolean.parseBoolean(isPublished) : true;

            blogService.updatePost(id, title, slug, snippet, content, category, readTime,
                    published, image, tagList, productIds, collIds);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<?> deletePost(@PathVariable int id) {
        try {
            blogService.deletePost(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // -------------------- PRODUCTS & COLLECTIONS --------------------
    @GetMapping("/product-variances")
    public ResponseEntity<List<Map<String, Object>>> getProductVariances() {
        List<Map<String, Object>> variances = productVarianceRepository.findAll().stream()
                .filter(v -> v.getStatus() != null && "Active".equalsIgnoreCase(v.getStatus().getStatus()))
                .map(v -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", v.getId());

                    StringBuilder name = new StringBuilder(v.getProduct().getName());
                    if (v.getSize() != null) name.append(" - ").append(v.getSize().getSize());
                    if (v.getColor() != null) name.append(" - ").append(v.getColor().getColor());
                    if (v.getGemstone() != null) name.append(" - ").append(v.getGemstone().getGemStone());

                    String fullName = name.toString();
                    map.put("name", fullName);
                    map.put("slug", generateSlug(fullName));
                    map.put("type", "product");
                    return map;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(variances);
    }

    @GetMapping("/collections")
    public ResponseEntity<List<Map<String, Object>>> getCollections() {
        List<Map<String, Object>> collections = collectionRepository.findAll().stream()
                .filter(c -> c.getStatus() != null && "Active".equalsIgnoreCase(c.getStatus().getStatus()))
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());
                    map.put("name", c.getName());
                    map.put("slug", generateSlug(c.getName()));
                    map.put("type", "collection");
                    return map;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(collections);
    }

    // Utility method to generate slug from name
    private String generateSlug(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    // -------------------- GEMINI TAG GENERATION --------------------
    public static class TagRequest { public String content; }
    public static class TagResponse { public List<String> tags; }

    @PostMapping("/generate-tags")
    public ResponseEntity<?> generateTags(@RequestBody TagRequest request) {
        if (request.content == null || request.content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Content cannot be empty");
        }
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            return ResponseEntity.status(500).body("Gemini API key not configured");
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper mapper = new ObjectMapper();

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-09-2025:generateContent?key=" + geminiApiKey;

            Map<String, Object> payload = new HashMap<>();
            payload.put("contents", List.of(Map.of("parts", List.of(Map.of("text",
                    "Analyze the following blog content and generate 5-8 short, relevant tags. Respond only with JSON array:\n\n" + request.content
            )))));
            payload.put("systemInstruction", Map.of("parts", List.of(Map.of("text",
                    "You are an expert tag generator for a high-end jewelry and gemology blog. Tags must be 1-3 words, keyword-rich."
            ))));
            payload.put("generationConfig", Map.of(
                    "responseMimeType", "application/json",
                    "responseSchema", Map.of("type", "ARRAY", "items", Map.of("type", "STRING"))
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(payload), headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);

            JsonNode candidates = response.getBody().get("candidates");
            if (candidates == null || !candidates.isArray() || candidates.size() == 0) {
                return ResponseEntity.status(500).body("No tags returned from Gemini API");
            }

            JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
            List<String> tags = mapper.readValue(textNode.asText(), new TypeReference<List<String>>() {});

            TagResponse resp = new TagResponse();
            resp.tags = tags;
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to generate tags: " + e.getMessage());
        }
    }

    // -------------------- UTIL --------------------
    private Map<String, Object> mapToDto(BlogPost p) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("title", p.getTitle());
        map.put("slug", p.getSlug());
        map.put("snippet", p.getSnippet());
        map.put("content", p.getContent());
        map.put("category", p.getCategory());
        map.put("readTime", p.getReadTime());
        map.put("isPublished", p.getIsPublished() != null ? p.getIsPublished() : true);
        map.put("date", p.getCreatedAt());

        // ✅ CHANGE: Use Cloudinary URL directly
        // Old: map.put("image", p.getImagePath() != null ? "/uploads" + blogImageBasePath + "/" + p.getImagePath() : null);
        map.put("image", p.getImagePath());

        return map;
    }

    private List<String> parseStringList(String data) {
        if (data == null || data.trim().isEmpty()) return new ArrayList<>();
        return Arrays.stream(data.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    private List<Integer> parseIntegerList(String data) {
        if (data == null || data.trim().isEmpty()) return new ArrayList<>();
        return Arrays.stream(data.split(",")).map(String::trim).filter(s -> !s.isEmpty()).map(Integer::parseInt).collect(Collectors.toList());
    }
}