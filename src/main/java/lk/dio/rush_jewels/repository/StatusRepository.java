package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusRepository extends JpaRepository<Status, Integer> {
    // This interface automatically provides methods like findById()
    // which the UserService needs to retrieve Status 1 (Active) and Status 2 (Unverified).
}