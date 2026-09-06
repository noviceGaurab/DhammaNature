package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.model.enums.Role;
import io.virinchi.dhammanature.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Replaces the old UserControllerImplements JDBC calls (userSignup / userExists)
 * with a proper service backed by Spring Data JPA, and upgrades plain-text
 * password storage to BCrypt hashing.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean emailTaken(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    public User signup(String fullName, String email, String rawPassword, String genderIdentity,
                        boolean readGuidelines, boolean understoodGuidelines, boolean acceptTerms) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank() || !acceptTerms) {
            throw new IllegalArgumentException("Email, password and Terms of Service acceptance are required.");
        }
        if (emailTaken(email)) {
            throw new IllegalStateException("Sign up failed - email already exists.");
        }
        User user = User.builder()
                .fullName((fullName == null || fullName.isBlank()) ? email.split("@")[0] : fullName)
                .email(email.trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .genderIdentity(genderIdentity)
                .readGuidelines(readGuidelines)
                .understoodGuidelines(understoodGuidelines)
                .acceptedTerms(acceptTerms)
                .role(Role.USER)
                .build();
        User saved = userRepository.save(user);
        notificationService.notifyUser(saved, "Welcome to Dhamma Nature",
                "Thanks for joining, " + saved.getFullName() + ". Explore meditation centers, events and Dhamma teachings to start earning reward points.",
                io.virinchi.dhammanature.model.enums.NotificationType.SYSTEM);
        return saved;
    }

    /** Returns the authenticated user, or empty if the email/password combination is wrong. */
    public Optional<User> authenticate(String email, String rawPassword) {
        if (email == null || rawPassword == null) {
            return Optional.empty();
        }
        return userRepository.findByEmailIgnoreCase(email.trim())
                .filter(User::isActive)
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPasswordHash()));
    }
}
