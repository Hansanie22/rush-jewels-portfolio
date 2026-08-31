package lk.dio.rush_jewels.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 * Provides endpoints to monitor application and database health
 */
@RestController
@RequestMapping("/health")
public class HealthCheckController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/db")
    public ResponseEntity<Map<String, String>> checkDatabaseConnection() {
        Map<String, String> response = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                response.put("status", "UP");
                response.put("database", "Connected");
                response.put("message", "Database connection is healthy");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("database", "Disconnected");
            response.put("error", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
        response.put("status", "DOWN");
        response.put("database", "Invalid");
        return ResponseEntity.status(503).body(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> checkApplicationHealth() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "RUSH_JEWELS");
        response.put("message", "Application is running");
        return ResponseEntity.ok(response);
    }
}

