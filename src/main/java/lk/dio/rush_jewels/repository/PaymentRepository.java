package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Orders;
import lk.dio.rush_jewels.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Optional<Payment> findByOrders(Orders orders);

    Optional<Payment> findByTransactionId(String transactionId);

    // Calculate total spent by a user
    @Query("SELECT COALESCE(SUM(p.finalTotal), 0.0) FROM Payment p WHERE p.user.id = :userId")
    Double getTotalSpentByUserId(@Param("userId") int userId);

    Optional<Payment> findByOrders_Id(String orderId);

    @Query("SELECT ps.paymentStatus FROM Payment p JOIN p.paymentStatus ps WHERE p.orders.id = :orderId")
    Optional<String> getPaymentStatusByOrderId(@Param("orderId") String orderId);

    // --- ANALYTICS ---

    @Query("SELECT COALESCE(SUM(p.finalTotal), 0.0) FROM Payment p " +
           "WHERE p.paymentStatus.paymentStatus IN ('COMPLETED', 'PAID', 'SUCCESS') " +
           "OR (p.paymentsMethod.method IN ('Cash on Delivery', 'Store Pickup') AND p.orders.orderStatus.orderStatus != 'Cancelled')")
    Double calculateTotalRevenue();

    @Query("SELECT COALESCE(SUM(p.finalTotal), 0.0) FROM Payment p WHERE p.orders.id IN (SELECT d.ordersId FROM DiscountUsage d)")
    Double getRevenueFromDiscountedOrders();

    @Query(value = "SELECT DATE(COALESCE(p.completed_at, p.created_at)) as d, COUNT(p.id), SUM(p.final_total) " +
            "FROM payment p " +
            "JOIN payment_status ps ON p.payment_status_id = ps.id " +
            "JOIN payments_method pm ON p.payments_method_id = pm.id " +
            "JOIN orders o ON p.orders_id = o.id " +
            "JOIN order_status os ON o.order_status_id = os.id " +
            "WHERE COALESCE(p.completed_at, p.created_at) BETWEEN :startDate AND :endDate " +
            "AND (ps.payment_status IN ('COMPLETED', 'PAID', 'SUCCESS') OR (pm.method IN ('Cash on Delivery', 'Store Pickup') AND os.order_status != 'Cancelled')) " +
            "GROUP BY DATE(COALESCE(p.completed_at, p.created_at)) " +
            "ORDER BY d DESC", nativeQuery = true)
    List<Object[]> getSalesByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query(value = "SELECT DATE_FORMAT(COALESCE(p.completed_at, p.created_at), '%Y-%m') as m, COUNT(p.id), SUM(p.final_total) " +
            "FROM payment p " +
            "JOIN payment_status ps ON p.payment_status_id = ps.id " +
            "JOIN payments_method pm ON p.payments_method_id = pm.id " +
            "JOIN orders o ON p.orders_id = o.id " +
            "JOIN order_status os ON o.order_status_id = os.id " +
            "WHERE YEAR(COALESCE(p.completed_at, p.created_at)) = :year " +
            "AND (ps.payment_status IN ('COMPLETED', 'PAID', 'SUCCESS') OR (pm.method IN ('Cash on Delivery', 'Store Pickup') AND os.order_status != 'Cancelled')) " +
            "GROUP BY m " +
            "ORDER BY m ASC", nativeQuery = true)
    List<Object[]> getMonthlySalesByYear(@Param("year") int year);

    // ✅ Top Customers Report (By Completed Orders from Payment Table)
    @Query("SELECT p.user.fname, p.user.lname, COUNT(p.id), SUM(p.finalTotal) " +
           "FROM Payment p " +
           "WHERE p.orders.orderStatus.id = 4 " +
           "GROUP BY p.user.id, p.user.fname, p.user.lname " +
           "ORDER BY SUM(p.finalTotal) DESC")
    List<Object[]> getTopCustomers(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT p.user.fname, p.user.lname, COUNT(p.id), SUM(p.finalTotal) " +
           "FROM Payment p " +
           "WHERE p.orders.orderStatus.id = 4 AND p.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY p.user.id, p.user.fname, p.user.lname " +
           "ORDER BY SUM(p.finalTotal) DESC")
    List<Object[]> getTopCustomersByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate, org.springframework.data.domain.Pageable pageable);

    @Query(value = "SELECT YEAR(COALESCE(p.completed_at, p.created_at)) as y, COUNT(p.id), SUM(p.final_total) " +
            "FROM payment p " +
            "JOIN payment_status ps ON p.payment_status_id = ps.id " +
            "JOIN payments_method pm ON p.payments_method_id = pm.id " +
            "JOIN orders o ON p.orders_id = o.id " +
            "JOIN order_status os ON o.order_status_id = os.id " +
            "WHERE (ps.payment_status IN ('COMPLETED', 'PAID', 'SUCCESS') OR (pm.method IN ('Cash on Delivery', 'Store Pickup') AND os.order_status != 'Cancelled')) " +
            "GROUP BY y " +
            "ORDER BY y DESC", nativeQuery = true)
    List<Object[]> getYearlySales();

    // 4. FINANCE: Payment Methods (All Time)
    @Query("SELECT p.paymentsMethod.method, COUNT(p), SUM(p.finalTotal), SUM(COALESCE(p.tax, 0.0)), SUM(COALESCE(p.discount, 0.0)) " +
            "FROM Payment p GROUP BY p.paymentsMethod.method")
    List<Object[]> getFinanceByMethod();

    // 5. DASHBOARD GRAPHS
    @Query(value = "SELECT DATE(COALESCE(p.completed_at, p.created_at)) as date, SUM(p.final_total) as total " +
            "FROM payment p " +
            "JOIN payment_status ps ON p.payment_status_id = ps.id " +
            "JOIN payments_method pm ON p.payments_method_id = pm.id " +
            "JOIN orders o ON p.orders_id = o.id " +
            "JOIN order_status os ON o.order_status_id = os.id " +
            "WHERE COALESCE(p.completed_at, p.created_at) >= DATE(NOW()) - INTERVAL 30 DAY " +
            "AND (ps.payment_status IN ('COMPLETED', 'PAID', 'SUCCESS') OR (pm.method IN ('Cash on Delivery', 'Store Pickup') AND os.order_status != 'Cancelled')) " +
            "GROUP BY DATE(COALESCE(p.completed_at, p.created_at)) ORDER BY date ASC", nativeQuery = true)
    List<Object[]> getDailySales();

    @Query(value = "SELECT CONCAT(YEAR(COALESCE(p.completed_at, p.created_at)), '-', WEEK(COALESCE(p.completed_at, p.created_at))) as week, SUM(p.final_total) as total " +
            "FROM payment p " +
            "JOIN payment_status ps ON p.payment_status_id = ps.id " +
            "JOIN payments_method pm ON p.payments_method_id = pm.id " +
            "JOIN orders o ON p.orders_id = o.id " +
            "JOIN order_status os ON o.order_status_id = os.id " +
            "WHERE COALESCE(p.completed_at, p.created_at) >= DATE(NOW()) - INTERVAL 3 MONTH " +
            "AND (ps.payment_status IN ('COMPLETED', 'PAID', 'SUCCESS') OR (pm.method IN ('Cash on Delivery', 'Store Pickup') AND os.order_status != 'Cancelled')) " +
            "GROUP BY week ORDER BY week ASC", nativeQuery = true)
    List<Object[]> getWeeklySales();

    @Query(value = "SELECT DATE_FORMAT(COALESCE(p.completed_at, p.created_at), '%Y-%m') as month, SUM(p.final_total) as total " +
            "FROM payment p " +
            "JOIN payment_status ps ON p.payment_status_id = ps.id " +
            "JOIN payments_method pm ON p.payments_method_id = pm.id " +
            "JOIN orders o ON p.orders_id = o.id " +
            "JOIN order_status os ON o.order_status_id = os.id " +
            "WHERE COALESCE(p.completed_at, p.created_at) >= DATE(NOW()) - INTERVAL 12 MONTH " +
            "AND (ps.payment_status IN ('COMPLETED', 'PAID', 'SUCCESS') OR (pm.method IN ('Cash on Delivery', 'Store Pickup') AND os.order_status != 'Cancelled')) " +
            "GROUP BY month ORDER BY month ASC", nativeQuery = true)
    List<Object[]> getMonthlySales();

    @Query(value = "SELECT DATE_FORMAT(COALESCE(p.completed_at, p.created_at), '%Y-%m') as month, SUM(p.final_total) as total FROM payment p WHERE p.orders_id IN (SELECT d.orders_id FROM discount_usages d) AND COALESCE(p.completed_at, p.created_at) >= DATE(NOW()) - INTERVAL 6 MONTH GROUP BY month ORDER BY month ASC", nativeQuery = true)
    List<Object[]> getMonthlyDiscountedRevenue();


    @Query("SELECT p.paymentsMethod.method, COUNT(p), SUM(p.finalTotal), SUM(COALESCE(p.tax, 0.0)), SUM(COALESCE(p.discount, 0.0)) " +
            "FROM Payment p " +
            "WHERE p.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY p.paymentsMethod.method")
    List<Object[]> getFinanceByMethodAndDateRange(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
    @Query(value = "SELECT DATE_FORMAT(COALESCE(p.completed_at, p.created_at), '%Y-%m-%d %H:%i:%s') AS dt, " +
            "o.id AS orderId, " +
            "p.transaction_id, " +
            "CONCAT(u.fname, ' ', u.lname) AS customerName, " +
            "o.order_source AS channel, " +
            "pm.method AS paymentMethod, " +
            "ps.payment_status AS paymentStatus, " +
            "p.sub_total, " +
            "COALESCE(p.discount, 0.0) AS discount, " +
            "p.final_total, " +
            "COALESCE(p.tendered_amount, p.final_total) AS tenderedAmount, " +
            "COALESCE(p.change_due, 0.0) AS changeDue " +
            "FROM payment p " +
            "JOIN orders o ON p.orders_id = o.id " +
            "JOIN user u ON p.user_id = u.id " +
            "JOIN payments_method pm ON p.payments_method_id = pm.id " +
            "JOIN payment_status ps ON p.payment_status_id = ps.id " +
            "WHERE COALESCE(p.completed_at, p.created_at) BETWEEN :startDate AND :endDate " +
            "ORDER BY COALESCE(p.completed_at, p.created_at) DESC", nativeQuery = true)
    List<Object[]> getTransactionHistory(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}