package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
    
    Optional<User> findFirstByMobile(String mobile);

    // ✅ Added for email verification
    Optional<User> findByVerification(String verification);

    // ✅ Required for Admin Dashboard Analytics (Counts New Customers This Month)
    @Query(value = "SELECT COUNT(*) FROM user WHERE created_at >= DATE_FORMAT(NOW() ,'%Y-%m-01')", nativeQuery = true)
    long countNewCustomersThisMonth();

    // ✅ Find all users subscribed to marketing emails
    List<User> findBySubscribedTrue();
}