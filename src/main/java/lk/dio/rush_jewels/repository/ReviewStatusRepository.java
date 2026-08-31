package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewStatusRepository extends JpaRepository<ReviewStatus, Integer> {
}