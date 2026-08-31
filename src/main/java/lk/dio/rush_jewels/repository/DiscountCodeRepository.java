package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.DiscountCode;
import lk.dio.rush_jewels.model.DiscountUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Integer> {

    Optional<DiscountCode> findByCode(String code);

    List<DiscountCode> findAllByIsActiveTrueOrderByCreatedAtDesc();

    boolean existsByCodeAndIdNot(String code, int id); // For unique check

    boolean existsByCode(String code);

    @Repository
    public interface DiscountUsageRepository extends JpaRepository<DiscountUsage, Integer> {
        // Count how many times a specific code has been used
        @Query("SELECT COUNT(u) FROM DiscountUsage u WHERE u.discountCode = :code")
        long countByDiscountCode(DiscountCode code);

        // Optional: Check if a specific user has already used this code (if you want 1 use per user)
        boolean existsByDiscountCodeAndUserId(DiscountCode discountCode, int userId);
    }
}