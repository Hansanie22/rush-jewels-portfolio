package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // Import List

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Integer> {
    // --- NEW: Add this method ---
    List<Province> findByCountryId(Integer countryId);
}