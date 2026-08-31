package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Integration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntegrationRepository extends JpaRepository<Integration, Integer> {
}