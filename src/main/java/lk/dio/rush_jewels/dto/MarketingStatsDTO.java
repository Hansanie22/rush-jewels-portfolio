package lk.dio.rush_jewels.dto;

public class MarketingStatsDTO {
    private double subscriberRate;   // Replaces "Open Rate" (since we don't track email opens yet)
    private double attributedRevenue; // Revenue from orders with coupons
    private double couponUsageRate;   // % of orders using coupons

    public MarketingStatsDTO(double subscriberRate, double attributedRevenue, double couponUsageRate) {
        this.subscriberRate = subscriberRate;
        this.attributedRevenue = attributedRevenue;
        this.couponUsageRate = couponUsageRate;
    }

    // Getters
    public double getSubscriberRate() { return subscriberRate; }
    public double getAttributedRevenue() { return attributedRevenue; }
    public double getCouponUsageRate() { return couponUsageRate; }
}