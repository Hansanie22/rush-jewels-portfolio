package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.dto.ReviewsDTO;
import lk.dio.rush_jewels.model.Review;
import lk.dio.rush_jewels.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;


    public List<ReviewsDTO> getTopTestimonials() {
        Pageable limitTwo = PageRequest.of(0, 2);
        Pageable limitOne = PageRequest.of(0, 1);

        List<Review> adminReviews = reviewRepository.findApprovedAdminReviews(limitTwo);
        List<Review> userReviews = reviewRepository.findApprovedUserReviews(limitOne);

        List<Review> combinedReviews = new ArrayList<>();
        combinedReviews.addAll(adminReviews);
        combinedReviews.addAll(userReviews);

        return combinedReviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ReviewsDTO convertToDTO(Review review) {
        String reviewerName;
        String reviewerType;
        String profileImagePath = null; // Default null (Frontend එකෙන් Avatar පෙන්නන්න)

        // --- 1. නම, වර්ගය සහ පින්තූරය තීරණය කිරීම ---
        if (review.getAdmin() != null) {
            reviewerType = "Admin";
            reviewerName = review.getAdmin().getFname() + " " + review.getAdmin().getLname();
            // Admin ටත් imagePath field එකක් Entity එකේ තියෙනවා නම් මෙතන දාන්න:
            // profileImagePath = review.getAdmin().getImagePath();
        } else if (review.getUser() != null) {
            reviewerType = "Customer";
            reviewerName = review.getUser().getFname() + " " + review.getUser().getLname();

            // ✅ CHANGE: Cloudinary URL එක කෙලින්ම User Entity එකෙන් ගන්නවා
            // (Files.exists කියලා disk එක check කරන්න දැන් ඕන නෑ)
            profileImagePath = review.getUser().getImagePath();

        } else {
            reviewerName = "Anonymous";
            reviewerType = "Customer";
        }

        return new ReviewsDTO(
                review.getId(),
                review.getRating(),
                review.getComment(),
                reviewerName,
                reviewerType,
                profileImagePath, // URL එක හෝ null
                review.getApprovedAt()
        );
    }
}