package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Shipments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipments, Integer> {

    /**
     * Retrieves all shipments sorted by the shipped date in descending order.
     */
    List<Shipments> findAllByOrderByShippedDateDesc();

    /**
     * Retrieves a list of shipments associated with a specific Order ID.
     */
    List<Shipments> findByOrder_Id(String orderId);
}