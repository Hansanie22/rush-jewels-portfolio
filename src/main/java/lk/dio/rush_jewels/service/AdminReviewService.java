package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.dto.ReviewDTO;
import lk.dio.rush_jewels.model.*;
import lk.dio.rush_jewels.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewStatusRepository statusRepository;
    private final ProductVarianceRepository varianceRepository;
    private final UserRepository userRepository; // ✅ Matches Entity (private User admin)

    public AdminReviewService(ReviewRepository reviewRepository,
                              ReviewStatusRepository statusRepository,
                              ProductVarianceRepository varianceRepository,
                              UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.statusRepository = statusRepository;
        this.varianceRepository = varianceRepository;
        this.userRepository = userRepository;
    }

    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Review saveReview(ReviewDTO dto) {
        Review review = new Review();

        // 1. Link Product Variance
        if (dto.getVarianceId() != null && dto.getVarianceId() > 0) {
            ProductVariance variance = varianceRepository.findById(dto.getVarianceId())
                    .orElseThrow(() -> new RuntimeException("Product Variance not found"));
            review.setProductVariance(variance);
        } else {
            review.setProductVariance(null);
        }

        // 2. Link Admin (Entity uses User class for admin field)
        if (dto.getAdminId() != null && dto.getAdminId() > 0) {
            User adminUser = userRepository.findById(dto.getAdminId())
                    .orElseThrow(() -> new RuntimeException("User/Admin not found"));
            review.setAdmin(adminUser);
        }

        // 3. Set Status
        int statusId = dto.getStatusId() > 0 ? dto.getStatusId() : 2;
        ReviewStatus status = statusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Status not found"));
        review.setStatus(status);

        if (statusId == 2) {
            review.setApprovedAt(OffsetDateTime.now());
        }

        // 4. Map Details (Clean Comment Logic)
        review.setRating(dto.getRating());

        // Construct Comment nicely
        String mainComment = dto.getComment();
        if (mainComment != null && !mainComment.trim().isEmpty()) {
            StringBuilder sb = new StringBuilder(mainComment);

            // Only append footer if customer details are actually provided manually
            if (dto.getCustomerName() != null && !dto.getCustomerName().trim().isEmpty()
                    && !dto.getCustomerName().equals("Admin Manual")) {

                sb.append("\n\n[Manual Review by: ").append(dto.getCustomerName());
                if (dto.getCustomerEmail() != null && !dto.getCustomerEmail().isEmpty()) {
                    sb.append(" (").append(dto.getCustomerEmail()).append(")");
                }
                sb.append("]");
            }
            review.setComment(sb.toString());
        } else {
            review.setComment(null); // Explicitly null if empty
        }

        return reviewRepository.save(review);
    }

    public void updateReviewStatus(int reviewId, int statusId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        ReviewStatus status = statusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Status not found"));

        review.setStatus(status);
        if (statusId == 2) review.setApprovedAt(OffsetDateTime.now());

        reviewRepository.save(review);
    }

    private ReviewDTO convertToDTO(Review r) {
        String varianceInfo = "General Review";
        Integer varId = null;

        if (r.getProductVariance() != null) {
            varianceInfo = r.getProductVariance().getProduct().getName();
            varId = r.getProductVariance().getId();
        }

        // Determine Author Display Name
        String custName = "Guest";
        String custEmail = "-";

        if (r.getUser() != null) {
            custName = r.getUser().getFname();
            custEmail = r.getUser().getEmail();
        } else if (r.getAdmin() != null) {
            custName = "Manual (Admin: " + r.getAdmin().getFname() + ")";
        }

        Integer adminId = (r.getAdmin() != null) ? r.getAdmin().getId() : null;

        return new ReviewDTO(
                r.getId(),
                varId,
                varianceInfo,
                custName,
                custEmail,
                r.getRating(),
                "", // Title not stored in entity
                r.getComment(),
                r.getStatus().getId(),
                r.getStatus().getReviewStatus(),
                r.getCreatedAt(),
                adminId
        );
    }
}