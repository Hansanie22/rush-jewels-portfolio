package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MessageProducerService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendEmailMessage(Map<String, Object> emailData) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_EMAIL, emailData);
    }

    public void sendPdfGenerationMessage(Map<String, Object> pdfData) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_PDF, pdfData);
    }
}
