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
    public void sendClaimStatusEmail(String toEmail, Long claimId, String status, Double amount) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject("SmartSure: Claim Status Update");
            
            String statusColor = "APPROVED".equalsIgnoreCase(status) ? "green" : "red";
            String htmlContent = "<h3>Claim " + status + "</h3>"
                    + "<p>Your claim (ID: <b>" + claimId + "</b>) has been reviewed.</p>"
                    + "<p>Status: <b style='color:" + statusColor + "'>" + status + "</b></p>"
                    + ("APPROVED".equalsIgnoreCase(status) ? "<p>Approved Amount: Rs. " + amount + "</p>" : "<p>We regret to inform you that your claim was not approved.</p>")
                    + "<br/><p>Best Regards,</p><p>SmartSure Team</p>";
                    
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Claim status email sent successfully to {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send claim status email to {}", toEmail, e);
        }
    }

    public void sendForgotPasswordEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject("SmartSure: Password Reset OTP");

            String htmlContent = "<h3>Password Reset Request</h3>"
                    + "<p>You have requested to reset your password.</p>"
                    + "<p>Use the following OTP to complete the process. This OTP is valid for 5 minutes.</p>"
                    + "<h2 style='color:blue'>" + otp + "</h2>"
                    + "<br/><p>If you did not request this, please ignore this email.</p>"
                    + "<br/><p>Best Regards,</p><p>SmartSure Team</p>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Forgot password email sent successfully to {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send forgot password email to {}", toEmail, e);
        }
    }
}
