package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {

    // Order by ID to ensure a consistent order
    List<PaymentMethod> findAllByOrderByIdAsc();

    Optional<PaymentMethod> findByMethod(String method);

    List<PaymentMethod> findByIsActiveTrueOrderByIdAsc();
}