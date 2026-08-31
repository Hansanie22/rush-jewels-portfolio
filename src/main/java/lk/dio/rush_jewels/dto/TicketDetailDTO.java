package lk.dio.rush_jewels.dto;

import java.util.List;

public class TicketDetailDTO {
    private int id;
    private String subject;
    private String status;
    private String customerName;
    private String customerEmail;
    private String customerContact; // Fetched from DeliveryAddress
    private List<MessageDTO> messages;

    public TicketDetailDTO(int id, String subject, String status, String customerName, String customerEmail, String customerContact, List<MessageDTO> messages) {
        this.id = id;
        this.subject = subject;
        this.status = status;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerContact = customerContact;
        this.messages = messages;
    }
    // Getters...
    public int getId() { return id; }
    public String getSubject() { return subject; }
    public String getStatus() { return status; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerContact() { return customerContact; }
    public List<MessageDTO> getMessages() { return messages; }

    public static class MessageDTO {
        public String sender; // "USER" or "ADMIN"
        public String message;
        public String attachment;
        public String time;
        public boolean isRead;
        public String readAt;

        public MessageDTO(String sender, String message, String attachment, String time, boolean isRead, String readAt) {
            this.sender = sender;
            this.message = message;
            this.attachment = attachment;
            this.time = time;
            this.isRead = isRead;
            this.readAt = readAt;
        }
    }
}