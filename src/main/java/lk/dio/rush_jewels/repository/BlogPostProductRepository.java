package lk.dio.rush_jewels.repository;

import lk.dio.rush_jewels.model.BlogPost;
import lk.dio.rush_jewels.model.BlogPostProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogPostProductRepository extends JpaRepository<BlogPostProduct, Integer> {
    List<BlogPostProduct> findByPost(BlogPost post);
    void deleteByPost(BlogPost post);

    @Query("SELECT bpp FROM BlogPostProduct bpp WHERE bpp.post.id = :postId")
    List<BlogPostProduct> findByPostId(@Param("postId") int postId);
}

