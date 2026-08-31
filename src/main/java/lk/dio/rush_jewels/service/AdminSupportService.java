package lk.dio.rush_jewels.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.dto.SupportTicketDTO;
import lk.dio.rush_jewels.dto.TicketDetailDTO;
import lk.dio.rush_jewels.model.AdminAuditLog;
import lk.dio.rush_jewels.model.DeliveryAddress;
import lk.dio.rush_jewels.model.SupportMessage;
import lk.dio.rush_jewels.model.SupportTicket;
import lk.dio.rush_jewels.repository.AdminAuditLogRepository;
import lk.dio.rush_jewels.repository.SupportMessageRepository;
import lk.dio.rush_jewels.repository.SupportTicketRepository;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminSupportService {

    private final SupportTicketRepository ticketRepository;
    private final SupportMessageRepository messageRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final CloudinaryService cloudinaryService; // ✅ Cloudinary Service එක සම්බන්ධ කළා

    public AdminSupportService(SupportTicketRepository ticketRepository,
                               SupportMessageRepository messageRepository,
                               AdminAuditLogRepository auditLogRepository,
                               ObjectMapper objectMapper,
                               CloudinaryService cloudinaryService) {
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
        this.cloudinaryService = cloudinaryService;
    }

    /**
     * Get All Tickets for List View.
     */
    @Retryable(value = {IOException.class, Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
    public List<SupportTicketDTO> getAllTickets() {
        return ticketRepository.findAllByOrderByUpdatedAtDesc().stream().map(t -> {
            List<SupportTicketDTO.MessageSummaryDTO> messageSummaries = t.getMessages().stream()
                    .map(m -> new SupportTicketDTO.MessageSummaryDTO(m.getSenderType(), m.isRead()))
                    .collect(Collectors.toList());

            return new SupportTicketDTO(
                    t.getId(),
                    t.getSubject(),
                    t.getUser().getFname() + " " + t.getUser().getLname(),
                    t.getStatus(),
                    t.getUpdatedAt(),
                    messageSummaries
            );
        }).collect(Collectors.toList());
    }

    /**
     * Get Details for Single Ticket.
     */
    @Retryable(value = {IOException.class, Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
    public TicketDetailDTO getTicketDetails(int id) {
        SupportTicket t = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // === MARK USER MESSAGES AS READ ===
        List<SupportMessage> unreadUserMessages = t.getMessages().stream()
                .filter(m -> "USER".equals(m.getSenderType()) && !m.isRead())
                .collect(Collectors.toList());

        if(!unreadUserMessages.isEmpty()) {
            LocalDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Colombo")).toLocalDateTime();
            for(SupportMessage m : unreadUserMessages) {
                m.setRead(true);
                m.setReadAt(now);
                messageRepository.save(m);
            }
        }
        // ==================================

        String contact = "N/A";
        if (t.getUser().getAddresses() != null && !t.getUser().getAddresses().isEmpty()) {
            contact = t.getUser().getAddresses().stream()
                    .filter(DeliveryAddress::isDefaultAddress)
                    .findFirst()
                    .map(DeliveryAddress::getContactNo)
                    .orElse(t.getUser().getAddresses().iterator().next().getContactNo());
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");

        List<TicketDetailDTO.MessageDTO> msgs = t.getMessages().stream().map(m -> new TicketDetailDTO.MessageDTO(
                m.getSenderType(),
                m.getMessage(),
                // ✅ Changed: දැන් කෙලින්ම Cloudinary URL එක යවනවා (Local path හදන්නේ නෑ)
                m.getAttachmentPath(),
                m.getCreatedAt().format(formatter),
                m.isRead(),
                m.getReadAt() != null ? m.getReadAt().format(formatter) : null
        )).collect(Collectors.toList());

        return new TicketDetailDTO(
                t.getId(),
                t.getSubject(),
                t.getStatus(),
                t.getUser().getFname() + " " + t.getUser().getLname(),
                t.getUser().getEmail(),
                contact,
                msgs
        );
    }

    public void replyToTicket(int ticketId, String message, MultipartFile file) throws IOException {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        String attachmentUrl = null;

        // ✅ Handle File Upload to Cloudinary
        if (file != null && !file.isEmpty()) {
            attachmentUrl = cloudinaryService.uploadImage(file);
        }

        // attachmentUrl එක DB එකේ සේව් වෙනවා
        SupportMessage msg = new SupportMessage(message, attachmentUrl, "ADMIN", ticket);
        msg.setCreatedAt(LocalDateTime.now());
        msg.setRead(false);
        messageRepository.save(msg);

        String oldStatus = ticket.getStatus();
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        Map<String, String> oldVal = new HashMap<>();
        oldVal.put("previousStatus", oldStatus);

        logAction("REPLY_TICKET", "support_message", String.valueOf(msg.getId()), oldVal, msg);
    }

    public void updateStatus(int ticketId, String status) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        String oldValue = convertToJson(ticket);
        ticket.setStatus(status);
        SupportTicket savedTicket = ticketRepository.save(ticket);

        logAction("UPDATE_STATUS", "support_ticket", String.valueOf(ticketId), oldValue, savedTicket);
    }

    private void logAction(String action, String table, String recordId, Object oldValueObj, Object newValueObj) {
        try {
            String oldValue = (oldValueObj instanceof String) ? (String) oldValueObj : convertToJson(oldValueObj);
            String newValue = (newValueObj instanceof String) ? (String) newValueObj : convertToJson(newValueObj);
            AdminAuditLog log = new AdminAuditLog(action, table, recordId, oldValue, newValue, LocalDateTime.now());
            auditLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Audit Log Failed: " + e.getMessage());
        }
    }

    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(sanitizeForAudit(object));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private Object sanitizeForAudit(Object obj) {
        if (obj instanceof SupportTicket) {
            SupportTicket t = (SupportTicket) obj;
            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("subject", t.getSubject());
            map.put("status", t.getStatus());
            map.put("userId", t.getUser().getId());
            return map;
        }
        return obj;
    }
}