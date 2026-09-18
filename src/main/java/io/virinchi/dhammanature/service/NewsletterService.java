package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.NewsletterSubscriber;
import io.virinchi.dhammanature.repository.NewsletterSubscriberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

/** Newsletter sign-up: keeps a unique subscriber list and confirms by email. */
@Service
@RequiredArgsConstructor
public class NewsletterService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final NewsletterSubscriberRepository subscriberRepository;
    private final EmailService emailService;

    public record SubscriptionResult(boolean success, boolean alreadySubscribed, String message) {
    }

    @Transactional
    public SubscriptionResult subscribe(String email) {
        String cleaned = email == null ? "" : email.trim().toLowerCase();
        if (cleaned.isBlank()) {
            return new SubscriptionResult(false, false, "Please enter your email address to subscribe.");
        }
        if (!EMAIL_PATTERN.matcher(cleaned).matches()) {
            return new SubscriptionResult(false, false, "Please enter a valid email address.");
        }
        if (subscriberRepository.existsByEmailIgnoreCase(cleaned)) {
            return new SubscriptionResult(false, true,
                    "You are already subscribed with " + cleaned + ".");
        }

        subscriberRepository.save(NewsletterSubscriber.builder().email(cleaned).active(true).build());
        emailService.send(cleaned, "Welcome to the Dhamma Nature newsletter",
                "Dear subscriber,\n\n"
                        + "Thank you for subscribing to the Dhamma Nature newsletter.\n"
                        + "You will receive sermons, teachings, events and updates directly in your inbox.\n\n"
                        + "With metta,\nThe Dhamma Nature team");
        return new SubscriptionResult(true, false,
                "Thank you for subscribing: " + cleaned + ". You are now on our newsletter list.");
    }
}