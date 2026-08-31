package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.dto.ChartDataDTO;
import lk.dio.rush_jewels.dto.MarketingStatsDTO;
import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.repository.DiscountUsageRepository;
import lk.dio.rush_jewels.repository.OrdersRepository;
import lk.dio.rush_jewels.repository.PaymentRepository;
import lk.dio.rush_jewels.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminMarketingService {

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final OrdersRepository ordersRepository;
    private final DiscountUsageRepository discountUsageRepository;

    public AdminMarketingService(UserRepository userRepository, PaymentRepository paymentRepository, OrdersRepository ordersRepository, DiscountUsageRepository discountUsageRepository) {
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.ordersRepository = ordersRepository;
        this.discountUsageRepository = discountUsageRepository;
    }

    public MarketingStatsDTO getMarketingStats() {
        long totalUsers = userRepository.count();
        long subscribedUsers = userRepository.findAll().stream().filter(User::isSubscribed).count();
        double subRate = totalUsers > 0 ? ((double) subscribedUsers / totalUsers) * 100 : 0.0;

        Double attrRevenue = paymentRepository.getRevenueFromDiscountedOrders();

        long totalOrders = ordersRepository.count();
        long discountedOrders = discountUsageRepository.count();
        double usageRate = totalOrders > 0 ? ((double) discountedOrders / totalOrders) * 100 : 0.0;

        return new MarketingStatsDTO(subRate, attrRevenue, usageRate);
    }

    // ✅ NEW: Get Chart Data
    public ChartDataDTO getMarketingChartData() {
        List<Object[]> rawData = paymentRepository.getMonthlyDiscountedRevenue();
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        for (Object[] row : rawData) {
            labels.add(row[0].toString()); // "2025-11"
            values.add(((Number) row[1]).doubleValue());
        }

        return new ChartDataDTO(labels, values);
    }
}