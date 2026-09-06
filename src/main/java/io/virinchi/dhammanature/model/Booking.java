package io.virinchi.dhammanature.model;

import io.virinchi.dhammanature.model.enums.BookingStatus;
import io.virinchi.dhammanature.model.enums.SessionMode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** FR-03/FR-04: booking a meditation session or event (validated by Mokshya interview). */
@Entity
@Table(name = "booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "event"})
@EqualsAndHashCode(of = "id")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SessionMode attendanceMode = SessionMode.PHYSICAL;

    @Builder.Default
    private int numberOfAttendees = 1;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(updatable = false)
    private LocalDateTime bookingDate;

    @PrePersist
    void onCreate() {
        this.bookingDate = LocalDateTime.now();
    }
}
