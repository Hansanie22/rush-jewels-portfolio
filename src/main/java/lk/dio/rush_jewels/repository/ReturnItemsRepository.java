package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.ReturnItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnItemsRepository extends JpaRepository<ReturnItems, Integer> {
    List<ReturnItems> findByReturns_Id(String returnId);
}