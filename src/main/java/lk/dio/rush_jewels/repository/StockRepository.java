package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Collection;
import lk.dio.rush_jewels.model.ProductVariance;
import lk.dio.rush_jewels.model.Stock;
import lk.dio.rush_jewels.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Integer> {

    // =====================================================
    // BASIC FINDERS
    // =====================================================
    List<Stock> findByProductVariance(ProductVariance productVariance);

    Optional<Stock> findByProductVariance_Id(int varianceId);

    Optional<Stock> findByCollection_Id(int collectionId);

    List<Stock> findByCollection(Collection collection);

    // =====================================================
    // WAREHOUSE 1 SPECIFIC (EXISTING)
    // =====================================================
    @Query("SELECT s FROM Stock s WHERE s.productVariance = :pv AND s.warehouse.id = 1")
    List<Stock> findByProductVarianceAndWarehouse1(@Param("pv") ProductVariance pv);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.productVariance = :pv AND s.warehouse.id = 1")
    List<Stock> findLockedByProductVarianceAndWarehouse1(@Param("pv") ProductVariance pv);

    @Query("SELECT s FROM Stock s WHERE s.collection = :col AND s.warehouse.id = 1")
    List<Stock> findByCollectionAndWarehouse1(@Param("col") Collection col);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.collection = :col AND s.warehouse.id = 1")
    List<Stock> findLockedByCollectionAndWarehouse1(@Param("col") Collection col);

    // =====================================================
    // PRODUCT STOCK SUM (UNCHANGED)
    // =====================================================
    @Query("""
        SELECT COALESCE(SUM(s.qty), 0)
        FROM Stock s
        WHERE s.productVariance.id = :pvId
          AND s.stockStatus.id IN (1, 2)
    """)
    Long sumAvailableStockByProductVarianceId(@Param("pvId") Integer pvId);

    @Query("""
        SELECT COALESCE(SUM(s.qty), 0)
        FROM Stock s
        WHERE s.productVariance.id = :pvId
          AND s.stockStatus.id = 1
    """)
    Long sumInStockQtyByProductVarianceId(@Param("pvId") Integer pvId);

    // =====================================================
    // ✅ COLLECTION STOCK SUM (NEW – WAREHOUSE 1)
    // =====================================================
    @Query("""
        SELECT COALESCE(SUM(s.qty), 0)
        FROM Stock s
        WHERE s.collection.id = :collectionId
          AND s.warehouse.id = 1
          AND s.stockStatus.id IN (1, 2)
    """)
    Long sumAvailableStockByCollectionId(@Param("collectionId") Integer collectionId);

    // =====================================================
    // OTHER EXISTING METHODS (UNCHANGED)
    // =====================================================
    @Query("SELECT s FROM Stock s WHERE s.productVariance = :pv AND s.stockStatus.id = 1 AND s.qty > 0")
    Optional<Stock> findAvailableStockByProductVariance(@Param("pv") ProductVariance pv);

    boolean existsByProductVarianceAndStockStatus_IdAndQtyGreaterThan(
            ProductVariance pv, int statusId, int qty
    );

    Optional<Stock> findFirstByProductVariance(ProductVariance pv);

    List<Stock> findByStockStatus_Id(int statusId);

    @Query("""
        SELECT s FROM Stock s
        WHERE (s.productVariance IS NOT NULL AND s.qty <= s.productVariance.stockLimit)
           OR (s.collection IS NOT NULL AND s.qty <= s.collection.stockLimit)
    """)
    List<Stock> findLowStockItems();

    @Query("""
        SELECT s FROM Stock s
        LEFT JOIN FETCH s.productVariance pv
        LEFT JOIN FETCH pv.product
        LEFT JOIN FETCH s.collection c
        LEFT JOIN FETCH s.warehouse w
        WHERE s.stockStatus.id IN :statusIds
    """)
    List<Stock> findByStockStatus_IdIn(@Param("statusIds") List<Integer> statusIds);

    Optional<Stock> findByProductVarianceAndWarehouse(
            ProductVariance productVariance, Warehouse warehouse
    );

    Optional<Stock> findByCollectionAndWarehouse(
            Collection collection, Warehouse warehouse
    );

    Optional<Stock> findByProductVariance_IdAndWarehouse_Id(
            int varianceId, int warehouseId
    );
}
