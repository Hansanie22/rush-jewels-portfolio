package lk.dio.rush_jewels.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import lk.dio.rush_jewels.model.AdminAuditLog;
import lk.dio.rush_jewels.repository.AdminAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AdminAuditBackupService {

    @Autowired
    private AdminAuditLogRepository auditLogRepository;

    /**
     * පරිගණකයේ පද්ධතිමය Downloads ෆෝල්ඩරය හඳුනාගෙන එහි 'audit-backups' ෆෝල්ඩරය සාදයි.
     */
    private String getBackupFolderPath() {
        String userHome = System.getProperty("user.home");
        // Windows, Linux සහ Mac සඳහා පොදු Downloads පථය (Path)
        Path downloadsPath = Paths.get(userHome, "Downloads", "audit-backups");
        return downloadsPath.toAbsolutePath().toString();
    }

    @Scheduled(cron = "0 59 23 * * ?") // සෑම දිනකම රාත්‍රී 23:59 ට ක්‍රියාත්මක වේ
    public void backupAndClearLogs() {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

            // 1. එදිනට අදාළ logs පමණක් ලබා ගැනීම
            List<AdminAuditLog> logs = auditLogRepository.findAll().stream()
                    .filter(log -> log.getActionTime().isAfter(startOfDay.minusSeconds(1)) &&
                            log.getActionTime().isBefore(endOfDay.plusSeconds(1)))
                    .toList();

            // logs නොමැති නම් ක්‍රියාවලිය නවත්වන්න
            if (logs.isEmpty()) {
                System.out.println("No logs found for today: " + today);
                return;
            }

            // 2. ෆෝල්ඩරය පවතීදැයි පරීක්ෂා කර නොමැති නම් සාදන්න
            String backupFolder = getBackupFolderPath();
            Path folderPath = Paths.get(backupFolder);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            String fileName = backupFolder + "/" + "audit-log-" + today + ".pdf";

            // 3. PDF එක නිර්මාණය කිරීම
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Font විස්තර
            Font headerFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 14);
            Font logFont = FontFactory.getFont(FontFactory.COURIER, 10);

            // Header එක එකතු කිරීම
            Paragraph header = new Paragraph("Rush Jewels - Audit Log Backup", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph("Date: " + today, logFont);
            subHeader.setAlignment(Element.ALIGN_CENTER);
            subHeader.setSpacingAfter(10f);
            document.add(subHeader);

            // ඉරක් (Line) ඇඳීම
            LineSeparator line = new LineSeparator();
            document.add(new Chunk(line));
            document.add(Chunk.NEWLINE);

            // Logs එකින් එක PDF එකට ඇතුළත් කිරීම
            for (AdminAuditLog log : logs) {
                StringBuilder logEntry = new StringBuilder();
                logEntry.append(log.getActionTime().toLocalTime().withNano(0)) // කාලය පමණක්
                        .append(" | ").append(log.getActionType())
                        .append(" | Table: ").append(log.getTableName())
                        .append(" | ID: ").append(log.getRecordId());

                // IP Address එක extract කිරීම (JSON ඇතුළත තිබේ නම්)
                if (log.getNewValue() != null && log.getNewValue().contains("ipAddress")) {
                    try {
                        String ip = log.getNewValue().replaceAll(".*\"ipAddress\"\\s*:\\s*\"([^\"]+)\".*", "$1");
                        logEntry.append(" | IP: ").append(ip);
                    } catch (Exception ignored) {}
                }

                Paragraph logPara = new Paragraph(logEntry.toString(), logFont);
                logPara.setSpacingAfter(4f);
                document.add(logPara);
            }

            document.close();
            System.out.println("Backup successfully saved to: " + fileName);

            // 4. Backup එක සාර්ථක නම් පමණක් Database එකෙන් logs ඉවත් කරන්න
            auditLogRepository.deleteAll(logs);

        } catch (Exception e) {
            System.err.println("Error during audit backup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}