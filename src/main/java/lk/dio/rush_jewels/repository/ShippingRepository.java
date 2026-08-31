package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional; // Import Optional for returning a single entity

public interface ShippingRepository extends JpaRepository<Shipping, Integer> {

    // This ensures "Standard" (id 1) is always first
    List<Shipping> findAllByOrderByIdAsc();

    /**
     * *** NEW METHOD ADDED ***
     * Finds a Shipping entity by its 'value' field.
     * Used in OrderService to retrieve the full entity after the cost (value) is sent from the frontend.
     */
    Optional<Shipping> findByValue(Double value);

    /**
     * Finds a Shipping entity by its method name.
     * Used for secure backend checkout validation.
     */
    Optional<Shipping> findByShippingMethod(String shippingMethod);
}