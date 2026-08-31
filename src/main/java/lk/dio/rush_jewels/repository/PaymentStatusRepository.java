package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentStatusRepository extends JpaRepository<PaymentStatus, Integer> {
    /**
     * Finds a PaymentStatus entity by its status string.
     * This is crucial for setting the status (COMPLETED, PENDING, etc.)
     */
    Optional<PaymentStatus> findByPaymentStatus(String paymentStatus);
}