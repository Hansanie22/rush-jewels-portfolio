package lk.dio.rush_jewels.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for the Admin Support List.
 */
public class SupportTicketDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int id;
    private String subject;
    private String customerName;
    private String status;
    private LocalDateTime lastUpdated;

    private List<MessageSummaryDTO> messages;

    public SupportTicketDTO() {}

    public SupportTicketDTO(int id, String subject, String customerName, String status, LocalDateTime lastUpdated, List<MessageSummaryDTO> messages) {
        this.id = id;
        this.subject = subject;
        this.customerName = customerName;
        this.status = status;
        this.lastUpdated = lastUpdated;
        this.messages = messages;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    public List<MessageSummaryDTO> getMessages() { return messages; }
    public void setMessages(List<MessageSummaryDTO> messages) { this.messages = messages; }

    public static class MessageSummaryDTO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String senderType;

        // IMPORTANT: @JsonProperty ensures this serializes as "isRead": false
        // Without this, Jackson often defaults to "read": false, breaking JS logic.
        @JsonProperty("isRead")
        private boolean isRead;

        public MessageSummaryDTO() {}

        public MessageSummaryDTO(String senderType, boolean isRead) {
            this.senderType = senderType;
            this.isRead = isRead;
        }

        public String getSenderType() { return senderType; }
        public void setSenderType(String senderType) { this.senderType = senderType; }

        public boolean isRead() { return isRead; }
        public void setRead(boolean read) { isRead = read; }
    }
}