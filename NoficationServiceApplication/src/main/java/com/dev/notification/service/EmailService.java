package com.dev.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendWelcomeEmail(String toEmail, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to SmartSure!");
            
            String htmlContent = "<h3>Hello " + name + ",</h3>"
                    + "<p>Welcome to SmartSure Insurance.</p>"
                    + "<p>We are thrilled to have you on board. You can now log in and browse our comprehensive policy offerings.</p>"
                    + "<br/><p>Best Regards,</p><p>SmartSure Team</p>";
                    
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Welcome email sent successfully to {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to {}", toEmail, e);
        }
    }

    public void sendPolicyConfirmationEmail(String toEmail, Long policyId, String policyName, Double amount) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject("SmartSure: Policy Purchase Confirmation");
            
            String htmlContent = "<h3>Thank You for Your Purchase!</h3>"
                    + "<p>Your policy <b>" + policyName + "</b> has been successfully activated.</p>"
                    + "<p><b>Policy ID:</b> " + policyId + "</p>"
                    + "<p><b>Premium Base Amount:</b> Rs. " + amount + "</p>"
                    + "<br/><p>Best Regards,</p><p>SmartSure Team</p>";
                    
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Policy confirmation email sent successfully to {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send policy confirmation email to {}", toEmail, e);
        }
    }
}
