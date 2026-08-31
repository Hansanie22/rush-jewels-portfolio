package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "banner")
public class Banner implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "media_path", nullable = false)
    private String mediaPath;

    @Column(name = "media_type", nullable = false)
    private String mediaType; // "IMAGE" or "VIDEO"

    public Banner() {}

    public Banner(String mediaPath, String mediaType) {
        this.mediaPath = mediaPath;
        this.mediaType = mediaType;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMediaPath() { return mediaPath; }
    public void setMediaPath(String mediaPath) { this.mediaPath = mediaPath; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
}