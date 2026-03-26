package com.dev.notification.listener;

import com.dev.notification.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationListener notificationListener;

    @Test
    void consumeUserRegistrationEvent_success() {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", "test@example.com");
        payload.put("name", "Test User");

        notificationListener.consumeUserRegistrationEvent(payload);

        verify(emailService, times(1)).sendWelcomeEmail("test@example.com", "Test User");
    }

    @Test
    void consumePolicyPurchasedEvent_success() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", "test@example.com");
        payload.put("policyId", 1L);
        payload.put("policyName", "Home Insurance");
        payload.put("amount", 1500.0);

        notificationListener.consumePolicyPurchasedEvent(payload);

        verify(emailService, times(1)).sendPolicyConfirmationEmail(
                eq("test@example.com"), eq(1L), eq("Home Insurance"), eq(1500.0));
    }
}
