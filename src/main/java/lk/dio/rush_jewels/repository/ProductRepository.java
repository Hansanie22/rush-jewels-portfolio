package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Existing method – do not change
    List<Product> findByStatus_IdOrderByIdDesc(int statusId, Pageable pageable);

    // New method to fetch products by category
    List<Product> findByCategoryId(int categoryId);


    // New: Find Product Names starting with query (for autocomplete)
    @Query("SELECT p.name FROM Product p WHERE p.status.id = 1 AND LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<String> findNamesByQuery(@Param("query") String query, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p JOIN ProductVariance pv ON pv.product = p WHERE p.status.id = 1 AND pv.stockLimit > 0 ORDER BY p.id DESC")
    List<Product> findActiveInStockProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status.id = 1 AND LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> findProductsByQuery(@Param("query") String query, Pageable pageable);
}
