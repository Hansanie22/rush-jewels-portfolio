package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.ProductVariance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVarianceRepository extends JpaRepository<ProductVariance, Integer> {

    // Existing method: Fetches variants for the shop page
    List<ProductVariance> findByProduct_Status_Id(int statusId);

    // New method: Finds all variants for a specific parent product (used when searching by variance ID)
    List<ProductVariance> findByProduct_Id(Integer productId);

    // New method: Text search across product name, category, color, and gemstone.
    @Query("SELECT pv FROM ProductVariance pv JOIN pv.product p " +
            "WHERE p.status.id = 1 AND (" +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.category.category) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(pv.gemstone.gemStone) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(pv.color.color) LIKE LOWER(CONCAT('%', :query, '%'))" +
            ")")
    List<ProductVariance> findActiveByTextSearch(@Param("query") String query);

    int countByProductId(int productId);

    @Query("SELECT pv FROM ProductVariance pv JOIN pv.product p WHERE p.status.id = 1 AND pv.discountPercentage > 0 AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<ProductVariance> searchActiveProducts(@Param("query") String query);

    // ✅ For Smart Marketing Campaigns
    List<ProductVariance> findTop6ByProduct_Status_IdOrderByIdDesc(int statusId);
    List<ProductVariance> findTop6ByProduct_Status_IdOrderByDiscountPercentageDesc(int statusId);
}