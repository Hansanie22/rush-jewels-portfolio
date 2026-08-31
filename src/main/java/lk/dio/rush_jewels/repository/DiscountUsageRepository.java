package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.DiscountCode;
import lk.dio.rush_jewels.model.DiscountUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountUsageRepository extends JpaRepository<DiscountUsage, Integer> {

    // Count usage by passing the Entity object (Used in DiscountService)
    long countByDiscountCode(DiscountCode discountCode);

    // Count usage by ID (Your specific request)
    long countByDiscountCode_Id(int discountCodeId);

    // Check if a specific user has already used a specific code
    boolean existsByDiscountCodeAndUserId(DiscountCode discountCode, int userId);
}