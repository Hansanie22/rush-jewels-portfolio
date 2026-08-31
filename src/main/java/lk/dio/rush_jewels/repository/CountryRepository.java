package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends JpaRepository<Country, Integer> {
    // Optional: add custom queries if needed
}
