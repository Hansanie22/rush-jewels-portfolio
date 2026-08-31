package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Orders;
import lk.dio.rush_jewels.model.Return;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnRepository extends JpaRepository<Return, String> {
    Optional<Return> findByOrders(Orders orders);

    Optional<Return> findByOrders_Id(String orderId);
    boolean existsByOrders_Id(String orderId);
    boolean existsByOrders(Orders orders);

    @Query("SELECT " +
            "   FUNCTION('DATE', r.approvedDate), " +
            "   COUNT(DISTINCT r.id), " +
            "   SUM(ri.qty * CASE WHEN c.id IS NOT NULL THEN c.price ELSE pv.price END) " +
            "FROM ReturnItems ri " +
            "JOIN ri.returns r " +
            "JOIN ri.orderItems oi " +
            "LEFT JOIN oi.collection c " +
            "LEFT JOIN oi.productVariance pv " +
            "WHERE r.status = 'COMPLETED' " +
            "AND (r.returnType.id = 1 OR r.returnType.id = 2) " +
            "AND r.approvedDate BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('DATE', r.approvedDate)")
    List<Object[]> getReturnsByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Fetch Monthly Returns for a Year
    @Query("SELECT " +
            "   FUNCTION('MONTHNAME', r.approvedDate), " +
            "   COUNT(DISTINCT r.id), " +
            "   SUM(ri.qty * CASE WHEN c.id IS NOT NULL THEN c.price ELSE pv.price END) " +
            "FROM ReturnItems ri " +
            "JOIN ri.returns r " +
            "JOIN ri.orderItems oi " +
            "LEFT JOIN oi.collection c " +
            "LEFT JOIN oi.productVariance pv " +
            "WHERE r.status = 'COMPLETED' " +
            "AND (r.returnType.id = 1 OR r.returnType.id = 2) " +
            "AND FUNCTION('YEAR', r.approvedDate) = :year " +
            "GROUP BY FUNCTION('MONTHNAME', r.approvedDate)")
    List<Object[]> getMonthlyReturnsByYear(@Param("year") int year);

}