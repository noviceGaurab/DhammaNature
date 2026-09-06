package io.virinchi.dhammanature.model;

import io.virinchi.dhammanature.model.enums.SessionMode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/** FR-04: Event Management - organizations publish meditation programs and events. */
@Entity
@Table(name = "event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"meditationCenter", "bookings"})
@EqualsAndHashCode(of = "id")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(length = 4000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    private String venue;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SessionMode mode = SessionMode.PHYSICAL;

    private Integer capacity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "center_id", nullable = false)
    private MeditationCenter meditationCenter;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Booking> bookings = new HashSet<>();
}
