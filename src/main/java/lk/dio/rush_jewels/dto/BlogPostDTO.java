package lk.dio.rush_jewels.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlogPostDTO implements Serializable {
    private int id;
    private String title;
    private String imagePath;
    private String content; // Full content for detail view, snippet for list
    private String date;
    private String readTime;
    private List<String> tags; // List of tag names
    private String category;

    private List<RelatedItemDTO> relatedItems;
}