package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.dto.BlogPostDTO;
import lk.dio.rush_jewels.dto.RelatedItemDTO;
import lk.dio.rush_jewels.model.BlogPost;
import lk.dio.rush_jewels.model.BlogPostProduct;
import lk.dio.rush_jewels.model.Collection;
import lk.dio.rush_jewels.model.ProductVariance;
import lk.dio.rush_jewels.repository.BlogPostProductRepository;
import lk.dio.rush_jewels.repository.BlogPostRepository;
import lk.dio.rush_jewels.repository.BlogPostTagRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BlogPostService {

    private final BlogPostRepository blogPostRepository;
    private final BlogPostTagRepository blogPostTagRepository;
    private final BlogPostProductRepository blogPostProductRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    public BlogPostService(BlogPostRepository blogPostRepository,
                           BlogPostTagRepository blogPostTagRepository,
                           BlogPostProductRepository blogPostProductRepository) {
        this.blogPostRepository = blogPostRepository;
        this.blogPostTagRepository = blogPostTagRepository;
        this.blogPostProductRepository = blogPostProductRepository;
    }

    private String getFullBlogImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return "https://images.unsplash.com/photo-1611085583191-a3b181a88401?auto=format&fit=crop&w=600&q=80";
        }
        // Since we are using Cloudinary, the imagePath IS the full URL.
        return imagePath;
    }

    private BlogPostDTO mapToDto(BlogPost blog, boolean isDetailView) {
        List<String> tags = blogPostTagRepository.findTagNamesByPostId(blog.getId());

        String contentToSend;
        if (isDetailView) {
            contentToSend = blog.getContent();
        } else {
            contentToSend = (blog.getSnippet() != null && !blog.getSnippet().isEmpty())
                    ? blog.getSnippet()
                    : blog.getContent().substring(0, Math.min(blog.getContent().length(), 150)) + "...";
        }

        String readTime = blog.getReadTime();
        if (readTime == null || readTime.isEmpty()) {
            int words = blog.getContent().split("\\s+").length;
            readTime = (words / 200) + " min read";
        }

        BlogPostDTO dto = new BlogPostDTO(
                blog.getId(),
                blog.getTitle(),
                getFullBlogImageUrl(blog.getImagePath()),
                contentToSend,
                blog.getCreatedAt().format(DATE_FORMATTER),
                readTime,
                tags,
                blog.getCategory(),
                new ArrayList<>()
        );

        if (isDetailView) {
            List<BlogPostProduct> relatedEntities = blogPostProductRepository.findByPostId(blog.getId());
            List<RelatedItemDTO> relatedItems = relatedEntities.stream()
                    .map(this::mapRelatedItem)
                    .collect(Collectors.toList());
            dto.setRelatedItems(relatedItems);
        }

        return dto;
    }

    private RelatedItemDTO mapRelatedItem(BlogPostProduct bpp) {
        RelatedItemDTO item = new RelatedItemDTO();

        // --- MAPPING PRODUCT VARIANCE ---
        if (bpp.getProductVariance() != null) {
            ProductVariance pv = bpp.getProductVariance();

            item.setId(pv.getId());
            item.setTitle(pv.getProduct().getTitle());
            item.setName(pv.getProduct().getName());
            item.setPrice(pv.getPrice());
            item.setRegularPrice(pv.getRegularPrice());
            item.setDiscountPercentage(pv.getDiscountPercentage());
            item.setType("PRODUCT");

            // Stock Logic
            item.setCurrentStockQty(pv.getStockLimit());
            item.setStockStatus(pv.getStockLimit() > 0 ? "In Stock" : "Out of Stock");

            // Rating Logic (Placeholder)
            item.setAverageRating(5.0);
            item.setReviewCount(0);

            // ✅ Image: Get directly from Product entity (Cloudinary URL)
            item.setImagePath(pv.getProduct().getImage1());

        }
        // --- MAPPING COLLECTION ---
        else if (bpp.getCollection() != null) {
            Collection c = bpp.getCollection();

            item.setId(c.getId());
            item.setTitle(c.getTitle() != null ? c.getTitle() : c.getName());
            item.setName(c.getName());
            item.setPrice(c.getPrice());
            item.setRegularPrice(c.getRegularPrice());
            item.setDiscountPercentage(c.getDiscountPercentage());
            item.setType("COLLECTION");

            // Stock Logic
            item.setCurrentStockQty(c.getStockLimit());
            item.setStockStatus(c.getStockLimit() > 0 ? "In Stock" : "Out of Stock");

            // Rating Logic
            item.setAverageRating(5.0);
            item.setReviewCount(0);

            // ✅ Image: Get directly from Collection entity (Cloudinary URL)
            item.setImagePath(c.getImage1());
        }

        return item;
    }

    public List<BlogPostDTO> getLatestBlogs() {
        return blogPostRepository.findTop3ByIsPublishedTrueOrderByCreatedAtDesc().stream()
                .map(blog -> mapToDto(blog, false))
                .collect(Collectors.toList());
    }

    public Optional<BlogPostDTO> getBlogDetailById(int id) {
        return blogPostRepository.findById(id)
                .map(blog -> mapToDto(blog, true));
    }
}