package io.virinchi.dhammanature.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Lightweight analytics log, carried over from the original page_interactions table. */
@Entity
@Table(name = "page_interaction")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = "user")
@EqualsAndHashCode(of = "id")
public class PageInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String pageName;

    private String section;

    private String contentInteraction;

    @Column(updatable = false)
    private LocalDateTime interactionTime;

    @PrePersist
    void onCreate() {
        this.interactionTime = LocalDateTime.now();
    }
}
