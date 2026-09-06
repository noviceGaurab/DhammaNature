package io.virinchi.dhammanature.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quiz_question")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = "quiz")
@EqualsAndHashCode(of = "id")
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false, length = 1000)
    private String questionText;

    @Column(nullable = false)
    private String optionA;
    @Column(nullable = false)
    private String optionB;
    private String optionC;
    private String optionD;

    /** "A", "B", "C" or "D". */
    @Column(nullable = false)
    private String correctOption;
}
