package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Collection;
import lk.dio.rush_jewels.model.ProductVariance;
import lk.dio.rush_jewels.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByProductVariance(ProductVariance pv);
    List<Review> findByCollection(Collection c);

    @Query("SELECT r FROM Review r WHERE r.status.id = 2 AND r.admin IS NOT NULL ORDER BY r.approvedAt DESC")
    List<Review> findApprovedAdminReviews(org.springframework.data.domain.Pageable pageable);

    // 2. Approved Reviews by User (Customer Reviews)
    @Query("SELECT r FROM Review r WHERE r.status.id = 2 AND r.user IS NOT NULL AND r.admin IS NULL ORDER BY r.approvedAt DESC")
    List<Review> findApprovedUserReviews(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productVariance.product.id = :productId AND r.status.reviewStatus = 'Approved'")
    Double getAverageRatingByProductId(@Param("productId") int productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productVariance.product.id = :productId AND r.status.reviewStatus = 'Approved'")
    int getReviewCountByProductId(@Param("productId") int productId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productVariance.id = :varianceId AND r.status.reviewStatus = 'Approved'")
    Double getAverageRatingByVarianceId(@Param("varianceId") int varianceId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productVariance.id = :varianceId AND r.status.reviewStatus = 'Approved'")
    int getReviewCountByVarianceId(@Param("varianceId") int varianceId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.collection.id = :collectionId AND r.status.reviewStatus = 'Approved'")
    Double getAverageRatingByCollectionId(@Param("collectionId") int collectionId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.collection.id = :collectionId AND r.status.reviewStatus = 'Approved'")
    int getReviewCountByCollectionId(@Param("collectionId") int collectionId);

}