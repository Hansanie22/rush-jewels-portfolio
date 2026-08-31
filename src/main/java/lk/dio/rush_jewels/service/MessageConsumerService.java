package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class MessageConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumerService.class);

    // Assuming there are EmailService and PDFService injected here in reality
    // @Autowired private EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL)
    public void consumeEmailMessage(Map<String, Object> emailData) {
        logger.info("Received message to send email: {}", emailData);
        try {
            // Process the email sending asynchronously here
            // emailService.sendEmail(...)
            Thread.sleep(1000); // Simulating time-consuming task
            logger.info("Email sent successfully.");
        } catch (Exception e) {
            logger.error("Error sending email: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PDF)
    public void consumePdfMessage(Map<String, Object> pdfData) {
        logger.info("Received message to generate PDF: {}", pdfData);
        try {
            // Process PDF generation asynchronously here
            Thread.sleep(2000); // Simulating time-consuming task
            logger.info("PDF generated successfully.");
        } catch (Exception e) {
            logger.error("Error generating PDF: {}", e.getMessage());
        }
    }
}
