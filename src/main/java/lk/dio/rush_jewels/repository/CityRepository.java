package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // Import List

@Repository
public interface CityRepository extends JpaRepository<City, Integer> {
    // --- NEW: Add this method ---
    List<City> findByProvinceId(Integer provinceId);
}