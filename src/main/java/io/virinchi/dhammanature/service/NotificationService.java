package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.Notification;
import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.model.enums.NotificationType;
import io.virinchi.dhammanature.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** FR-10: Notification System. */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void notifyUser(User user, String title, String message, NotificationType type) {
        notificationRepository.save(Notification.builder()
                .user(user).title(title).message(message).type(type).build());
    }

    public void broadcast(String title, String message, NotificationType type) {
        notificationRepository.save(Notification.builder()
                .user(null).title(title).message(message).type(type).build());
    }

    /** Personal notifications + platform-wide broadcasts, newest first. */
    public List<Notification> forUser(Integer userId) {
        return Stream.concat(
                        notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream(),
                        notificationRepository.findByUserIsNullOrderByCreatedAtDesc().stream())
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }
}
