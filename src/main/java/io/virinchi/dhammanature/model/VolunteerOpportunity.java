package io.virinchi.dhammanature.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/** NFR-06: Volunteer Registration - food distribution, cleaning, tree plantation, religious events. */
@Entity
@Table(name = "volunteer_opportunity")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"meditationCenter", "registrations"})
@EqualsAndHashCode(of = "id")
public class VolunteerOpportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private LocalDate opportunityDate;

    private String location;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "center_id", nullable = false)
    private MeditationCenter meditationCenter;

    @OneToMany(mappedBy = "opportunity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<VolunteerRegistration> registrations = new HashSet<>();
}
