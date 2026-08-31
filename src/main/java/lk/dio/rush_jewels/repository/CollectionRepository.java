package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Collection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.util.Optional;

import java.util.List;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Collection c WHERE c.id = :id")
    Optional<Collection> findLockedById(@Param("id") Integer id);

    List<Collection> findByStatus_Id(int statusId);

    @Query("SELECT c FROM Collection c WHERE c.status.id = 1 AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :text, '%')) OR LOWER(c.title) LIKE LOWER(CONCAT('%', :text, '%')))")
    List<Collection> findActiveByTextSearch(@Param("text") String text);

    // For suggestions
    List<Collection> findByStatus_IdOrderByIdDesc(int statusId, Pageable pageable);

    // ✅ Updated: Case-insensitive and partial match for suggestions
    @Query("SELECT c.name FROM Collection c WHERE c.status.id = 1 AND LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<String> findNamesByQuery(@Param("query") String query, Pageable pageable);

    @Query("SELECT c FROM Collection c WHERE c.status.id = 1 AND LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Collection> findCollectionsByQuery(@Param("query") String query, Pageable pageable);

    @Query("SELECT c FROM Collection c WHERE c.status.id = 1 AND c.discountPercentage > 0 AND LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Collection> searchActiveCollections(@Param("query") String query);
}