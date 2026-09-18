package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.User;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Transactional email delivery. All outbound mail goes through the configured
 * Gmail SMTP account; user-facing confirmations go to the acting user and site
 * alerts are mirrored to the owner's inbox.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${app.notify-email:}")
    private String notifyEmail;

    public void send(String to, String subject, String text) {
        if (to == null || to.isBlank()) {
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (!mailFrom.isBlank()) {
                message.setFrom(mailFrom.trim());
            }
            message.setTo(to.trim());
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (MailException e) {
            log.warn("Email not sent to {} (subject '{}'): {}", to, subject, e.getMessage());
        }
    }

    public void sendToUser(User user, String subject, String text) {
        if (user != null) {
            send(user.getEmail(), subject, text);
        }
    }

    /** Site alert emailed to the owner's inbox (defaults to the mail account). */
    public void sendSiteAlert(String subject, String text) {
        String target = notifyEmail.isBlank() ? mailFrom : notifyEmail;
        send(target, subject, text);
    }
}