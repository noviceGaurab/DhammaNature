package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUser_IdOrderByCreatedAtDesc(Integer userId);
    List<Notification> findByUserIsNullOrderByCreatedAtDesc();
}
