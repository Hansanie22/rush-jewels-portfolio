package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.StockStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockStatusRepository extends JpaRepository<StockStatus, Integer> {

    Optional<StockStatus> findByStockStatus(String stockStatus);
}