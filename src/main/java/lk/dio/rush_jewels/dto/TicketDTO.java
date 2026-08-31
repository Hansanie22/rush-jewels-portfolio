package lk.dio.rush_jewels.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class TicketDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int id;
    private String subject;
    private String status;
    private String createdAt;
    private List<MessageDTO> messages;

    public TicketDTO() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public List<MessageDTO> getMessages() { return messages; }
    public void setMessages(List<MessageDTO> messages) { this.messages = messages; }
}