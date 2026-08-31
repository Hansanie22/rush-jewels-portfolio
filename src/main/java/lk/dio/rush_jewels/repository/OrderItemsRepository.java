package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.OrderItems;
import lk.dio.rush_jewels.model.Orders;
import lk.dio.rush_jewels.model.ProductVariance;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface OrderItemsRepository extends JpaRepository<OrderItems, Integer> {

    // ✅ 1. Find Items by Order ID (Used for Order Details Modal)
    List<OrderItems> findByOrders_Id(String orderId);

    // ✅ 2. Find Items by Order Entity (Used for Service Logic)
    List<OrderItems> findByOrders(Orders orders);

    // ✅ 3. Sum Quantity Sold (Consolidated method)
    @Query("SELECT COALESCE(SUM(oi.qty), 0) FROM OrderItems oi WHERE oi.productVariance = :pv AND oi.orders.orderStatus.id = :statusId")
    Integer sumQtyByProductVarianceAndOrderStatusId(@Param("pv") ProductVariance pv, @Param("statusId") int statusId);

    // ✅ 4. Product Performance (All Time) - JPQL
    @Query("SELECT " +
            // 0. Name
            "   CASE " +
            "       WHEN c.id IS NOT NULL THEN c.name " +
            "       ELSE CONCAT( " +
            "              p.name, " +
            "              COALESCE(CONCAT(' - ', sz.size), ''), " +
            "              COALESCE(CONCAT(' - ', clr.color), ''), " +
            "              COALESCE(CONCAT(' - ', gm.gemStone), '') " +
            "       ) " +
            "   END, " +

            // 1. Category
            "   CASE WHEN c.id IS NOT NULL THEN 'Collection' ELSE COALESCE(cat.category, 'General') END, " +

            // 2. Units Sold (Gross)
            "   SUM(oi.qty), " +

            // 3. Gross Revenue
            "   SUM(oi.qty * CASE WHEN c.id IS NOT NULL THEN c.price ELSE pv.price END), " +

            // 4. Current Stock (Warehouse 1)
            "   (SELECT COALESCE(SUM(s.qty), 0) " +
            "    FROM Stock s " +
            "    WHERE s.warehouse.id = 1 " +
            "    AND ( " +
            "       (c.id IS NOT NULL AND s.collection.id = c.id) OR " +
            "       (pv.id IS NOT NULL AND s.productVariance.id = pv.id) " +
            "    ) " +
            "   ), " +

            // 5. Units Returned (Status = COMPLETED AND Type = 1 or 2)
            "   (SELECT COALESCE(SUM(ri.qty), 0) " +
            "    FROM ReturnItems ri " +
            "    JOIN ri.returns r " +
            "    JOIN ri.orderItems oi2 " +
            "    WHERE r.status = 'COMPLETED' " +
            "    AND (r.returnType.id = 1 OR r.returnType.id = 2) " +
            "    AND ( " +
            "       (c.id IS NOT NULL AND oi2.collection.id = c.id) OR " +
            "       (pv.id IS NOT NULL AND oi2.productVariance.id = pv.id) " +
            "    ) " +
            "   ) " +

            "FROM OrderItems oi " +
            "LEFT JOIN oi.collection c " +
            "LEFT JOIN oi.productVariance pv " +
            "LEFT JOIN pv.product p " +
            "LEFT JOIN pv.size sz " +
            "LEFT JOIN pv.color clr " +
            "LEFT JOIN pv.gemstone gm " +
            "LEFT JOIN p.category cat " +
            "GROUP BY c.id, pv.id, c.name, p.name, cat.category, c.price, pv.price, sz.size, clr.color, gm.gemStone " +
            "ORDER BY SUM(oi.qty) DESC")
    List<Object[]> getProductPerformance();

    // ✅ 5. Product Performance (Date Range) - Native SQL
    @Query(value =
            "SELECT " +
                    "    COALESCE(c.name, p.name) AS productName, " +
                    "    COALESCE(p_cat.category, 'Collection') AS categoryName, " +
                    "    SUM(oi.qty) AS unitsSold, " +
                    "    SUM(oi.qty * CASE WHEN c.id IS NOT NULL THEN c.price ELSE pv.price END) AS revenue, " +
                    "    COALESCE(MAX(s.qty), 0) AS stock, " +
                    "    COALESCE(( " +
                    "        SELECT SUM(ri.qty) " +
                    "        FROM return_items ri " +
                    "        JOIN `return` r ON r.id = ri.return_id " +
                    "        WHERE r.status = 'COMPLETED' " +
                    "        AND (r.return_type_id = 1 OR r.return_type_id = 2) " +
                    "        AND r.approved_date BETWEEN :returnStart AND :returnEnd " +
                    "        AND ( " +
                    "            (ri.order_items_id IN (SELECT id FROM order_items WHERE collection_id = c.id)) " +
                    "            OR " +
                    "            (ri.order_items_id IN (SELECT id FROM order_items WHERE product_variance_id = pv.id)) " +
                    "        ) " +
                    "    ), 0) AS returned " +
                    "FROM order_items oi " +
                    "LEFT JOIN collection c ON c.id = oi.collection_id " +
                    "LEFT JOIN product_variance pv ON pv.id = oi.product_variance_id " +
                    "LEFT JOIN product p ON p.id = pv.product_id " +
                    "LEFT JOIN category p_cat ON p_cat.id = p.category_id " +
                    "LEFT JOIN stock s ON s.collection_id = c.id OR s.product_variance_id = pv.id " +
                    "JOIN orders o ON o.id = oi.orders_id " +
                    "WHERE o.ordered_at BETWEEN :orderStart AND :orderEnd " +
                    "GROUP BY c.id, pv.id, c.name, p.name, p_cat.category",
            nativeQuery = true)
    List<Object[]> getProductPerformanceByDateRange(
            @Param("orderStart") Date orderStart,
            @Param("orderEnd") Date orderEnd,
            @Param("returnStart") String returnStart,
            @Param("returnEnd") String returnEnd
    );

    // ✅ 6. Best Selling Products (Used for Dashboard/Widgets)
    @Query("SELECT p.name FROM OrderItems oi " +
            "JOIN oi.productVariance pv " +
            "JOIN pv.product p " +
            "WHERE p.status.id = 1 AND oi.orders.orderStatus.id = 4 " +
            "GROUP BY p.id, p.name " +
            "ORDER BY SUM(oi.qty) DESC")
    List<String> findBestSellingProductNames(Pageable pageable);
}