package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    interface CategoryWithProductCount {
        Integer getId();
        String getCategory();
        Integer getStatusId();
        Long getProductCount();
    }

    @Query(value = "SELECT c.id AS id, c.category AS category, c.status_id AS statusId, " +
            "COALESCE(COUNT(p.id), 0) AS productCount " +
            "FROM category c " +
            "LEFT JOIN product p ON p.category_id = c.id " +
            "GROUP BY c.id, c.category, c.status_id " +
            "ORDER BY c.category ASC",
            nativeQuery = true)
    List<CategoryWithProductCount> findAllWithProductCount();

    @Query(value = "SELECT c.id AS id, c.category AS category, c.status_id AS statusId, " +
            "COALESCE(COUNT(p.id), 0) AS productCount " +
            "FROM category c " +
            "LEFT JOIN product p ON p.category_id = c.id " +
            "WHERE c.id = :id " +
            "GROUP BY c.id, c.category, c.status_id",
            nativeQuery = true)
    CategoryWithProductCount findWithProductCountById(@Param("id") Integer id);

    boolean existsByCategoryIgnoreCase(String category);

    Optional<Category> findByCategory(String category);

    List<Category> findByStatusId(int statusId);


}