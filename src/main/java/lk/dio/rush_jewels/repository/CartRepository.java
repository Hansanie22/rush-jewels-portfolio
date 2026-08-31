package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Cart;
import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.model.ProductVariance;
import lk.dio.rush_jewels.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    // 1. Find existing product variance with Pessimistic Write Lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Cart> findByUserAndProductVariance(User user, ProductVariance productVariance);

    // 2. Find existing collection with Pessimistic Write Lock (Added)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Cart> findByUserAndCollection(User user, Collection collection);

    // 3. Load all cart items for a user
    List<Cart> findByUser(User user);

    // 4. Delete all cart items for a user
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.user = :user")
    int deleteByUser(@Param("user") User user);

    // 5. Find cart item by ID and User (security check)
    Optional<Cart> findByIdAndUser(Integer id, User user);

    // 6. Delete cart item by ID and User (security check)
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.id = :cartId AND c.user = :user")
    int deleteByIdAndUser(@Param("cartId") Integer cartId, @Param("user") User user);
}