package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.dto.MessageDTO;
import lk.dio.rush_jewels.dto.TicketDTO;
import lk.dio.rush_jewels.model.SupportMessage;
import lk.dio.rush_jewels.model.SupportTicket;
import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.repository.SupportMessageRepository;
import lk.dio.rush_jewels.repository.SupportTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupportService {

    private final SupportTicketRepository ticketRepo;
    private final SupportMessageRepository messageRepo;
    private final CloudinaryService cloudinaryService; // ✅ Cloudinary Service එක සම්බන්ධ කළා

    // ✅ NOTE: Local file path configuration removed.

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public SupportService(SupportTicketRepository ticketRepo,
                          SupportMessageRepository messageRepo,
                          CloudinaryService cloudinaryService) {
        this.ticketRepo = ticketRepo;
        this.messageRepo = messageRepo;
        this.cloudinaryService = cloudinaryService;
    }

    // 1. Create Ticket
    @Transactional
    public TicketDTO createTicket(User user, String subject, String messageText, MultipartFile file) throws IOException {
        SupportTicket ticket = new SupportTicket();
        ticket.setSubject(subject);
        ticket.setUser(user);
        ticket.setStatus("OPEN");
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        ticket = ticketRepo.save(ticket);

        String attachmentUrl = null;
        // ✅ Cloudinary Upload Logic
        if (file != null && !file.isEmpty()) {
            attachmentUrl = cloudinaryService.uploadImage(file);
        }

        // Message from USER is initially UNREAD for the ADMIN
        SupportMessage firstMessage = new SupportMessage(messageText, attachmentUrl, "USER", ticket);
        firstMessage.setCreatedAt(LocalDateTime.now());
        firstMessage.setRead(false);
        messageRepo.save(firstMessage);

        return mapToTicketDTO(ticket, false);
    }

    // 2. Reply to Ticket
    @Transactional
    public MessageDTO replyToTicket(int ticketId, User user, String messageText, MultipartFile file) throws IOException {
        SupportTicket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (ticket.getUser().getId() != user.getId()) {
            throw new SecurityException("Unauthorized access to ticket");
        }

        String attachmentUrl = null;
        // ✅ Cloudinary Upload Logic
        if (file != null && !file.isEmpty()) {
            attachmentUrl = cloudinaryService.uploadImage(file);
        }

        // Reply from USER is UNREAD for the ADMIN
        SupportMessage message = new SupportMessage(messageText, attachmentUrl, "USER", ticket);
        message.setCreatedAt(LocalDateTime.now());
        message.setRead(false);
        messageRepo.save(message);

        // User reply usually keeps/sets status to OPEN
        ticket.setStatus("OPEN");
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepo.save(ticket);

        return mapToMessageDTO(message);
    }

    // 3. Get User Tickets
    public List<TicketDTO> getUserTickets(User user) {
        return ticketRepo.findByUserOrderByUpdatedAtDesc(user).stream()
                .map(t -> mapToTicketDTO(t, true))
                .collect(Collectors.toList());
    }

    // 4. Get Ticket Details (This is called when User opens the chat)
    @Transactional
    public TicketDTO getTicketDetails(int ticketId, User user) {
        SupportTicket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (ticket.getUser().getId() != user.getId()) {
            throw new SecurityException("Unauthorized access to ticket");
        }

        // --- CORE LOGIC: MARK ADMIN MESSAGES AS READ ---
        List<SupportMessage> unreadAdminMessages = ticket.getMessages().stream()
                .filter(m -> "ADMIN".equals(m.getSenderType()) && !m.isRead())
                .collect(Collectors.toList());

        if (!unreadAdminMessages.isEmpty()) {
            LocalDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Colombo")).toLocalDateTime();
            for (SupportMessage m : unreadAdminMessages) {
                m.setRead(true);
                m.setReadAt(now);
                messageRepo.save(m);
            }
        }
        // -----------------------------------------------

        return mapToTicketDTO(ticket, true);
    }

    // --- Helpers ---

    // ❌ Removed: saveFile() method (No longer needed)

    private TicketDTO mapToTicketDTO(SupportTicket t, boolean includeMessages) {
        TicketDTO dto = new TicketDTO();
        dto.setId(t.getId());
        dto.setSubject(t.getSubject());
        dto.setStatus(t.getStatus());

        if (t.getCreatedAt() != null) {
            dto.setCreatedAt(t.getCreatedAt().format(DATE_FMT));
        }

        if (includeMessages && t.getMessages() != null) {
            List<MessageDTO> sortedMessages = t.getMessages().stream()
                    .sorted(Comparator.comparing(SupportMessage::getCreatedAt))
                    .map(this::mapToMessageDTO)
                    .collect(Collectors.toList());

            dto.setMessages(sortedMessages);
        }
        return dto;
    }

    private MessageDTO mapToMessageDTO(SupportMessage m) {
        MessageDTO dto = new MessageDTO();
        dto.setId(m.getId());
        dto.setMessage(m.getMessage());
        dto.setSenderType(m.getSenderType());

        if (m.getCreatedAt() != null) {
            dto.setTimestamp(m.getCreatedAt().format(DATE_FMT));
        }

        // ✅ Cloudinary URL Direct Assignment
        dto.setAttachmentUrl(m.getAttachmentPath());

        dto.setRead(m.isRead());
        if (m.getReadAt() != null) {
            dto.setReadAt(m.getReadAt().format(DATE_FMT));
        }

        return dto;
    }
}