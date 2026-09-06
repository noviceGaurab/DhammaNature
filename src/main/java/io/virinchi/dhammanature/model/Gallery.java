package io.virinchi.dhammanature.model;

import jakarta.persistence.*;
import lombok.*;

/** Gallery of photos - kept from the original gallery table, now optionally linked to a center. */
@Entity
@Table(name = "gallery")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = "meditationCenter")
@EqualsAndHashCode(of = "id")
public class Gallery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;

    @Column(nullable = false)
    private String imageUrl;

    @Column(length = 2000)
    private String description;

    /** Nullable: general platform gallery images have no center. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id")
    private MeditationCenter meditationCenter;
}
