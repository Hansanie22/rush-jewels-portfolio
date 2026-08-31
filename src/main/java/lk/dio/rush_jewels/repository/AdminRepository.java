package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {

    Optional<Admin> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Admin> findByEmailAndPassword(String email, String password);

    @Query("SELECT a FROM Admin a WHERE a.email = :email AND a.status.id = :statusId")
    Optional<Admin> findByEmailAndStatusId(@Param("email") String email, @Param("statusId") Integer statusId);

    long count();

    @Query("SELECT COUNT(a) FROM Admin a WHERE a.status.id = :statusId")
    long countByStatusId(@Param("statusId") Integer statusId);

    boolean existsById(Integer id);
}