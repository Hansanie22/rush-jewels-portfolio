package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Collection;
import lk.dio.rush_jewels.model.ProductVariance;
import lk.dio.rush_jewels.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Integer> {
    List<Sale> findByIsActiveTrue();

    Optional<Sale> findFirstByIsActiveTrueOrderByIdDesc();

    @Query("SELECT s FROM Sale s " +
            "WHERE s.isActive = true AND s.endDate >= CURRENT_DATE " +
            "ORDER BY s.startDate DESC LIMIT 1")
    Sale findLatestActiveSale();
}
