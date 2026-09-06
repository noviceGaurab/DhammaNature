package io.virinchi.dhammanature.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** A single post inside a DiscussionTopic thread (FR-06). */
@Entity
@Table(name = "comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"topic", "user"})
@EqualsAndHashCode(of = "id")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private DiscussionTopic topic;

    /** Nullable so unregistered visitors can still leave a name/email + comment, as the old form allowed. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String name;

    private String email;

    @Column(nullable = false, length = 2000)
    private String content;

    /** Only administrators may remove misleading content (Business Rule 7); no hard delete needed for that. */
    @Builder.Default
    private boolean hidden = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
