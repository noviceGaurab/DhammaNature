package io.virinchi.dhammanature.dto;

import java.time.LocalDateTime;

/**
 * A lightweight participant summary for the discuss section.
 * Registered members get richer info (id, bio, profile photo, last-seen); guests only carry a name.
 */
public record ParticipantView(
        Integer id,
        String name,
        String bio,
        String profileImage,
        String roleLabel,
        boolean guest,
        boolean afk,
        long daysAway,
        LocalDateTime lastSeenAt,
        String avatarFallback,
        boolean blockedByMe
) {
}