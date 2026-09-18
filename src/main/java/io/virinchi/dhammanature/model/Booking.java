package io.virinchi.dhammanature.model;

import io.virinchi.dhammanature.model.enums.BookingStatus;
import io.virinchi.dhammanature.model.enums.PaymentMethod;
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

    /** FR-04 extension: how the booking was paid (null/free for free sessions). */
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    /** FR-04 extension: reward points redeemed toward this booking. */
    @Builder.Default
    private int pointsUsed = 0;

    /** What the attendee plans to bring to the session (requested before booking). */
    @Column(length = 1000)
    private String itemsToBring;

    /** Credential/reference the attendee gave (e.g. student or member ID) before booking. */
    @Column(length = 200)
    private String credential;

    /** Whether the attendee accepted the center's etiquette &amp; rules agreement. */
    @Builder.Default
    private boolean etiquetteAgreed = false;

    @Column(updatable = false)
    private LocalDateTime bookingDate;

    @PrePersist
    void onCreate() {
        this.bookingDate = LocalDateTime.now();
    }
}
