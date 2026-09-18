package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.model.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * The "community cares" nudge layer for the discuss page. These fire only from the
 * hover-on-participant menu: a private message, a random Dhamma quote, or a gentle
 * reach-out to someone who has been away for a while. Everything is delivered as a
 * personal notification so the other person sees it at the top of their profile.
 */
@Service
@RequiredArgsConstructor
public class NudgeService {

    private final NotificationService notificationService;
    private final io.virinchi.dhammanature.repository.UserRepository userRepository;

    private static final List<String> DHAMMA_QUOTES = List.of(
            "Better than a thousand hollow words is one word that brings peace. — The Buddha (Dhammapada 100)",
            "You yourself, as much as anybody in the entire universe, deserve your love and affection. — The Buddha",
            "Peace comes from within. Do not seek it without. — The Buddha",
            "The mind is everything. What you think you become. — The Buddha (attrib.)",
            "Hatred does not cease by hatred, but only by love; this is the eternal rule. — Dhammapada 5",
            "In the end, only three things matter: how much you loved, how gently you lived, and how gracefully you let go of things not meant for you. — The Buddha (attrib.)",
            "As a solid rock cannot be moved by the wind, the wise are not shaken by praise or blame. — Dhammapada 81",
            "Just as a candle cannot burn without fire, men cannot live without a spiritual life. — The Buddha (attrib.)",
            "However many holy words you read, however many you speak, what good will they do you if you do not act upon them? — The Buddha",
            "Doubt everything. Find your own light. — paraphrase of the Buddha's last words");

    public void sendMessage(User sender, Integer targetId, String message) {
        User target = requireRegistered(targetId);
        notificationService.notifyUser(target, "Private message from " + sender.getFullName(),
                sender.getFullName() + " wrote to you personally:\n" + message.trim(),
                NotificationType.PRIVATE_MESSAGE);
    }

    public void sendQuote(User sender, Integer targetId) {
        User target = requireRegistered(targetId);
        notificationService.notifyUser(target, sender.getFullName() + " sent you a Dhamma quote",
                randomQuote() + "\n\n— with metta, " + sender.getFullName(),
                NotificationType.DHAMMA_QUOTE);
    }

    public void sendAfkNudge(User sender, Integer targetId) {
        User target = requireRegistered(targetId);
        long daysAway = daysAway(target.getLastSeenAt());
        String howLong = daysAway <= 0 ? "a little while"
                : daysAway == 1 ? "a day"
                : daysAway + " days";
        String message = "The Dhamma Nature community has been missing your presence — you have been away for "
                + howLong + ". " + sender.getFullName()
                + " reached out to let you know your voice and practice matter deeply here, and we would love to see you back in the discussion soon. May your days be filled with peace.";
        notificationService.notifyUser(target, "The community misses you, " + target.getFullName(),
                message, NotificationType.NUDGE);
    }

    private String randomQuote() {
        return DHAMMA_QUOTES.get(ThreadLocalRandom.current().nextInt(DHAMMA_QUOTES.size()));
    }

    private long daysAway(LocalDateTime lastSeenAt) {
        return lastSeenAt == null ? 0 : DAYS.between(lastSeenAt, LocalDateTime.now());
    }

    private User requireRegistered(Integer targetId) {
        if (targetId == null) {
            throw new NoSuchElementException("Guest participants cannot receive nudges.");
        }
        return userRepository.findById(targetId)
                .orElseThrow(() -> new NoSuchElementException("Participant not found"));
    }
}