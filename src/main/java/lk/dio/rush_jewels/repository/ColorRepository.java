package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Color;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColorRepository extends JpaRepository<Color, Integer> {
    boolean existsByColor(String color);
    boolean existsByColorAndIdNot(String color, int id);
    java.util.Optional<Color> findByColor(String color);
}
