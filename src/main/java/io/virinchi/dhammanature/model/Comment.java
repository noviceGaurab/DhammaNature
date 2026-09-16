package io.virinchi.dhammanature.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** A single post inside a DiscussionTopic thread (FR-06). Supports an optional title and nested replies. */
@Entity
@Table(name = "comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"topic", "user", "parent", "replies"})
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

    /** Optional title describing what this post is about (the "share a thought" title field). */
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    /** Self-reference used to thread replies under a parent post. Null = top-level comment. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC, id ASC")
    @Builder.Default
    private List<Comment> replies = new ArrayList<>();

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