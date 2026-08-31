package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.CourierService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourierServiceRepository extends JpaRepository<CourierService, Integer> {
    // Fetch all branches for a specific company ID
    List<CourierService> findByCourierCompanyId(int courierCompanyId);
}