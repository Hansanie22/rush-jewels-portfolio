package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Gemstone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GemstoneRepository extends JpaRepository<Gemstone, Integer> {
    boolean existsByGemStone(String gemStone);
    boolean existsByGemStoneAndIdNot(String gemStone, int id);
    java.util.Optional<Gemstone> findByGemStone(String gemStone);
}
