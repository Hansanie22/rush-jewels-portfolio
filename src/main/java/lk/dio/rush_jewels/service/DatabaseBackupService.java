package lk.dio.rush_jewels.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DatabaseBackupService {

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    private final String dbName = "rush_jewels";
    private final String backupDir = "/opt/rush_jewels/backups/";

    @Scheduled(cron = "0 0 2 * * ?") // Runs every day at 2:00 AM
    public void backupDatabase() {
        System.out.println("Starting scheduled database backup...");
        performBackup();
    }

    public String performBackup() {
        File dir = new File(backupDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "backup_" + dbName + "_" + timestamp + ".sql.gz";
        String filePath = backupDir + fileName;

        // Command: mysqldump -u root -pPassword dbname | gzip > /path/to/backup.sql.gz
        String command = String.format("mysqldump -u%s -p%s %s | gzip > %s", dbUsername, dbPassword, dbName, filePath);
        
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                System.out.println("Database backup created successfully: " + filePath);
                cleanOldBackups();
                return fileName;
            } else {
                System.err.println("Error creating database backup, exit code: " + exitCode);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Exception during database backup: " + e.getMessage());
            return null;
        }
    }

    private void cleanOldBackups() {
        try {
            File dir = new File(backupDir);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".sql.gz"));
            if (files != null && files.length > 7) {
                Arrays.sort(files, Comparator.comparingLong(File::lastModified));
                // Delete oldest files keeping only 7
                for (int i = 0; i < files.length - 7; i++) {
                    files[i].delete();
                    System.out.println("Deleted old backup: " + files[i].getName());
                }
            }
        } catch (Exception e) {
            System.err.println("Error cleaning old backups: " + e.getMessage());
        }
    }

    public List<String> getAvailableBackups() {
        try {
            File dir = new File(backupDir);
            if (!dir.exists()) return List.of();
            
            return Files.list(Paths.get(backupDir))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".sql.gz"))
                    .map(path -> path.getFileName().toString())
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return List.of();
        }
    }

    public Path getBackupPath(String fileName) {
        return Paths.get(backupDir, fileName);
    }
}
