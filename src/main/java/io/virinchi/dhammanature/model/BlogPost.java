package io.virinchi.dhammanature.model;

import io.virinchi.dhammanature.model.enums.BlogStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Blog article. Articles are written by logged-in users (or seeded by admins);
 * each post starts in {@code PENDING} status and only becomes visible on the
 * public blog once an administrator approves it (see AdminController).
 */
@Entity
@Table(name = "blog_post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    private String category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    private String authorName;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BlogStatus status = BlogStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private int likeCount = 0;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}