package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.SupportMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, Integer> {
}