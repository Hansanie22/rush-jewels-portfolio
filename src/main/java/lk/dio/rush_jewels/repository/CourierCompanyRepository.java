package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.CourierCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourierCompanyRepository extends JpaRepository<CourierCompany, Integer> {
    // Standard CRUD methods (findAll, findById, save, delete) are included automatically.
}