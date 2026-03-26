package com.dev.notification.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendWelcomeEmail_success() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendWelcomeEmail("test@example.com", "Test User");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendPolicyConfirmationEmail_success() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendPolicyConfirmationEmail("test@example.com", 1L, "Home Insurance", 1500.0);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
