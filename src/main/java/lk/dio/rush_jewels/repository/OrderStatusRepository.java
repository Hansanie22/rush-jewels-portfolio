package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrderStatusRepository extends JpaRepository<OrderStatus, Integer> {

    // Method to find an OrderStatus by its string value
    Optional<OrderStatus> findByOrderStatus(String orderStatus);

}