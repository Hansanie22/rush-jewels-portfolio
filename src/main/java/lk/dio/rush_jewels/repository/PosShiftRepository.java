package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.PosShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PosShiftRepository extends JpaRepository<PosShift, Integer> {
}
