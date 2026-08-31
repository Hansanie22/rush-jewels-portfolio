package lk.dio.rush_jewels.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletResponse response) {
        if (response.isCommitted()) return null;
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", false);
        body.put("message", "Validation failed");
        body.put("errors", errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Handle ClientAbortException - occurs when client closes connection (e.g., closing tab during video stream)
     * We MUST NOT attempt to write a response body when this happens, as the stream is already closed.
     */
    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbortException(ClientAbortException ex, HttpServletRequest request) {
        // Log at DEBUG level only - this is expected behavior when users close tabs/navigate away
        logger.debug("Client aborted connection: {} {}", request.getMethod(), request.getRequestURI());
        // DO NOT return a ResponseEntity or try to write to the response - the connection is already closed
    }

    /**
     * Handle HttpMessageNotWritableException - occurs when trying to write to an already committed response
     * This typically happens after a ClientAbortException when the response Content-Type is already set
     */
    @ExceptionHandler(HttpMessageNotWritableException.class)
    public void handleHttpMessageNotWritableException(HttpMessageNotWritableException ex,
                                                       HttpServletRequest request,
                                                       HttpServletResponse response) {
        // Only log if the response is NOT committed (unexpected scenario)
        if (!response.isCommitted()) {
            logger.warn("Cannot write message to response for {} {}: {}",
                       request.getMethod(), request.getRequestURI(), ex.getMessage());
        } else {
            // Response already committed - this is expected after ClientAbortException
            logger.debug("Response already committed for {} {}", request.getMethod(), request.getRequestURI());
        }
        // DO NOT attempt to write a response - it's already committed
    }

    /**
     * Handle general IOException - but check if it's a client abort first
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, String>> handleIOException(IOException ex,
                                                                   HttpServletRequest request,
                                                                   HttpServletResponse response) {
        // Check if this is a client abort exception (connection reset by peer)
        if (ex.getMessage() != null &&
            (ex.getMessage().contains("Connection reset by peer") ||
             ex.getMessage().contains("Broken pipe"))) {
            logger.debug("Client connection closed: {} {}", request.getMethod(), request.getRequestURI());
            return null; // Don't try to write response
        }

        // Only attempt to return error response if not committed
        if (response.isCommitted()) {
            logger.debug("Cannot send error response - already committed: {} {}",
                        request.getMethod(), request.getRequestURI());
            return null;
        }

        logger.error("Connection error occurred: {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "Connection error");
        error.put("message", "Unable to connect to the service. Please try again.");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, String>> handleSQLException(SQLException ex, HttpServletResponse response) {
        if (response.isCommitted()) {
            logger.debug("Cannot send SQL error response - already committed");
            return null;
        }

        logger.error("Database error occurred: {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "Database error");
        error.put("message", "Database connection failed. Please try again.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex, HttpServletResponse response) {
        if (response.isCommitted()) {
            logger.debug("Cannot send runtime error response - already committed");
            return null;
        }

        logger.error("Runtime error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity
                .badRequest()
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Handle NoHandlerFoundException - when no handler is found for a request
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNotFound(NoHandlerFoundException ex) {
        logger.debug("Handler not found: {}", ex.getRequestURL());
        return "forward:/404.html";
    }

    /**
     * Handle NoResourceFoundException - when a static resource is not found
     * This is common for missing images/videos - log at DEBUG level to avoid log clutter
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFound(NoResourceFoundException ex) {
        // Log at DEBUG level - missing profile images and other resources are common
        logger.debug("Resource not found: {}", ex.getResourcePath());
        return "forward:/404.html";
    }

    /**
     * Handle all other exceptions as a fallback
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, HttpServletResponse response) {
        if (response.isCommitted()) {
            logger.debug("Cannot handle exception - response already committed: {}", ex.getMessage());
            return null;
        }

        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return "forward:/404.html";
    }
}