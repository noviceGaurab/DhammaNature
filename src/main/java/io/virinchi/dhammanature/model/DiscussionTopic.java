package io.virinchi.dhammanature.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * FR-06: Community Discussion Forum. Each page-level comment widget in the old
 * JSP site (community_discuss, sermon1_discuss...) now maps to one topic here,
 * identified by its slug, so existing comment counts/threads carry a clean
 * one-to-many relationship instead of a bare "page_name" string on every row.
 */
@Entity
@Table(name = "discussion_topic")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "comments")
@EqualsAndHashCode(of = "id")
public class DiscussionTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    private String category;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Comment> comments = new HashSet<>();
}
