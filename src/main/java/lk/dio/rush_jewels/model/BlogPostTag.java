package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Entity
@Table(name = "blog_post_tag")
@Getter
@Setter
public class BlogPostTag implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id")
    private BlogPost post;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    public BlogPostTag() {}

    public BlogPostTag(BlogPost post, Tag tag) {
        this.post = post;
        this.tag = tag;
    }
}
