package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Orders;
import lk.dio.rush_jewels.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, String> {

    // ✅ Required for Admin Notifications (Fetch all 'Order Placed' by Status ID 1)
    List<Orders> findByOrderStatus_IdOrderByOrderedAtDesc(int statusId);

    // ✅ Required for Admin Notifications (Alternative string based search with limit)
    List<Orders> findTop5ByOrderStatus_OrderStatusOrderByOrderedAtDesc(String status);

    // ✅ Required for Admin Order List
    List<Orders> findAllByOrderByOrderedAtDesc();

    // ✅ Required for Admin Dashboard & Customer Stats
    long countByUser_Id(int userId);

    // ✅ Required for Admin Dashboard (Pending Orders Count)
    long countByOrderStatus_OrderStatus(String status);

    // Top Customers moved to PaymentRepository

    // ✅ Order Status Breakdown Report
    // ✅ Order Status Breakdown Report
    @Query("SELECT os.orderStatus, COUNT(o.id) " +
           "FROM Orders o JOIN o.orderStatus os " +
           "GROUP BY os.orderStatus " +
           "ORDER BY COUNT(o.id) DESC")
    List<Object[]> getOrderStatusBreakdown();

    @Query("SELECT os.orderStatus, COUNT(o.id) " +
           "FROM Orders o JOIN o.orderStatus os " +
           "WHERE o.orderedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY os.orderStatus " +
           "ORDER BY COUNT(o.id) DESC")
    List<Object[]> getOrderStatusBreakdownByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    // --- User App Methods ---

    @Query(value = "SELECT o.id FROM Orders o WHERE o.id LIKE :prefix% ORDER BY o.id DESC LIMIT 1", nativeQuery = false)
    Optional<String> findLastOrderIdByPrefix(@Param("prefix") String prefix);

    List<Orders> findByUserOrderByOrderedAtDesc(User user);

    @Query("SELECT o FROM Orders o ORDER BY o.orderedAt DESC")
    List<Orders> findAllDesc();
}