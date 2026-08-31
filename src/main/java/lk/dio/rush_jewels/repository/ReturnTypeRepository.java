package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.ReturnType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReturnTypeRepository extends JpaRepository<ReturnType, Integer> {

    // Find by returnType column (String)
    Optional<ReturnType> findByReturnType(String returnType);
}
