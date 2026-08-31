package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.BlogPost;
import lk.dio.rush_jewels.model.BlogPostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogPostTagRepository extends JpaRepository<BlogPostTag, Integer> {
    List<BlogPostTag> findByPost(BlogPost post);
    void deleteByPost(BlogPost post);

    @Query("SELECT bpt.tag.name FROM BlogPostTag bpt WHERE bpt.post.id = :postId")
    List<String> findTagNamesByPostId(@Param("postId") int postId);
}

