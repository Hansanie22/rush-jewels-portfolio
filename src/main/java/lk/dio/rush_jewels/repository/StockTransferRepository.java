package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.StockTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockTransferRepository extends JpaRepository<StockTransfer, Integer> {
}