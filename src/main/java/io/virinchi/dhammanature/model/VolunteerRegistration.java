package io.virinchi.dhammanature.model;

import io.virinchi.dhammanature.model.enums.VolunteerStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "volunteer_registration")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"user", "opportunity"})
@EqualsAndHashCode(of = "id")
public class VolunteerRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private VolunteerOpportunity opportunity;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VolunteerStatus status = VolunteerStatus.REGISTERED;

    @Column(length = 100)
    private String studentIdNumber;

    @Column(length = 255)
    private String studentIdImage;

    @Column(length = 255)
    private String collegeApprovalImage;

    @Column(length = 150)
    private String collegeName;

    @Builder.Default
    private boolean warningsAccepted = false;

    @Column(updatable = false)
    private LocalDateTime registeredAt;

    @PrePersist
    void onCreate() {
        this.registeredAt = LocalDateTime.now();
    }
}
