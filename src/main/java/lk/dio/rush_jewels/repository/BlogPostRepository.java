package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Integer> {
    List<BlogPost> findAllByOrderByCreatedAtDesc();

    List<BlogPost> findTop3ByOrderByCreatedAtDesc();

    List<BlogPost> findTop3ByIsPublishedTrueOrderByCreatedAtDesc();
}