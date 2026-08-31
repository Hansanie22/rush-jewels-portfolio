package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.AddressType;
import lk.dio.rush_jewels.model.DeliveryAddress;
import lk.dio.rush_jewels.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Integer> {

    List<DeliveryAddress> findByUser(User user);

    Optional<DeliveryAddress> findTopByUserOrderByIdDesc(User user);

    Optional<DeliveryAddress> findByUserAndAddressTypeAndDefaultAddressTrue(User user, AddressType addressType);

    Optional<DeliveryAddress> findTopByUserAndAddressTypeOrderByIdDesc(User user, AddressType addressType);

    // Required for CheckoutAddressService logic
    boolean existsByUserAndAddressTypeAndDefaultAddressTrue(User user, AddressType addressType);

    // --- *** FIX *** ---
    // The query must set defaultAddress = false, not true.
    @Modifying
    @Query("UPDATE DeliveryAddress a SET a.defaultAddress = false WHERE a.user = :user AND a.addressType = :addressType")
    void updateAllDefaultToFalse(@Param("user") User user, @Param("addressType") AddressType addressType);
}