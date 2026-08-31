package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SizeRepository extends JpaRepository<Size, Integer> {

    // Check for duplicates during Creation
    boolean existsBySizeAndCategory_Id(String size, int categoryId);

    // Check for duplicates during Update (excluding self)
    boolean existsBySizeAndCategory_IdAndIdNot(String size, int categoryId, int id);
    
    List<Size> findBySize(String size);
}