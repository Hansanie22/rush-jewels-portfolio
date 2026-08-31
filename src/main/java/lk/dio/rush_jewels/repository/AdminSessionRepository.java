package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.AdminSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminSessionRepository extends JpaRepository<AdminSession, Integer> {
}
