package lk.dio.rush_jewels.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.model.*;
import lk.dio.rush_jewels.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminBlogService {

    private final BlogPostRepository blogRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final TagRepository tagRepository;
    private final BlogPostTagRepository blogPostTagRepository;
    private final BlogPostProductRepository blogPostProductRepository;
    private final ProductVarianceRepository productVarianceRepository;
    private final CollectionRepository collectionRepository;
    private final ObjectMapper objectMapper;
    private final CloudinaryService cloudinaryService; // ✅ Added Cloudinary Service

    public AdminBlogService(BlogPostRepository blogRepository,
                            AdminAuditLogRepository auditLogRepository,
                            TagRepository tagRepository,
                            BlogPostTagRepository blogPostTagRepository,
                            BlogPostProductRepository blogPostProductRepository,
                            ProductVarianceRepository productVarianceRepository,
                            CollectionRepository collectionRepository,
                            ObjectMapper objectMapper,
                            CloudinaryService cloudinaryService) {
        this.blogRepository = blogRepository;
        this.auditLogRepository = auditLogRepository;
        this.tagRepository = tagRepository;
        this.blogPostTagRepository = blogPostTagRepository;
        this.blogPostProductRepository = blogPostProductRepository;
        this.productVarianceRepository = productVarianceRepository;
        this.collectionRepository = collectionRepository;
        this.objectMapper = objectMapper;
        this.cloudinaryService = cloudinaryService;
    }

    // ==============================
    // BLOG OPERATIONS
    // ==============================
    public List<BlogPost> getAllPosts() {
        return blogRepository.findAllByOrderByCreatedAtDesc();
    }

    public BlogPost getPostById(int id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    public BlogPost savePost(String title, String slug, String snippet, String content,
                             String category, String readTime, boolean isPublished,
                             MultipartFile image, List<String> tagNames,
                             List<Integer> productVarianceIds, List<Integer> collectionIds) throws IOException {
        BlogPost post = new BlogPost();
        post.setTitle(title);
        post.setSlug(slug);
        post.setSnippet(snippet);
        post.setContent(content);
        post.setCategory(category);
        post.setReadTime(readTime);
        post.setIsPublished(isPublished);

        // ✅ Handle image with Cloudinary
        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(image);
            post.setImagePath(imageUrl);
        }

        BlogPost savedPost = blogRepository.save(post);

        // Handle tags
        if (tagNames != null && !tagNames.isEmpty()) {
            saveTags(savedPost, tagNames);
        }

        // Handle product and collection associations
        if (productVarianceIds != null && !productVarianceIds.isEmpty()) {
            saveProductAssociations(savedPost, productVarianceIds, null);
        }
        if (collectionIds != null && !collectionIds.isEmpty()) {
            saveCollectionAssociations(savedPost, collectionIds);
        }

        // Audit log for CREATE
        logAction("CREATE", "blog_post", savedPost.getId(), null, savedPost);

        return savedPost;
    }

    public BlogPost updatePost(int id, String title, String slug, String snippet, String content,
                               String category, String readTime, boolean isPublished,
                               MultipartFile image, List<String> tagNames,
                               List<Integer> productVarianceIds, List<Integer> collectionIds) throws IOException {
        BlogPost post = getPostById(id);

        // Capture old value for audit
        String oldValue = convertToJson(post);

        post.setTitle(title);
        post.setSlug(slug);
        post.setSnippet(snippet);
        post.setContent(content);
        post.setCategory(category);
        post.setReadTime(readTime);
        post.setIsPublished(isPublished);

        // ✅ Handle image update with Cloudinary
        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(image);
            post.setImagePath(imageUrl);
        }

        BlogPost savedPost = blogRepository.save(post);

        // Update tags - delete old ones and add new ones
        blogPostTagRepository.deleteByPost(savedPost);
        if (tagNames != null && !tagNames.isEmpty()) {
            saveTags(savedPost, tagNames);
        }

        // Update product and collection associations
        blogPostProductRepository.deleteByPost(savedPost);
        if (productVarianceIds != null && !productVarianceIds.isEmpty()) {
            saveProductAssociations(savedPost, productVarianceIds, null);
        }
        if (collectionIds != null && !collectionIds.isEmpty()) {
            saveCollectionAssociations(savedPost, collectionIds);
        }

        // Audit log for UPDATE
        logAction("UPDATE", "blog_post", savedPost.getId(), oldValue, savedPost);

        return savedPost;
    }

    public void deletePost(int id) {
        BlogPost post = getPostById(id);

        String oldValue = convertToJson(post);

        // Delete associations first
        blogPostTagRepository.deleteByPost(post);
        blogPostProductRepository.deleteByPost(post);

        // Local file deletion logic removed (Cloudinary URL just stays or deleted via API if needed)
        blogRepository.delete(post);

        // Audit log for DELETE
        Map<String, Object> deleteInfo = new HashMap<>();
        deleteInfo.put("message", "Deleted Post");
        deleteInfo.put("title", post.getTitle());

        logAction("DELETE", "blog_post", post.getId(), oldValue, deleteInfo);
    }

    // ==============================
    // TAG OPERATIONS
    // ==============================
    private void saveTags(BlogPost post, List<String> tagNames) {
        for (String tagName : tagNames) {
            if (tagName == null || tagName.trim().isEmpty()) continue;

            // Find or create tag
            Tag tag = tagRepository.findByName(tagName.trim())
                    .orElseGet(() -> {
                        Tag newTag = new Tag(tagName.trim());
                        return tagRepository.save(newTag);
                    });

            // Create blog-tag association
            BlogPostTag blogPostTag = new BlogPostTag(post, tag);
            blogPostTagRepository.save(blogPostTag);
        }
    }

    public List<String> getTagsForPost(int postId) {
        BlogPost post = getPostById(postId);
        return blogPostTagRepository.findByPost(post).stream()
                .map(bpt -> bpt.getTag().getName())
                .collect(Collectors.toList());
    }

    // ==============================
    // PRODUCT ASSOCIATION OPERATIONS
    // ==============================
    private void saveProductAssociations(BlogPost post, List<Integer> productVarianceIds, List<Integer> collectionIds) {
        for (Integer varianceId : productVarianceIds) {
            if (varianceId == null) continue;

            productVarianceRepository.findById(varianceId).ifPresent(variance -> {
                BlogPostProduct blogPostProduct = new BlogPostProduct();
                blogPostProduct.setPost(post);
                blogPostProduct.setProductVariance(variance);
                blogPostProductRepository.save(blogPostProduct);
            });
        }
    }

    private void saveCollectionAssociations(BlogPost post, List<Integer> collectionIds) {
        for (Integer collectionId : collectionIds) {
            if (collectionId == null) continue;

            collectionRepository.findById(collectionId).ifPresent(collection -> {
                BlogPostProduct blogPostProduct = new BlogPostProduct();
                blogPostProduct.setPost(post);
                blogPostProduct.setCollection(collection);
                blogPostProductRepository.save(blogPostProduct);
            });
        }
    }

    public List<Integer> getProductVarianceIdsForPost(int postId) {
        BlogPost post = getPostById(postId);
        return blogPostProductRepository.findByPost(post).stream()
                .filter(bpp -> bpp.getProductVariance() != null)
                .map(bpp -> bpp.getProductVariance().getId())
                .collect(Collectors.toList());
    }

    public List<Integer> getCollectionIdsForPost(int postId) {
        BlogPost post = getPostById(postId);
        return blogPostProductRepository.findByPost(post).stream()
                .filter(bpp -> bpp.getCollection() != null)
                .map(bpp -> bpp.getCollection().getId())
                .collect(Collectors.toList());
    }

    // ==============================
    // AUDIT LOGGING
    // ==============================
    private void logAction(String action, String table, int recordId, String oldValue, Object newValueObj) {
        try {
            String newValue = convertObjectToJsonSafe(newValueObj);
            String oldVal = oldValue != null ? oldValue : "null";

            AdminAuditLog log = new AdminAuditLog(
                    action,
                    table,
                    String.valueOf(recordId),
                    oldVal,
                    newValue,
                    LocalDateTime.now()
            );
            auditLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Audit Log Failed: " + e.getMessage());
        }
    }

    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(sanitizeForAudit(object));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String convertObjectToJsonSafe(Object object) {
        try {
            if (object == null) return "null";
            if (object instanceof String) return objectMapper.writeValueAsString(object);
            return objectMapper.writeValueAsString(sanitizeForAudit(object));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private Object sanitizeForAudit(Object obj) {
        if (obj instanceof BlogPost) {
            BlogPost p = (BlogPost) obj;
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("title", p.getTitle());
            map.put("slug", p.getSlug());
            map.put("snippet", p.getSnippet());
            map.put("content", p.getContent());
            map.put("category", p.getCategory());
            map.put("readTime", p.getReadTime());
            map.put("isPublished", p.getIsPublished());
            map.put("image", p.getImagePath());
            map.put("createdAt", p.getCreatedAt());
            return map;
        }
        return obj;
    }
}