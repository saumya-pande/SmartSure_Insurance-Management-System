package com.dev.notification.listener;


import com.dev.notification.config.RabbitMQConfig;
import com.dev.notification.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

    @Autowired
    private EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_USER_REGISTERED)
    public void consumeUserRegistrationEvent(Map<String, String> payload) {
        String email = payload.get("email");
        String name = payload.getOrDefault("name", "User");
        log.info("Received Registration Event for user: {}", email);
        emailService.sendWelcomeEmail(email, name);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_POLICY_PURCHASED)
    public void consumePolicyPurchasedEvent(Map<String, Object> payload) {
        String email = (String) payload.get("email");
        Long policyId = Long.valueOf(payload.get("policyId").toString());
        String policyName = (String) payload.get("policyName");
        Double amount = Double.valueOf(payload.get("amount").toString());

        log.info("Received Policy Purchased Event for Policy ID: {}", policyId);
        emailService.sendPolicyConfirmationEmail(email, policyId, policyName, amount);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_POLICY_TYPE_CREATED)
    public void consumePolicyTypeCreatedEvent(Map<String, Object> payload) {
        // Simple log for now, as requested.
        log.info("Admin Action Detected: New Policy Type Created! Details: {}", payload);
    }
}
