package com.alerts.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service for sending email notifications
 */
@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${external.mail.from-email:noreply@pi-system.com}")
    private String fromEmail;

    @Value("${spring.mail.enabled:true}")
    private boolean emailEnabled;

    /**
     * Send a simple email notification
     */
    public void sendEmail(String to, String subject, String body) {
        if (!emailEnabled || mailSender == null) {
            log.warn("Email service is disabled or not configured. Skipping email to: {}", to);
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}. Error: {}", to, e.getMessage());
            // Don't throw exception - just log it
        }
    }

    /**
     * Send email with HTML content (future enhancement)
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        // Implementation for HTML emails can be added here
        log.info("HTML email support - to be implemented");
    }
}
