package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.PasswordResetToken;
import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.repository.PasswordResetTokenRepository;
import io.virinchi.dhammanature.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * "Forgot password" flow: issues a short-lived reset token by email and applies
 * the new BCrypt-hashed password when a valid, unexpired token is presented.
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final long EXPIRE_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${app.base-url:http://localhost:3069}")
    private String baseUrl;

    /**
     * Emails the owner of {@code email} a reset link. The method always returns
     * quietly so the form never reveals whether an account exists.
     */
    @Transactional
    public void requestPasswordReset(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        userRepository.findByEmailIgnoreCase(email.trim()).ifPresent(user -> {
            tokenRepository.invalidateTokensFor(user.getId());
            String token = UUID.randomUUID().toString().replace("-", "");
            tokenRepository.save(PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusMinutes(EXPIRE_MINUTES))
                    .build());
            sendResetEmail(user, token);
        });
    }

    @Transactional
    public boolean resetPassword(String token, String rawPassword) {
        if (token == null || token.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            return false;
        }
        return tokenRepository.findByToken(token.trim())
                .filter(t -> !t.isUsed())
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(t -> {
                    User user = t.getUser();
                    user.setPasswordHash(passwordEncoder.encode(rawPassword));
                    userRepository.save(user);
                    t.setUsed(true);
                    tokenRepository.save(t);
                    return true;
                })
                .orElse(false);
    }

    private void sendResetEmail(User user, String token) {
        String resetUrl = baseUrl + "/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Reset your Dhamma Nature password");
        message.setText("Hello " + user.getFullName() + ",\n\n"
                + "We received a request to reset your Dhamma Nature password. "
                + "Use the link below to choose a new password. It is valid for "
                + EXPIRE_MINUTES + " minutes:\n\n"
                + resetUrl + "\n\n"
                + "If you didn't ask for this, you can simply ignore this email - "
                + "your password will stay the same.\n\n"
                + "With metta,\nThe Dhamma Nature team");
        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("Password reset email could not be sent to {} : {}", user.getEmail(), e.getMessage());
            log.info("Password reset link for {} : {}", user.getEmail(), resetUrl);
        }
    }
}