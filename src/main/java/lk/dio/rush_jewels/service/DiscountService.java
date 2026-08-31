package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.model.DiscountCode;
import lk.dio.rush_jewels.model.DiscountUsage;
import lk.dio.rush_jewels.repository.DiscountCodeRepository;
import lk.dio.rush_jewels.repository.DiscountUsageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
public class DiscountService {

    private final DiscountCodeRepository discountCodeRepo;
    private final DiscountUsageRepository discountUsageRepo;

    public DiscountService(DiscountCodeRepository discountCodeRepo, DiscountUsageRepository discountUsageRepo) {
        this.discountCodeRepo = discountCodeRepo;
        this.discountUsageRepo = discountUsageRepo;
    }

    /**
     * Validates and calculates discount amount based on subtotal.
     * @return The calculated discount amount (0.0 if invalid)
     */
    public double calculateDiscount(String code, double subtotal) {
        if (code == null || code.trim().isEmpty()) {
            return 0.0;
        }

        Optional<DiscountCode> optCode = discountCodeRepo.findByCode(code.trim());
        if (optCode.isEmpty()) {
            throw new IllegalArgumentException("Invalid Coupon Code");
        }

        DiscountCode dc = optCode.get();

        // 1. Check Active Status
        if (!dc.isActive()) {
            throw new IllegalArgumentException("This coupon is inactive.");
        }

        // 2. Check Expiration
        if (new Date().after(dc.getExpirationDate())) {
            throw new IllegalArgumentException("This coupon has expired.");
        }

        // 3. Check Usage Limit
        long currentUsage = discountUsageRepo.countByDiscountCode(dc);
        if (dc.getUsageLimit() > 0 && currentUsage >= dc.getUsageLimit()) {
            throw new IllegalArgumentException("This coupon has reached its usage limit.");
        }

        // 4. Calculate Value (ALWAYS AS PERCENTAGE)
        String valueStr = dc.getValue();
        double discountAmount = 0.0;

        try {
            // Logic: Treat database value (e.g. "20") strictly as a percentage (20%)
            double percentage = Double.parseDouble(valueStr.trim());
            discountAmount = subtotal * (percentage / 100.0);

        } catch (NumberFormatException e) {
            throw new IllegalStateException("Error parsing discount value from database: " + valueStr);
        }

        // Ensure discount doesn't exceed subtotal
        return Math.min(discountAmount, subtotal);
    }

    /**
     * Records that a discount code was successfully used in an order.
     */
    @Transactional
    public void recordUsage(String code, int userId, String orderId) {
        if (code == null || code.trim().isEmpty()) return;

        Optional<DiscountCode> optCode = discountCodeRepo.findByCode(code.trim());
        if (optCode.isPresent()) {
            DiscountCode dc = optCode.get();

            DiscountUsage usage = new DiscountUsage();
            usage.setDiscountCode(dc);
            usage.setUserId(userId);
            usage.setOrdersId(orderId);
            usage.setUsedAt(new Date());

            discountUsageRepo.save(usage);
        }
    }
}