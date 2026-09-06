package io.virinchi.dhammanature.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/** FR-08: Quiz Module - encourages Dhamma learning and improves engagement. */
@Entity
@Table(name = "quiz")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"questions", "attempts"})
@EqualsAndHashCode(of = "id")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    private String description;

    /** Points awarded to reward balance on a passing attempt (feeds FR-09). */
    @Builder.Default
    private int rewardPoints = 10;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuizQuestion> questions = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuizAttempt> attempts = new ArrayList<>();
}
