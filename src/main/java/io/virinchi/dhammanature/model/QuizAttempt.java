package io.virinchi.dhammanature.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_attempt")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"user", "quiz"})
@EqualsAndHashCode(of = "id")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private int totalQuestions;

    @Column(updatable = false)
    private LocalDateTime attemptedAt;

    @PrePersist
    void onCreate() {
        this.attemptedAt = LocalDateTime.now();
    }
}
