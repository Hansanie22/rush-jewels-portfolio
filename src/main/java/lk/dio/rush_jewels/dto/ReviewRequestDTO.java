package lk.dio.rush_jewels.dto;
import java.io.Serializable;

public class ReviewRequestDTO implements Serializable {
    private Integer rating;
    private String comment;
    private Integer productVariantId; // Optional
    private Integer collectionId;     // Optional

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Integer getProductVariantId() { return productVariantId; }
    public void setProductVariantId(Integer productVariantId) { this.productVariantId = productVariantId; }
    public Integer getCollectionId() { return collectionId; }
    public void setCollectionId(Integer collectionId) { this.collectionId = collectionId; }
}