package io.virinchi.dhammanature.model;

import jakarta.persistence.*;
import lombok.*;

/** A recurring meditation/yoga program offered by a center (Class Diagram: Program). */
@Entity
@Table(name = "program")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = "meditationCenter")
@EqualsAndHashCode(of = "id")
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private String instructorName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "center_id", nullable = false)
    private MeditationCenter meditationCenter;
}
