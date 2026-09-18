package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Lightweight heartbeat tracker so "last seen" (and the AFK nudges) stay accurate without a write per page view. */
@Service
@RequiredArgsConstructor
public class UserActivityService {

    private static final int TOUCH_INTERVAL_MINUTES = 5;

    private final UserRepository userRepository;

    @Transactional
    public void recordActivity(Integer userId) {
        if (userId == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        userRepository.touchLastSeen(userId, now, now.minusMinutes(TOUCH_INTERVAL_MINUTES));
    }
}