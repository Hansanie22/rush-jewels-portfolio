package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.Collection;
import lk.dio.rush_jewels.model.CollectionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionSetRepository extends JpaRepository<CollectionSet, Integer> {

    // Finds sets by the Collection entity (Matches the service logic)
    List<CollectionSet> findByCollection(Collection collection);

    // Finds sets by Collection ID (Useful for direct ID lookups)
    List<CollectionSet> findByCollection_Id(int collectionId);

    // Deletes sets by Collection ID
    void deleteByCollection_Id(int collectionId);
}