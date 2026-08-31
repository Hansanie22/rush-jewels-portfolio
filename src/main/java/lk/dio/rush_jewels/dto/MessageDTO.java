package lk.dio.rush_jewels.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;

public class MessageDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int id;
    private String message;
    private String attachmentUrl;
    private String senderType; // "USER" or "ADMIN"
    private String timestamp;

    // IMPORTANT: @JsonProperty ensures this serializes as "isRead": false
    // properly for the frontend JS (which expects m.isRead)
    @JsonProperty("isRead")
    private boolean isRead;

    private String readAt;

    public MessageDTO() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getReadAt() { return readAt; }
    public void setReadAt(String readAt) { this.readAt = readAt; }
}