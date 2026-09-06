package io.virinchi.dhammanature.model;

import io.virinchi.dhammanature.model.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** FR-10: Notification System - automated alternative to manual Facebook/notice-board announcements. */
@Entity
@Table(name = "notification")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = "user")
@EqualsAndHashCode(of = "id")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Null = broadcast notification shown to every user. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationType type = NotificationType.SYSTEM;

    @Builder.Default
    private boolean isRead = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
