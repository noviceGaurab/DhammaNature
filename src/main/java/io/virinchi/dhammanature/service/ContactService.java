package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.ContactMessage;
import io.virinchi.dhammanature.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** Contact form handling: validates and persists public messages submitted via /contact. */
@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;

    public ContactMessage submit(String name, String email, String subject, String message) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Please enter your name.");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Please enter a subject.");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Please write a message.");
        }
        return contactMessageRepository.save(ContactMessage.builder()
                .name(name.trim())
                .email(email.trim())
                .subject(subject.trim())
                .message(message.trim())
                .build());
    }

    public List<ContactMessage> list() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc();
    }

    public long unresolvedCount() {
        return contactMessageRepository.countUnresolved();
    }
}