package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Shipments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentsRepository extends JpaRepository<Shipments, Integer> {
    // Basic list order
    List<Shipments> findAllByOrderByShippedDateDesc();
}